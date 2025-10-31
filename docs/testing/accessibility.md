# Accessibility Audit – Reports Screen

## Checklist
- [x] **TalkBack labels**: Filter chips announce label and selected state.
- [x] **Chart semantics**: Each bar exposes month and earnings via `contentDescription`.
- [x] **Focus order**: Top-to-bottom order confirmed in TalkBack linear navigation.
- [x] **Colour contrast**: Primary/surface combinations meet WCAG AA (verified with Material Theme builder).
- [x] **Dynamic type**: UI scales without clipping at font scale 1.5x.
- [ ] **Switch control**: Requires follow-up to confirm horizontal chip row works with D-pad navigation.

## Notes
- Range chips use Material FilterChip providing built-in state announcements.
- Empty states include descriptive copy for chart and top students list.
- Remaining action item: schedule test on TV/D-pad device to validate chip focus behaviour.
