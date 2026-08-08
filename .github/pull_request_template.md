## Description

Please include a summary of the change and which issue it fixes.

Fixes #(issue)

## Type of change

- [ ] Bug fix (non-breaking change which fixes an issue)
- [ ] New feature (non-breaking change which adds functionality)
- [ ] Breaking change (fix or feature that would break existing functionality)
- [ ] Documentation update

## Checklist

### Core principles
- [ ] Kept the code offline-first (no cloud requirement added)
- [ ] No Google Play Services dependency added
- [ ] No third-party analytics or tracking added
- [ ] Data stays encrypted at rest (SQLCipher)
- [ ] Screenshot blocking (FLAG_SECURE) preserved

### Accessibility
- [ ] Colors are not the only signal (labels/icons added)
- [ ] Large enough touch targets
- [ ] Semantic labels / content descriptions present
- [ ] Theme tokens used instead of hardcoded colors
- [ ] Calm / reduced-motion modes considered

### Code quality
- [ ] Business logic kept out of composables
- [ ] No pure-Kotlin domain code depends on Android classes
- [ ] Tests added/updated for business logic where applicable
- [ ] Dependencies justified (no unnecessary additions)
- [ ] Documentation updated if needed

### Sync
- [ ] Sync events versioned and signed
- [ ] Deduplication preserved (unique event IDs)
- [ ] Local database remains source of truth

## How has this been tested?

Please describe the tests you ran:
- [ ] Unit tests
- [ ] Instrumented tests
- [ ] Manual testing on device
- [ ] Test device/Android version:
- [ ] App role tested: Parent / Child (or both)

## Screenshots (if applicable)

## Additional context

**Privacy note:** Do NOT include real names, addresses, PINs, or other personal data in this PR.