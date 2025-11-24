# Copilot Instructions for Orbital Railgun Enhanced

## Repository Overview

**Project Type:** Minecraft Fabric Mod (Java)
**Description:** Orbital Railgun Enhanced is a Minecraft mod that adds an orbital strike weapon with sound effects, visual shaders, and enhanced gameplay features. This is a fork of the original Orbital Railgun mod by Mishkis with added sounds and useful features.

**Size:** Small to medium (~57 files)
**Languages:** Java 21, GLSL (shaders), JSON (resources)
**Framework:** Fabric Loader with Fabric API
**Target Minecraft Version:** 1.20.1

### Key Dependencies
- Fabric Loader: 0.18.1
- Fabric API: 0.92.6+1.20.1
- GeckoLib (animation library): 4.8.2
- owo-lib (configuration/UI library): 0.11.2+1.20
- Satin (shader library): 1.14.0

## Build Configuration

### Prerequisites
- **Java 21** (Required - project uses Java 21 features)
- **Gradle 9.2.0+** (system installation required - no wrapper scripts exist)
- **Internet access** to Maven Central, Fabric Maven, and external repositories

### Environment Setup

**IMPORTANT:** Always use Java 21. The project will NOT compile with Java 17 or earlier:
```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64  # or your Java 21 installation
export PATH=$JAVA_HOME/bin:$PATH
java -version  # Verify Java 21 is active
```

### Build Commands

**Note:** This project does NOT have gradlew wrapper scripts. Always use the system `gradle` command.

#### Clean Build (Recommended first step)
```bash
gradle clean
```

#### Full Build
```bash
gradle build --no-daemon
```
- Use `--no-daemon` to avoid Gradle daemon issues in CI environments
- Build time: ~60-120 seconds on first run (downloading dependencies)
- Output JAR: `build/libs/orbital_railgun_enhanced-<version>.jar`
- Sources JAR: `build/libs/orbital_railgun_enhanced-<version>-sources.jar`

#### Common Build Issues

**Issue 1: Network Download Failures**
If you encounter `net.fabricmc.loom.util.download.DownloadException: Failed to download` errors:
- This happens when Minecraft asset downloads from `piston-data.mojang.com` are blocked
- The build requires downloading Minecraft client.jar and server.jar
- **Workaround:** Ensure network access or use a pre-cached Gradle Loom directory
- If cache is corrupted: `rm -rf ~/.gradle/caches/fabric-loom`

**Issue 2: Cache Lock Issues**
Error: "Previous process has disowned the lock due to abrupt termination"
- **Solution:** Delete fabric-loom cache: `rm -rf ~/.gradle/caches/fabric-loom`
- Then retry the build

### Testing

**No automated tests exist in this repository.** Manual testing is required:
1. Build the mod: `gradle build --no-daemon`
2. Place the JAR from `build/libs/` into a Minecraft 1.20.1 instance with Fabric Loader
3. Run Minecraft and test in-game functionality

### Linting

No linting tools are configured. Follow Java code style conventions:
- Use 4 spaces for indentation
- Follow existing code patterns in the repository

## Project Structure

### Source Layout

The mod uses Fabric's split source sets:

**Main Source Set** (`src/main/`):
- `java/io/github/kingironman2011/orbital_railgun_enhanced/` - Server-side and shared code
  - `OrbitalRailgun.java` - Main mod initializer (ModInitializer entrypoint)
  - `config/ServerConfig.java` - Server-side configuration
  - `item/OrbitalRailgunItem.java` - Item implementation
  - `item/OrbitalRailgunItems.java` - Item registry
  - `registry/SoundsRegistry.java` - Sound event registry
  - `registry/CommandRegistry.java` - Command registration (/ore, /orbitalrailgun)
  - `util/OrbitalRailgunStrikeManager.java` - Strike effect management
  - `listener/PlayerAreaListener.java` - Player area detection for sounds
  - `logger/SoundLogger.java` - Sound debugging logger
- `resources/` - Shared resources
  - `fabric.mod.json` - Mod metadata and entrypoints
  - `orbital_railgun_enhanced.mixins.json` - Server mixins (currently empty)
  - `assets/orbital_railgun_enhanced/lang/` - Language files (15 languages)
  - `data/orbital_railgun_enhanced/` - Data packs (recipes, damage types)

**Client Source Set** (`src/client/`):
- `java/io/github/kingironman2011/orbital_railgun_enhanced/client/` - Client-only code
  - `OrbitalRailgunClient.java` - Client initializer
  - `config/EnhancedConfig.java` - Client-side configuration (using owo-lib)
  - `rendering/OrbitalRailgunShader.java` - Shader management
  - `rendering/OrbitalRailgunGuiShader.java` - GUI shader effects
  - `item/OrbitalRailgunRenderer.java` - GeckoLib item renderer
  - `handler/SoundsHandler.java` - Client sound handling
  - `mixin/` - Client-side mixins (MouseMixin, MinecraftClientMixin, AbstractClientPlayerEntity)
