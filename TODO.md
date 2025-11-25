# TODO — orbital-railgun-enhanced

## High priority
- [ ] Consolidate version branches into a single main branch  
  - impl note: prefer a single codebase with small runtime adapters or Gradle sourceSets for unavoidable version-specific differences.
- [ ] Add and verify compatibility for Minecraft 1.20.5  
  - impl note: update mappings/build target and run smoke tests for core behavior.
- [ ] Add and verify support for Minecraft 1.21.x  
  - impl note: update Loom/mappings and add minimal compatibility adapters for API differences.
- [ ] Remove dead/unused code, assets, and commented-out legacy files
- [x] Add a GitHub Actions build matrix for supported versions
  - ✅ Build workflow updated to support multi-version Minecraft branches (1.20.1, 1.20.2, 1.20.4, 1.20.6)
- [ ] Upgrade build tooling (Gradle, Loom, mappings) and lock dependency versions

## Medium priority
- [ ] Add unit tests for non-API logic (utils, config parsing, math)
- [ ] Add integration/smoke tests (headless or lightweight server runs)  
  - impl note: prioritize tests for firing, projectile lifecycle, and state/serialization.
- [x] Add CHANGELOG.md and update README with supported versions and installation notes
  - ✅ README updated with supported versions table and detailed installation notes
  - ⏳ CHANGELOG.md pending (can be auto-generated from releases)
- [x] Add CONTRIBUTING.md and a code style/linter configuration
  - ✅ CONTRIBUTING.md added with development setup, coding guidelines, and contribution instructions
  - ⏳ Code style/linter configuration pending
- [ ] Improve mod metadata (fabric.mod.json / mods.toml): supported versions, dependencies, and compatibility notes
- [ ] Add tests for saves/persistence, networking correctness, and serialization

## Low priority
- [ ] Profile and optimize performance (projectile tick cost, collision checks, allocations)  
  - impl note: run focused profiles on servers with many entities and projectiles.
- [ ] Add configurable balancing options (damage, range, charge mechanics)
- [ ] Improve assets: textures, particles, sounds
- [x] Add localization (i18n) support
  - ✅ 15 languages supported: English, Arabic, German, Spanish, French, Hindi, Italian, Japanese, Korean, Dutch, Polish, Portuguese, Russian, Swedish, Chinese
- [ ] Improve error handling and user-facing messages

## Nice-to-have
- [ ] Modularize into core + optional version adapters or modules
- [ ] Provide example server configs and migration scripts for upgrades
- [x] Automated release pipeline producing artifacts/tags per supported MC version
  - ✅ GitHub Actions workflow automatically builds and releases for each supported MC version
- [ ] Automated compatibility testing against popular mods or modpacks
- [ ] Expose a small API/hooks for other mods to interact with the railgun (events)
- [ ] Security/dependency audit and periodic dependency updates
- [ ] Add documentation for maintainers: architecture overview, module map, and common code patterns
- [ ] Add large-scale stress tests (many simultaneous projectiles/entities)
- [ ] Add accessibility and UX improvements (HUD charge indicators, clear error messages)

## Maintenance & housekeeping
- [x] Keep a single-source list of supported MC versions (in README + mod metadata)
  - ✅ README now includes a table of supported Minecraft versions
- [ ] Maintain a lightweight checklist for each release (build targets, changelog entry, compatibility notes)
- [x] Tag releases and maintain semantic versioning
  - ✅ Automated releases create version tags (e.g., v1.3.4-1.20.1)
- [ ] Periodically run static analysis and remove new dead/unused code

## Optional experiments
- [ ] Explore runtime feature toggles to disable expensive effects on low-end servers
- [ ] Investigate using a small compatibility library to reduce reflection boilerplate across MC versions
- [ ] Consider a small telemetry/stats mode (opt-in) to capture performance hotspots in the wild
