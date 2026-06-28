---
name: polish
description: Final quality pass before shipping. Check for inconsistencies, rough edges, and missed details across backend and frontend.
user-invokable: true
args:
  - name: target
    description: The area to polish
    required: false
---

# Final Polish

Last pass before shipping. Find and fix the small things that separate professional software from amateur work.

## Backend Polish
- API response format consistency (always wrap in standard envelope)
- HTTP status codes used correctly (201 for creation, 204 for deletion)
- Logging levels appropriate (DEBUG for dev, INFO for business events, ERROR for failures)
- No TODO comments left in production code
- Application properties documented
- Actuator health check includes database and external service status

## Frontend Polish
- Consistent spacing (Tailwind scale: 4, 6, 8, 12, 16, 20)
- Hover states on all interactive elements
- Focus rings on all focusable elements
- Loading spinners on all async operations
- Success/error toasts for all user actions
- Page titles update with React Router
- Favicon and meta tags complete
- No console.log statements in production
- 404 page for unmatched routes

## Cross-Cutting
- Form validation messages are user-friendly (not technical)
- Error messages never expose system internals
- All dates displayed in user-friendly format
- Currency always formatted with $ and 2 decimal places
- TIN/SSN always masked in display (***-**-1234)
