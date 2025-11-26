# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Added CHANGELOG.md for tracking changes
- Added unit tests for config parsing, utils, and math
- Added integration/smoke tests for server runs
- Added code style configuration (Checkstyle)
- Improved mod metadata in fabric.mod.json

## [1.3.4] - 2025-11-26

### Added
- GitHub Actions build and release workflow
- CONTRIBUTING.md with contribution guidelines
- TODO.md for tracking project tasks
- Copilot instructions for development assistance
- FUNDING.yml for sponsorship options

### Changed
- Updated README.md with comprehensive documentation

## [1.3.3] - 2025-11-25

### Added
- Localization support for 15 languages (ar_sa, de_de, en_us, es_es, fr_fr, hi_in, it_it, ja_jp, ko_kr, nl_nl, pl_pl, pt_br, ru_ru, sv_se, zh_cn)
- Server-side configuration system (ServerConfig.java)
- Debug mode for troubleshooting
- Sound logging system
- Player area listener for spatial sound handling
- Command registry with /ore and /orbitalrailgun commands

### Changed
- Improved sound handling with spatial awareness
- Enhanced strike manager with configurable damage and cooldown
- Better network packet handling for sounds

## [1.3.2] - 2025-11-20

### Added
- GeckoLib integration for item animations
- Satin shader support for visual effects
- owo-lib for configuration UI

### Changed
- Updated to Fabric Loader 0.18.1
- Updated to Fabric API 0.92.6+1.20.1

### Fixed
- Fixed shader rendering issues on some graphics cards

## [1.3.1] - 2025-11-15

### Added
- Orbital strike visual effects with custom shaders
- Chromatic aberration shader effect during strikes
- GUI shader for scope overlay

### Fixed
- Fixed strike damage calculation when entities are at the edge of impact radius

## [1.3.0] - 2025-11-10

### Added
- Initial enhanced version fork from original Orbital Railgun by Mishkis
- Custom sound effects for railgun (equip, shoot, scope)
- Client-side configuration via owo-lib
- Player area detection for sound range management

### Changed
- Migrated to split source sets (main/client)
- Improved entity damage handling with configurable damage values

### Fixed
- Fixed item cooldown not being applied correctly
- Fixed sound playback issues when multiple players are in range

## [1.2.0] - 2025-11-01 (Original Mod)

### Added
- Basic orbital railgun item
- Strike damage system
- Block destruction in strike radius

## [1.1.0] - 2025-10-15 (Original Mod)

### Added
- Initial release with basic functionality
- Orbital strike mechanic

[Unreleased]: https://github.com/KingIronMan2011/orbital-railgun-enhanced/compare/v1.3.4...HEAD
[1.3.4]: https://github.com/KingIronMan2011/orbital-railgun-enhanced/compare/v1.3.3...v1.3.4
[1.3.3]: https://github.com/KingIronMan2011/orbital-railgun-enhanced/compare/v1.3.2...v1.3.3
[1.3.2]: https://github.com/KingIronMan2011/orbital-railgun-enhanced/compare/v1.3.1...v1.3.2
[1.3.1]: https://github.com/KingIronMan2011/orbital-railgun-enhanced/compare/v1.3.0...v1.3.1
[1.3.0]: https://github.com/KingIronMan2011/orbital-railgun-enhanced/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/KingIronMan2011/orbital-railgun-enhanced/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/KingIronMan2011/orbital-railgun-enhanced/releases/tag/v1.1.0
