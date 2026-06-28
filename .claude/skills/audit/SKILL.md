---
name: audit
description: Comprehensive audit of code quality, security, accessibility, performance, and IRS compliance across the Spring Boot backend and React frontend.
user-invokable: true
args:
  - name: target
    description: The area or module to audit
    required: false
---

# Platform Audit

Systematic quality audit for the IRS 1099 filing platform.

## Audit Dimensions

### 1. Security (CRITICAL)
- PII encryption (TIN/SSN/EIN via AES-256-GCM)
- Password hashing (BCrypt strength 12)
- JWT token lifecycle (expiry, refresh, revocation)
- CORS configuration
- CSRF protection
- Input sanitization (XSS, SQL injection)
- Audit logging completeness
- IRS Pub 4557 compliance

### 2. Backend Quality
- JPA entity relationships and indexes
- Repository query efficiency (N+1 detection)
- Service layer transaction boundaries
- REST API consistency and documentation
- Error handling coverage
- Async email processing
- Database migration integrity

### 3. Frontend Quality
- TypeScript strictness (no `any`)
- Component composition and reuse
- Form validation completeness
- API error handling and user feedback
- Loading and empty states
- Responsive design (mobile, tablet, desktop)

### 4. Accessibility
- Semantic HTML structure
- ARIA labels on interactive elements
- Keyboard navigation
- Color contrast ratios
- Screen reader compatibility
- Focus management in forms

### 5. IRS Compliance
- XML schema adherence
- Business rule validation
- Character encoding (UTF-8, no BOM)
- Prohibited character stripping
- UTID uniqueness enforcement
- Record count verification
- CF/SF state filing support

## Output

For each finding, provide:
- Severity (CRITICAL/HIGH/MEDIUM/LOW)
- Category (from dimensions above)
- Location (file:line)
- Description and remediation
