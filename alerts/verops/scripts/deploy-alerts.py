#!/usr/bin/env python3
"""deploy-alerts.py — idempotent Verops alert rule deployment.

Deploys alert rules defined in alerts/verops/rules/*.json to a Verops tenant,
scoped to a specific autopoet environment (preview or production).

Requires VEROPS_USERNAME and VEROPS_PASSWORD environment variables.
Optionally set VEROPS_ORG_SLUG for org-scoped login.

Ref: ski/autopose#607
"""

import argparse
import fnmatch
import json
import os
import sys
import textwrap
import urllib.request
import urllib.error
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
VEROPS_DIR = SCRIPT_DIR.parent
RULES_DIR = VEROPS_DIR / "rules"
ENV_DIR = VEROPS_DIR / "environments"

EPILOG = textwrap.dedent("""\
examples:
  Deploy all alerts to preview (dry run):
    python deploy-alerts.py --env preview --dry-run

  Deploy all alerts to production:
    python deploy-alerts.py --env production

  Deploy only session-health rules:
    python deploy-alerts.py --env preview --file session-health.json

  Deploy only WARNING-level alerts:
    python deploy-alerts.py --env preview --severity WARNING

  Deploy only LCP-related alerts:
    python deploy-alerts.py --env preview --match "LCP*"

  Combine filters — CRITICAL TTFB alerts from the RUM file only:
    python deploy-alerts.py --env preview --file rum-user-experience.json --severity CRITICAL --match "*TTFB*"

  Deploy all nudge and stall alerts at WARNING level:
    python deploy-alerts.py --env preview --match "*Nudge*,*Stall*" --severity WARNING

environment variables:
  VEROPS_USERNAME   Verops login username (required)
  VEROPS_PASSWORD   Verops login password (required)
  VEROPS_ORG_SLUG   Verops org slug (optional)

available rule files:
  session-health.json        Session Health & Frustration     (4 metrics, 8 rules)
  autopoet-operations.json   Autopoet Operations              (5 metrics, 10 rules)
  ndo-variance-drift.json    NDO: Variance & Drift            (5 metrics, 10 rules)
  ndo-verification.json      NDO: Verification                (2 metrics, 4 rules)
  rum-user-experience.json   RUM: User Experience             (5 metrics, 10 rules)

troubleshooting:
  See alerts/verops/TROUBLESHOOTING.md for known Verops API pitfalls.
  Common issues: HTTP 400 with empty body usually means a field name or
  value constraint violation. Check rule names and queryType first.
""")


def load_env(env_file: Path) -> dict:
    """Parse a KEY=VALUE env file, skipping comments and blanks."""
    env = {}
    for line in env_file.read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        key, _, value = line.partition("=")
        env[key.strip()] = value.strip()
    return env


def api(url: str, token: str = None, method: str = "GET", body: dict = None):
    """Make an API request and return (status_code, parsed_json | None)."""
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    data = json.dumps(body).encode() if body else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            return resp.status, json.loads(resp.read())
    except urllib.error.HTTPError as e:
        try:
            resp_body = json.loads(e.read())
        except Exception:
            resp_body = None
        return e.code, resp_body


def authenticate(base_url: str, username: str, password: str, org_slug: str = None) -> str:
    """Authenticate and return JWT token."""
    body = {"username": username, "password": password}
    if org_slug:
        body["orgSlug"] = org_slug
    status, data = api(f"{base_url}/api/auth/login", method="POST", body=body)
    if status != 200 or not data or not data.get("token"):
        print(f"Error: authentication failed (HTTP {status})")
        if data:
            print(json.dumps(data, indent=2))
        sys.exit(1)
    print(f"Authenticated as {data.get('username')} ({data.get('role')})")
    return data["token"]


def fetch_existing_rules(base_url: str, token: str) -> dict:
    """Fetch all existing alert rules, return dict keyed by name."""
    status, data = api(f"{base_url}/api/v1.0/alerts/rules?size=500", token=token)
    if status != 200:
        print(f"Warning: could not fetch existing rules (HTTP {status})")
        return {}
    if isinstance(data, dict) and "content" in data:
        rules = data["content"]
    elif isinstance(data, list):
        rules = data
    else:
        rules = []
    return {r["name"]: r for r in rules}


def substitute(content: str, env: dict) -> str:
    """Replace {{PLACEHOLDER}} tokens in rule JSON."""
    content = content.replace("{{SERVICE_NAME_PATTERN}}", env["SERVICE_NAME_PATTERN"])
    content = content.replace("{{ENV_LABEL}}", env["ENV_LABEL"])
    content = content.replace("{{RUM_APP_ID}}", env.get("RUM_APP_ID", ""))
    return content


def matches_filters(rule: dict, severity: str = None, match_patterns: list = None) -> bool:
    """Check if a rule passes the severity and name-match filters."""
    if severity and rule.get("severity") != severity:
        return False
    if match_patterns:
        name = rule.get("name", "")
        if not any(fnmatch.fnmatch(name, pat) for pat in match_patterns):
            return False
    return True


