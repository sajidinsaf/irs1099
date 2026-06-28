---
name: distill
description: Strip designs and code to their essence. Remove unnecessary complexity, redundant components, and over-engineering.
user-invokable: true
args:
  - name: target
    description: The area to distill
    required: false
---

# Distill to Essence

Remove unnecessary complexity. Every element must earn its place.

## Questions for Every Element
1. Does this serve the user's goal of filing 1099s?
2. Can this be removed without losing functionality?
3. Can two components be merged into one?
4. Is this abstraction necessary or premature?

## Backend Distillation
- Remove service interfaces with single implementations (use concrete classes)
- Remove DTOs that mirror entities 1:1 (create only when transformation needed)
- Remove unused configuration properties
- Collapse thin controller -> service -> repository layers where logic is trivial

## Frontend Distillation
- Remove wrapper components that only add a className
- Remove state management for data that can be derived
- Remove utility functions used once (inline them)
- Replace complex component hierarchies with simpler flat structures

## Design Distillation
- Remove decorative elements that don't aid comprehension
- Reduce color palette to minimum needed for meaning
- Remove animations that don't communicate state changes
- Simplify navigation (fewer clicks to file a 1099)
