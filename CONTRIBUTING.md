# Contributing to RetroNest

Thanks for wanting to help improve RetroNest.

This project is designed to be:
- offline-first
- accessibility-first
- maintainable
- privacy-focused
- secure
- long-lived

Please read this before contributing.

---

## 1. Contribution principles

### Keep the app offline-first
Do not introduce cloud dependencies into core features.

### Keep accessibility strong
Any UI change must consider:
- autistic-friendly design
- colorblind safety
- clear labels
- low motion
- predictable layout

### Keep the architecture clean
- no business logic in composables
- no transport logic in domain
- no direct Android dependencies in pure domain logic

### Keep dependencies minimal
Only add dependencies when they are justified.

---

## 2. Branching and workflow

1. Create a branch from `main`
2. Make a small focused change
3. Add or update tests
4. Run lint and tests
5. Open a pull request
6. Address review feedback
7. Merge only when checks pass

---

## 3. Code style

- Use Kotlin idioms
- Prefer readable code over clever code
- Keep functions focused
- Name things clearly
- Avoid giant classes
- Split feature modules when needed

---

## 4. Testing expectations

Every meaningful change should include tests.

### Minimum expectation
- unit tests for business logic
- UI tests for key flows when relevant
- migration tests when schema changes
- accessibility verification when UI changes

### Good examples to test
- reward calculation
- ledger balances
- task approval
- sync retries
- internet gating
- calendar conversion

---

## 5. Accessibility expectations

If you change the UI, ensure:
- labels are present
- icons do not replace text
- colors are not the only signal
- tap targets are easy
- navigation remains predictable
- contrast remains sufficient

---

## 6. Pull request checklist

Before submitting a PR:
- [ ] I kept the code offline-first
- [ ] I added or updated tests
- [ ] I ran lint
- [ ] I verified accessibility impact
- [ ] I did not add unnecessary dependencies
- [ ] I kept business logic out of UI
- [ ] I updated documentation if needed

---

## 7. Reporting issues

When reporting a bug, please include:
- Android version
- device model
- app flavor used
- steps to reproduce
- expected behavior
- actual behavior
- logs if available

---

## 8. Feature requests

Feature requests are welcome, but please consider:
- Does it need internet?
- Can it work offline?
- Can it be done with a simple event model?
- Is it accessible?
- Is it maintainable?

---

## 9. Maintainer notes

The long-term health of this codebase matters more than short-term speed.

If a change improves maintainability, readability, or testability, that is a strong positive.

If a change makes the app more fragile or harder to reason about, reconsider it.

---

## 10. Thank you

Thanks for helping build a private, calm, accessible family app that can last for years.