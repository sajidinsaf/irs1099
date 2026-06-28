---
name: harden
description: Improve platform resilience through better error handling, input validation, edge case management, and security hardening for both Spring Boot backend and React frontend.
user-invokable: true
args:
  - name: target
    description: The feature or area to harden
    required: false
---

# Platform Hardening

Strengthen the IRS 1099 platform against edge cases, errors, and real-world usage.

## Backend Hardening (Spring Boot)

### Input Validation
- `@Valid` on all request DTOs
- Custom validators for TIN format, EIN format, state codes
- Request size limits for file uploads (CSV, Excel)
- XML payload size validation (100MB max per IRS spec)

### Transaction Safety
- `@Transactional` on all multi-table operations
- Optimistic locking on Submission and FormRecord entities
- Idempotency keys for payment processing
- Rollback on partial XML generation failures

### Error Resilience
- Retry with exponential backoff for IRS API calls
- Circuit breaker for external service calls (Stripe, IRIS)
- Graceful degradation when AI service is unavailable
- Rate limiting on auth endpoints (prevent brute force)

### PII Protection
- Never log TIN/SSN/EIN values
- Mask PII in API responses (show last 4 digits only)
- Encrypt before database storage
- Audit log all PII access

## Frontend Hardening (React)

### Form Resilience
- Zod validation matching backend constraints
- TIN/SSN input masking (XXX-XX-XXXX format)
- Auto-save drafts to prevent data loss
- Confirmation dialogs before destructive actions

### Network Resilience
- Axios interceptor for JWT refresh
- Toast notifications for network errors
- Optimistic UI updates with rollback
- Offline detection and user notification

### Display Hardening
- Long text truncation with tooltips
- Number formatting for financial amounts
- Date formatting consistency
- Empty state handling for all data views
