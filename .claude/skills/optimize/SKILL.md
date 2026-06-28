---
name: optimize
description: Improve performance across Spring Boot backend and React frontend. Database queries, API response times, bundle size, rendering.
user-invokable: true
args:
  - name: target
    description: The area to optimize
    required: false
---

# Performance Optimization

## Backend (Spring Boot)
- JPA query optimization (avoid N+1, use `@EntityGraph` or `JOIN FETCH`)
- Pagination for all list endpoints (`Page<T>`)
- Database indexes on frequently queried columns
- Response compression (gzip)
- Connection pooling (HikariCP tuning)
- Async processing for email sending and IRS status polling
- Caching for static data (form type definitions, state lists)

## Frontend (React)
- Code splitting with `React.lazy()` and `Suspense`
- Memoization with `useMemo` and `useCallback` where measured
- Virtualized lists for large datasets (submission history)
- Image optimization (SVG for icons, WebP for images)
- Tailwind CSS purge for minimal CSS bundle
- API response caching with stale-while-revalidate pattern

## Database
- Proper indexes on foreign keys and query predicates
- Query analysis with `EXPLAIN` for slow queries
- Batch inserts for bulk upload processing
- Connection pool sizing for expected concurrency