def process_rules(
    rule_file: Path,
    env: dict,
    existing: dict,
    base_url: str,
    token: str,
    channel_ids: list,
    created_by: str,
    dry_run: bool,
    severity: str = None,
    match_patterns: list = None,
) -> tuple:
    """Process a single rule file. Returns (created, updated, skipped, errors)."""
    print(f"\n=== Processing {rule_file.name} ===")
    raw = rule_file.read_text()
    content = substitute(raw, env)
    rules = json.loads(content)

    created = updated = skipped = errors = 0

    for rule in rules:
        rule["notificationChannelIds"] = channel_ids
        rule["createdBy"] = created_by
        name = rule["name"]

        if not matches_filters(rule, severity, match_patterns):
            skipped += 1
            continue

        existing_rule = existing.get(name)

        if dry_run:
            if existing_rule:
                print(f"  [dry-run] WOULD UPDATE: {name} (id={existing_rule['id']})")
            else:
                print(f"  [dry-run] WOULD CREATE: {name}")
            continue

        if existing_rule:
            rid = existing_rule["id"]
            status, _ = api(
                f"{base_url}/api/v1.0/alerts/rules/{rid}",
                token=token,
                method="PUT",
                body=rule,
            )
            if 200 <= status < 300:
                print(f"  UPDATED: {name} (id={rid})")
                updated += 1
            else:
                print(f"  ERROR updating {name} (HTTP {status})")
                errors += 1
        else:
            status, resp = api(
                f"{base_url}/api/v1.0/alerts/rules",
                token=token,
                method="POST",
                body=rule,
            )
            if 200 <= status < 300:
                new_id = resp.get("id", "?") if resp else "?"
                print(f"  CREATED: {name} (id={new_id})")
                created += 1
            else:
                print(f"  ERROR creating {name} (HTTP {status})")
                if resp:
                    print(f"    {json.dumps(resp)[:200]}")
                errors += 1

    return created, updated, skipped, errors


def main():
    parser = argparse.ArgumentParser(
        description="Deploy Verops alert rules for autopoet environments.",
        epilog=EPILOG,
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument(
        "--env", required=True, choices=["preview", "production"],
        help="Target autopoet environment (determines service_name pattern and notification channels)",
    )
    parser.add_argument(
        "--dry-run", action="store_true",
        help="Show what would be created/updated without making any changes",
    )
    parser.add_argument(
        "--file", dest="rule_file", default=None,
        help="Deploy rules from a single file only (e.g. session-health.json)",
    )
    parser.add_argument(
        "--severity", choices=["WARNING", "CRITICAL"], default=None,
        help="Deploy only rules of this severity level",
    )
    parser.add_argument(
        "--match", dest="match", default=None,
        help='Comma-separated glob patterns matched against rule names (e.g. "*LCP*" or "*Nudge*,*Stall*")',
    )
    args = parser.parse_args()

    # Parse match patterns
    match_patterns = None
    if args.match:
        match_patterns = [p.strip() for p in args.match.split(",") if p.strip()]

    # Load environment
    env_file = ENV_DIR / f"{args.env}.env"
    if not env_file.exists():
        print(f"Error: environment file not found: {env_file}")
        sys.exit(1)
    env = load_env(env_file)

    base_url = env["VEROPS_BASE_URL"]
    created_by = env.get("CREATED_BY", "aai")

    # Parse notification channel IDs
    raw_channels = env.get("NOTIFICATION_CHANNEL_IDS", "")
    channel_ids = [int(x.strip()) for x in raw_channels.split(",") if x.strip()] if raw_channels else []

    # Credentials from env vars
    username = os.environ.get("VEROPS_USERNAME")
    password = os.environ.get("VEROPS_PASSWORD")
    org_slug = os.environ.get("VEROPS_ORG_SLUG", "")
    if not username or not password:
        print("Error: VEROPS_USERNAME and VEROPS_PASSWORD environment variables are required.")
        print()
        parser.print_help()
        sys.exit(1)

    # Show active filters
    active_filters = []
    if args.rule_file:
        active_filters.append(f"file={args.rule_file}")
    if args.severity:
        active_filters.append(f"severity={args.severity}")
    if match_patterns:
        active_filters.append(f"match={','.join(match_patterns)}")
    if active_filters:
        print(f"Filters: {', '.join(active_filters)}")

    # Authenticate
    token = authenticate(base_url, username, password, org_slug or None)

    # Fetch existing rules
    print("Fetching existing alert rules ...")
    existing = fetch_existing_rules(base_url, token)
    print(f"Found {len(existing)} existing rule(s)")

    # Determine rule files
    if args.rule_file:
        path = Path(args.rule_file)
        if not path.exists():
            path = RULES_DIR / args.rule_file
        if not path.exists():
            print(f"Error: rule file not found: {args.rule_file}")
            print(f"\nAvailable rule files in {RULES_DIR}:")
            for f in sorted(RULES_DIR.glob("*.json")):
                print(f"  {f.name}")
            sys.exit(1)
        rule_files = [path]
    else:
        rule_files = sorted(RULES_DIR.glob("*.json"))

    if not rule_files:
        print("No rule files found.")
        sys.exit(1)

    # Process
    total_created = total_updated = total_skipped = total_errors = 0
    for rf in rule_files:
        c, u, s, e = process_rules(
            rf, env, existing, base_url, token, channel_ids, created_by,
            args.dry_run, args.severity, match_patterns,
        )
        total_created += c
        total_updated += u
        total_skipped += s
        total_errors += e

    # Summary
    print(f"\n=== Summary ({args.env}) ===")
    if args.dry_run:
        print("  (dry-run — no changes made)")
    else:
        print(f"  Created:   {total_created}")
        print(f"  Updated:   {total_updated}")
    if total_skipped:
        print(f"  Skipped:   {total_skipped} (filtered out)")
    if not args.dry_run and total_errors:
        print(f"  Errors:    {total_errors}")

    if total_errors > 0:
        sys.exit(1)


if __name__ == "__main__":
    main()
