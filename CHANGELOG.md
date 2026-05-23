# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2026-05-23

### Changed
- **Desktop UX:** Migrated menu and dock interactions from touch-centric gestures (`awaitEachGesture`) to complete pointer event handling (`awaitPointerEventScope`). 
- **UI Elements:** Main menu and dock items now fully support native mouse hover states (scaling, highlighting, and animations) without requiring a click-and-hold action.
- **Swipe Actions:** Opening the AI training sub-menu on desktop is now triggered by a standard drag interaction rather than a touch-swipe.

### Fixed
- Resolved an issue where pointer exit and release states could become stuck or behave unpredictably when using a physical mouse instead of a touchscreen.

## [1.0.0] - 2026-05-20

### Added
- Initial release of La Riposte.
- 5x7 grid fencing logic with dynamic Touché point relocation.
- Tournament Mode featuring a curated roster of AI opponents.
- 12 fully custom, unmonetized visual and audio themes.
- Native builds for Android, Windows, and Linux.
