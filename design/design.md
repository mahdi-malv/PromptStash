# PromptStash — Design System

A note-app inspired design system for **PromptStash**, an Android + Desktop Kotlin Multiplatform app for stashing and reusing AI prompts. The aesthetic is **warm paper / quiet ink**: a Bear-like calm in light mode, a Things-3-like generosity of whitespace, and a single ink-teal accent that earns its visibility. A serif/sans pairing (STIX General serif for display + clean sans for chrome) is the signature twist.

---

## 1. Color Tokens

All tokens are designed to map cleanly to Material 3 slots. Light mode is a warm off-white "paper"; dark mode is a warm charcoal "ink".

### Light Mode (Warm Paper)

| Token              | Hex       | M3 Slot                  | Notes                              |
| ------------------ | --------- | ------------------------ | ---------------------------------- |
| background         | `#FAF7F1` | background               | Warm paper, slight amber bias      |
| surface            | `#FAF7F1` | surface                  | Same as background                 |
| surfaceContainer   | `#F3EEE4` | surfaceContainer         | Cards, inputs, settings rows       |
| surfaceContainerHi | `#EBE5D7` | surfaceContainerHigh     | Hovered / pressed states           |
| surfaceVariant     | `#E8E2D4` | surfaceVariant           | Chips, dividers backgrounds        |
| onSurface          | `#1F1B16` | onSurface                | Primary text (warm near-black)     |
| onSurfaceMuted     | `#6B645A` | onSurfaceVariant         | Secondary text, metadata           |
| outline            | `#C7BFAE` | outline                  | Dividers, borders                  |
| outlineMuted       | `#E1DACA` | outlineVariant           | Hairline separators                |
| accent             | `#0E6E6B` | primary                  | Ink-teal, used sparingly           |
| onAccent           | `#FFFFFF` | onPrimary                | Text on accent fill                |
| accentContainer    | `#C2EBE7` | primaryContainer         | Pinned highlight, chip fill        |
| onAccentContainer  | `#062523` | onPrimaryContainer       |                                    |
| danger             | `#A8362B` | error                    | Warm rust, not corporate red       |
| onDanger           | `#FFFFFF` | onError                  |                                    |
| dangerContainer    | `#F5D8D2` | errorContainer           |                                    |

### Dark Mode (Warm Ink)

| Token              | Hex       | M3 Slot                  | Notes                              |
| ------------------ | --------- | ------------------------ | ---------------------------------- |
| background         | `#1A1814` | background               | Warm charcoal, never pure black    |
| surface            | `#1A1814` | surface                  |                                    |
| surfaceContainer   | `#23201B` | surfaceContainer         | Cards, inputs                      |
| surfaceContainerHi | `#2D2922` | surfaceContainerHigh     |                                    |
| surfaceVariant     | `#33302A` | surfaceVariant           |                                    |
| onSurface          | `#EFE9DC` | onSurface                | Warm off-white                     |
| onSurfaceMuted     | `#A39C8E` | onSurfaceVariant         |                                    |
| outline            | `#544E43` | outline                  |                                    |
| outlineMuted       | `#33302A` | outlineVariant           |                                    |
| accent             | `#7BD3CF` | primary                  | Lifted ink-teal                    |
| onAccent           | `#062523` | onPrimary                |                                    |
| accentContainer    | `#0E4F4D` | primaryContainer         |                                    |
| onAccentContainer  | `#C2EBE7` | onPrimaryContainer       |                                    |
| danger             | `#F2B6AD` | error                    |                                    |
| onDanger           | `#5A1810` | onError                  |                                    |
| dangerContainer    | `#7A2218` | errorContainer           |                                    |

---

## 2. Typography

**Display / Headline / Title** — STIX General (serif). Already embedded in `shared/src/commonMain/composeResources/font/`. The serif carries the "literary" feel and matches the prompt-as-text content.

**Body / Label / UI chrome** — Inter (sans). Adopted for legibility at small sizes and to contrast the serif.

| Role            | Font            | Size  | Weight | Line-height |
| --------------- | --------------- | ----- | ------ | ----------- |
| displayLarge    | STIX Regular    | 40 sp | 400    | 48 sp       |
| headlineLarge   | STIX Regular    | 30 sp | 400    | 36 sp       |
| headlineMedium  | STIX Regular    | 26 sp | 400    | 32 sp       |
| titleLarge      | STIX Bold       | 22 sp | 700    | 28 sp       |
| titleMedium     | STIX Bold       | 18 sp | 700    | 24 sp       |
| bodyLarge       | Inter Regular   | 16 sp | 400    | 24 sp (1.5) |
| bodyMedium      | Inter Regular   | 14 sp | 400    | 22 sp       |
| bodySmall       | Inter Regular   | 12 sp | 400    | 18 sp       |
| labelLarge      | Inter Medium    | 14 sp | 500    | 20 sp       |
| labelMedium     | Inter Medium    | 12 sp | 500    | 16 sp       |
| labelSmall      | Inter Medium    | 11 sp | 500    | 14 sp       |

Prompt body content uses `bodyLarge` with `1.55` line-height for comfortable reading of long prompts.

---

## 3. Shapes (Corner Radius)

| Token       | Value | Used for                                 |
| ----------- | ----- | ---------------------------------------- |
| sm          | 8 dp  | Chips, small buttons                     |
| md          | 12 dp | Inputs, text fields, segmented controls  |
| lg          | 20 dp | Cards (default), settings rows           |
| xl          | 28 dp | FAB, floating pill nav, modal sheets     |
| pill        | 999   | Tags, sync chip, status indicators       |

