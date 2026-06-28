---
name: frontend-design
description: Create distinctive, production-grade React + Tailwind CSS interfaces for the IRS 1099 filing platform. Professional, trust-oriented design for financial/tax services.
user-invokable: true
args:
  - name: target
    description: The component or page to design
    required: false
---

# Frontend Design - IRS 1099 Filing Platform

Create professional, trust-oriented interfaces using React 18 + TypeScript + Tailwind CSS.

## Design Language

### Brand Identity
- **Primary**: Navy (#1a365d) - trust, authority
- **Accent**: Blue (#2563eb / primary-600) - action, clarity
- **Success**: Green - accepted submissions
- **Warning**: Amber - pending, needs attention
- **Error**: Red - rejected, validation errors

### Typography
- Font: Inter (Google Fonts)
- Headings: font-bold, text-gray-900
- Body: text-gray-600
- Form labels: text-sm font-medium text-gray-700

### Spacing & Layout
- Max width: max-w-7xl for content areas
- Card padding: p-6
- Section padding: py-20
- Form spacing: space-y-4

## Component Patterns

### Cards
```tsx
<div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
```

### Buttons
```tsx
// Primary: bg-primary-600 text-white hover:bg-primary-700
// Secondary: border-2 border-primary-600 text-primary-600 hover:bg-primary-50
// Always include focus:ring-2 and disabled states
```

### Forms (React Hook Form + Zod)
- Always show validation errors below inputs
- Loading spinner on submit buttons
- Disable form during submission
- Success toast on completion

### Dashboard
- Stats cards with icons (lucide-react)
- Status badges with semantic colors
- Empty states with illustrations and CTAs

## Anti-Patterns
- No generic placeholder text - use realistic tax filing data
- No skeleton screens without actual data loading
- No modals for simple actions - use inline forms
- No dark mode (tax software = professional, clean, light)
- Never show raw TIN/SSN - always mask (***-**-1234)