- `resources/` - Client-only resources
  - `orbital_railgun_enhanced.client.mixins.json` - Client mixin configuration
  - `assets/orbital_railgun_enhanced/shaders/` - GLSL shader files
  - `assets/orbital_railgun_enhanced/geo/` - GeckoLib geometry models
  - `assets/orbital_railgun_enhanced/textures/` - Textures
  - `assets/orbital_railgun_enhanced/models/` - Item models

### Configuration Files

- `build.gradle` - Gradle build configuration
- `gradle.properties` - Project properties (version, Minecraft version, dependencies)
- `settings.gradle` - Gradle settings (plugin repositories)
- `.gitignore` - Git ignore patterns (includes build/, .gradle/, run/, runs/)

### Key Constants

**Mod ID:** `orbital_railgun_enhanced`
**Maven Group:** `io.github.kingironman2011`
**Archives Base Name:** `orbital_railgun_enhanced`
**Current Version:** Check `gradle.properties` `mod_version` property

### Network Packets

The mod uses custom network packets (defined in `OrbitalRailgun.java`):
- `orbital_railgun_enhanced:play_sound` - Play sound to nearby players
- `orbital_railgun_enhanced:stop_area_sound` - Stop area-based sounds
- `orbital_railgun_enhanced:shoot_packet` - Trigger orbital strike
- `orbital_railgun_enhanced:client_sync_packet` - Sync strike to clients

## GitHub Actions CI/CD

### Workflow: `build-and-release.yml`

**Triggers:**
- Push to main/master branches
- Changes to: `gradle.properties`, `src/**`, `build.gradle`, workflow file
- Manual workflow_dispatch

**Build Process:**
1. Checks mod version from `gradle.properties` (`mod_version=`)
2. Verifies if git tag `v<version>` already exists (skips if exists)
3. Sets up JDK 21 (Temurin distribution)
4. Runs `gradle build --no-daemon` (or `./gradlew` if present)
5. Creates GitHub release with tag `v<version>`
6. Uploads built JAR as release asset

**To trigger a new release:** Increment `mod_version` in `gradle.properties` and push to main/master.

## Translation Guidelines

**Project Policy:** Use machine translations for efficiency. This is a Minecraft mod where:
- The community often contributes improved translations after release
- Quick iteration is prioritized over perfect initial translations

Use automated translation tools (Google Translate, DeepL, etc.) for new translation strings as a starting point. Community contributions will refine translations over time.

## Development Tips

### Making Code Changes

1. **Source organization:** Server code in `src/main/`, client code in `src/client/`
2. **Mixins:** Use appropriate mixin JSON file (client vs server)
3. **Configuration:** Server config in `ServerConfig.java`, client in `EnhancedConfig.java` (uses owo-lib)
4. **Sound events:** Register in `SoundsRegistry.java`, implement handlers in client code
5. **Commands:** Add to `CommandRegistry.java` (uses Fabric's command API)

### Resource Modifications

- **Language files:** JSON in `src/main/resources/assets/orbital_railgun_enhanced/lang/`
- **Shaders:** GLSL files in `src/client/resources/assets/orbital_railgun_enhanced/shaders/`
- **Models/Textures:** In `src/client/resources/assets/orbital_railgun_enhanced/`
- **Data packs:** In `src/main/resources/data/orbital_railgun_enhanced/`

### Version Management

Mod version is in `gradle.properties`:
```properties
mod_version=1.3.0
```
This is automatically substituted into `fabric.mod.json` during build.

### Common Modification Patterns

**Adding a new sound:**
1. Add `.ogg` file to `src/main/resources/assets/orbital_railgun_enhanced/sounds/`
2. Register in `SoundsRegistry.java`
3. Add localization to `lang/en_us.json` (subtitle key)
4. Implement playback in appropriate handler

**Adding a new item:**
1. Create item class in `item/` package
2. Register in `OrbitalRailgunItems.java`
3. Add model JSON to `resources/assets/.../models/item/`
4. Add texture to `resources/assets/.../textures/item/`
5. Add localization keys

**Modifying strike behavior:**
- Edit `OrbitalRailgunItem.java` (item usage)
- Edit `OrbitalRailgunStrikeManager.java` (strike effects/timing)
- Adjust `ServerConfig.java` for configuration options

## Files in Repository Root

```
.git/                    - Git repository data
.github/                 - GitHub configuration (workflows, this file)
.gitignore              - Git ignore patterns
LICENSE.txt             - MIT License
README.md               - Project documentation
build.gradle            - Gradle build script
gradle/                 - Gradle wrapper (only properties, no scripts)
gradle.properties       - Project version and dependencies
settings.gradle         - Gradle settings
src/                    - Source code (main/ and client/)
```

## Trust These Instructions

**These instructions are comprehensive and validated.** Only search for additional information if:
- You encounter an error not documented here
- You need to understand specific code logic
- These instructions are incomplete for your specific task

For build issues, ALWAYS check:
1. Java version is 21
2. Network connectivity for Maven repositories
3. Gradle cache is not corrupted

For code changes:
1. Respect the split source sets (main vs client)
2. Follow existing patterns in similar files
3. Test with an actual Minecraft instance when possible
