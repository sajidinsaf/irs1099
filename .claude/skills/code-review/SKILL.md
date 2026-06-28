---
name: code-review
description: |
  Comprehensive code review for Java/Spring Boot backend and React/TypeScript frontend.
  Catches bugs, improves quality, ensures IRS compliance and PII security.
  Use when: reviewing code changes, PR reviews, architecture reviews, security audits.
---

# Code Review Excellence

Transform code reviews from gatekeeping to knowledge sharing through constructive feedback and systematic analysis.

## Core Principles

- Catch bugs and edge cases
- Ensure code maintainability
- Enforce coding standards
- Verify security (especially PII/TIN handling)
- Check IRS compliance requirements

## Review Checklist

### Java / Spring Boot

1. **JPA/Hibernate**: N+1 queries, missing `@Transactional`, lazy loading issues, missing indexes
2. **Security**: BCrypt for passwords, AES-256 for TIN/SSN/EIN, no PII in logs, proper `@PreAuthorize`
3. **Validation**: `@Valid` on request DTOs, Zod schemas matching backend validation
4. **Error Handling**: Proper exception hierarchy, `GlobalExceptionHandler` coverage, no stack traces in responses
5. **REST API**: Consistent naming, proper HTTP methods/status codes, pagination for lists
6. **Async**: `@Async` on email sending, proper executor configuration, no blocking calls in async methods
7. **Testing**: Service layer unit tests, controller integration tests, security tests

### React / TypeScript

1. **Type Safety**: No `any` types, proper interface definitions, Zod schema validation
2. **State Management**: Zustand store design, no unnecessary re-renders, proper selector usage
3. **Forms**: React Hook Form with Zod resolvers, proper error display, loading states
4. **API Calls**: Proper error handling, loading indicators, retry logic for auth token refresh
5. **Security**: No PII stored in localStorage (only JWT tokens), XSS prevention, CSRF tokens

### IRS 1099 Domain-Specific

1. **TIN Validation**: SSN format (XXX-XX-XXXX), EIN format (XX-XXXXXXX), proper masking in UI
2. **XML Generation**: UTF-8 encoding, no BOM, escaped special characters per IRS spec
3. **UTID Format**: Must match `UUID:IRIS:TCC::A` pattern
4. **Audit Trail**: All PII access must be logged to audit_log table

## Output Format

For each issue:
```
**[SEVERITY]: [title]**
File: path/to/file:line
[Description and fix suggestion]
```

Order by severity: CRITICAL > HIGH > MEDIUM > LOW