**Signature detail:** **Pinned prompt cards** use **asymmetric radii** — `topStart=32, topEnd=20, bottomStart=20, bottomEnd=20`. The larger top-left corner subtly marks them as pinned without needing a heavy icon.

---

## 4. Spacing Scale

`4, 8, 12, 16, 24, 32, 48` dp. Default screen edge inset: `16 dp`. Default vertical rhythm between sections: `24 dp`.

List row min-height: `72 dp` (touch-friendly + breathable).

---

## 5. Elevation

Minimal — paper doesn't float. Cards rest on the background with a 1dp outline (`outlineMuted`) instead of a shadow. The FAB and floating pill nav are the only elevated surfaces, using a soft 8dp shadow at 12% opacity in light mode, and a 1dp accent-tinted outline in dark mode.

---

## 6. Component Primitives

### PromptCard (default)
- Surface: `surfaceContainer`
- Corner: `lg` (20 dp)
- Padding: `16 dp`
- Title: `titleMedium` (STIX Bold)
- Body preview: `bodyMedium`, max 2 lines, ellipsis, color `onSurfaceMuted`
- Tag chips row below body, gap `8 dp`

### PromptCard (pinned)
- Same as default, **but** asymmetric corner (`topStart=32`)
- Surface: `accentContainer` at 40% over `surfaceContainer`
- Tiny pin glyph top-right in `accent` color

### Tag chip
- Shape: `pill`
- Background: `surfaceVariant`
- Text: `labelMedium`, `onSurfaceMuted`
- Horizontal padding: `10 dp`, vertical: `4 dp`

### FloatingNavBar
- Shape: `xl` (28 dp) pill
- Surface: `surfaceContainerHigh` with 1dp `outlineMuted` border
- Items: icon + label, `labelMedium`
- Selected: pill fill in `accentContainer`, icon/text in `accent`
- 64 dp tall, 16 dp inset from screen edges

### FAB ("New Prompt")
- Shape: circle, 56 dp
- Surface: `accent`
- Icon: `+` in `onAccent`
- Subtle 8dp shadow (light) or 1dp accent-tinted ring (dark)

### Search field (Library top)
- Shape: `md` (12 dp)
- Surface: `surfaceContainer`
- Inline leading search icon, `bodyLarge` text
- Placeholder: "Search prompts..."

### Sync-status chip
- Shape: `pill`
- States: `Synced` (accent dot + label), `Syncing…` (spinner + label), `Offline` (muted dot + label)
- Background: transparent; just a dot + `labelSmall`

### SettingsRow
- Min-height: `72 dp`
- Leading icon (24 dp) in `onSurfaceMuted`
- Title `bodyLarge`, subtitle `bodySmall` in `onSurfaceMuted`
- Trailing: switch, chevron, or segmented control
- Divider below: `1 dp` `outlineMuted`

### Empty State (Library)
- Vertically centered
- Serif headline `headlineMedium`: "Your stash is empty"
- `bodyLarge` `onSurfaceMuted`: "Stash your first prompt to reuse it anywhere."
- Primary button: "New prompt" — pill, accent fill

---

## 7. Screen Layouts

### Library
- Top: title "PromptStash" in serif `headlineLarge`, right-aligned sync chip
- Search field below title (16dp gap)
- Section header "Pinned" in `titleMedium` (only if pinned items exist)
- Pinned cards in vertical list with asymmetric radius
- Section header "All prompts" in `titleMedium`
- Regular cards
- Floating pill nav bottom (Library / Settings)
- FAB bottom-right above nav

### Editor
- Top app bar: back arrow, "Edit prompt" / "New prompt" title in `titleLarge`, "Copy" icon action right
- Title field: borderless, large serif `headlineMedium` placeholder "Title"
- Body field: borderless, `bodyLarge`, multi-line, placeholder "Write the prompt…"
- Tag row: existing tag chips + a `+` chip
- Save FAB bottom-right with checkmark icon

### Settings
- Top app bar: back arrow, "Settings" title `titleLarge`
- Section "Appearance": theme selector as a 3-segment control (System / Light / Dark) — pill shape, accent fill on selected
- Section "Sync": current provider name with chevron, sync status chip inline
- Section "About": app version, device id (small, muted)
- Generous 32dp spacing between sections

### Empty Library
- See Empty State above. Centered illustration: a stylized line-drawing of an open book / paper stack in `outline` color.

### Widget (Glance, 4×2)
- Header: "PromptStash" serif `titleMedium`, with a small `+` action right
- 3 prompt rows: title + first 30 chars of body, tap-to-copy
- Surface: `surfaceContainer`
- Corners: `lg` (20 dp) outer
- Same color tokens as app; widget honors system light/dark

---

## 8. Motion (notes for later)

- Page transitions: 220ms standard easing, slight fade + 8dp translate
- Card press: 100ms scale-down to 0.98
- FAB: 250ms emphasized-decelerate when revealing
- Skip motion in dark mode reduced-motion settings

---

## 9. Iconography

Material Outlined icon set, 24dp at default size, color `onSurfaceMuted` unless inside an accent component. No icon should ever appear in raw accent color outside the FAB / pinned indicator / segmented-selected state.

---

## 10. Brand voice (microcopy)

- Empty state: "Your stash is empty."
- New prompt button: "New prompt"
- Save success toast: "Stashed."
- Copy success toast: "Copied to clipboard."
- Sync error: "Couldn't sync. We'll retry."

Quiet, warm, slightly literary — matches the serif typography.
