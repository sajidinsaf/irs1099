---
name: clarify
description: Improve unclear UX copy, error messages, instructions, and labels. Make the filing experience understandable for non-technical users.
user-invokable: true
args:
  - name: target
    description: The area to clarify
    required: false
---

# Clarify UX Copy

Make the IRS 1099 filing experience understandable for business owners who aren't tax experts.

## Principles
- Use plain English, not IRS jargon
- Explain what users need to DO, not what the system does
- Error messages should say what went wrong AND how to fix it
- Help text should prevent errors, not just explain fields

## Common Clarifications
- "TIN" -> "Tax Identification Number (SSN or EIN)"
- "Transmission" -> "Filing submission"
- "UTID" -> "Tracking number" (show UTID in details for advanced users)
- "Acknowledged" -> "Confirmed by the IRS"
- "CF/SF" -> "Combined Federal/State filing"

## Error Message Pattern
```
What happened: "Your filing was rejected by the IRS."
Why: "The recipient's SSN doesn't match IRS records."
What to do: "Please verify the SSN for [Recipient Name] and resubmit."
```

## Avoid
- Technical error codes without explanation
- Passive voice ("An error was encountered")
- Blame language ("You entered an invalid...")
- IRS publication numbers without context
