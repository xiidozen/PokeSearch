# PokeSearch

<!-- loc -->
![Lines of code](https://img.shields.io/badge/lines_of_code-4,685-blue)
<!-- /loc -->

An Android app for building and parsing [Pokémon GO](https://pokemongolive.com) search strings.

---

## Features

- **Visual query builder** — A nested group/filter tree that maps directly to Pokémon GO's search operators (`&`, `,`, `!`, `()`).
- **Tappable AND/OR separators** — The connector chip between every pair of items shows the current operator and switches it instantly when tapped.
- **Drag-to-reorder** — Long-press the ≡ handle on any filter or group to drag it to a new position within the same parent group.
- **Full syntax coverage** — Every currently documented search keyword: CP/HP/IV ranges, moves, types, generations, status flags, egg distances, buddy levels, regional forms, tags, and more.
- **Complex logic** — Build arbitrarily deep nested groups with AND / OR operators and NOT toggles on individual filters *or* entire groups (e.g. `((1*&4*),(!(3*)&shiny))`).
- **Parse existing strings** — Paste any Pokémon GO search string and the app reconstructs the full filter tree so you can view and modify it.
- **One-tap copy** — A dedicated Copy button puts the final search string straight on the clipboard, ready to paste into the game.

---

## Screenshots

> *(Run the app and take screenshots here)*

---

## Building

### Requirements

| Tool | Version |
|------|---------|
| Android SDK | API 29+ (Android 10) |
| Java | 17 |
| Gradle | 8.7 (via wrapper) |
| Android Gradle Plugin | 8.5.0 |
| Kotlin | 2.1.20 |

### Clone and build

```bash
git clone <repo-url>
cd PokeSearch
./gradlew assembleRelease
```

The signed APK will be at:
```
app/build/outputs/apk/release/PokeSearch-<version>.apk
```

### Run unit tests

```bash
./gradlew test
```

---

## Architecture

```
app/src/main/java/com/pokesearch/
├── MainActivity.kt                     Entry point
├── model/
│   ├── FilterDef.kt                    Data types: FilterDef, FilterValue, LogicOperator
│   ├── FilterDefs.kt                   Catalogue of all supported Pokémon GO search filters
│   └── QueryNode.kt                    Immutable query tree (sealed classes + tree ops)
├── parser/
│   ├── SearchParser.kt                 String → QueryNode tree (recursive descent)
│   └── SearchSerializer.kt            QueryNode tree → String (+ UI summaries)
├── viewmodel/
│   └── MainViewModel.kt                Holds tree state; dispatches mutations
└── ui/
    ├── MainScreen.kt                   Root Compose screen
    ├── theme/                          Material 3 theme (Pokémon-inspired colours)
    └── components/
        ├── QueryTreeView.kt            Recursive tree renderer
        └── FilterEditorSheet.kt        Bottom sheet for adding/editing filters
```

### Query tree model

The query is stored as an immutable tree of `QueryNode`:

```
GroupNode(OR)
  ├── FilterNode(shiny)
  └── GroupNode(AND, negated=true)
        ├── FilterNode(cp-1500)
        └── FilterNode(4*)
```

This tree serialises to:
```
shiny,!(cp-1500&4*)
```

All tree mutations (add, remove, update, toggle negation, change operator) produce new tree copies — the `ViewModel` holds the root in a `StateFlow` and recomputes the search string on every change.

---

## Pokémon GO Search Syntax

See [`SEARCH_SYNTAX.md`](SEARCH_SYNTAX.md) for the full reference table of every supported search keyword.

---

## Signing

The APK is signed with the `debug.keystore` (alias `androiddebugkey`, store/key password `android`). This is sufficient for personal use and sideloading but should be replaced with a production keystore before publishing to the Play Store.

---

## Versioning

Version bumps follow [Semantic Versioning](https://semver.org):

| File | Field |
|------|-------|
| `app/build.gradle.kts` | `versionName` (human-readable, e.g. `1.0.0`) |
| `app/build.gradle.kts` | `versionCode` (integer, monotonically increasing) |

## Changelog

### v1.2.0
- Light/dark/system theme toggle in the top bar (both Android app and web PWA)
- Expanded filter coverage: Pokémon size (xxs/xs/xl/xxl), mega level (mega0–mega3), weak-to / strong-against type, move type filters (@1type, @2type), encounter origin (raid, research, GBL, rocket, snapshot, etc.), distance, eggs-only, baby, background, and more
- Generation name aliases (kanto, johto, hoenn, etc.) now parse as the corresponding gen filter
- Web PWA (index.html) updated with all new filters and theme persistence via localStorage

### v1.1.0
- Tappable AND/OR separator chip between sibling items — tap once to switch the group's logic operator
- Drag-to-reorder: long-press the ≡ handle to drag filters/groups to a new position within the same group
- 20 new unit tests covering reorder logic, `calculateTargetIndex`, and `LogicOperator.opposite()`

### v1.0.0
- Initial release

---

## License

This project is for personal use. Pokémon GO is a trademark of Niantic, Inc. / The Pokémon Company.
