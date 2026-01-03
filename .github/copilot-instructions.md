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

- **Java 21** (Required - project uses Java 21 toolchain for compilation)
  - Note: The build.gradle is configured to compile to Java 17 bytecode but requires Java 21 toolchain
- **Gradle 9.2.0+** (Gradle wrapper scripts are included: `gradlew` and `gradlew.bat`)
- **Internet access** to Maven Central, Fabric Maven, and external repositories

### Environment Setup

**IMPORTANT:** Always use Java 21. The project will NOT compile with Java 17 or earlier:

```bash
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64  # or your Java 21 installation
export PATH=$JAVA_HOME/bin:$PATH
java -version  # Verify Java 21 is active
```

### Build Commands

**Note:** This project uses Gradle wrapper scripts (`gradlew` on Linux/Mac, `gradlew.bat` on Windows).

#### Multi-Module Project Structure

The project uses a multi-module structure with version-specific subprojects under `versions/`:
- `versions/1.20.4/` - Minecraft 1.20.1-1.20.4 compatible build (using 1.20.1 mappings)

#### Clean Build (Recommended first step)

```bash
./gradlew clean
```

#### Build All Versions

```bash
./gradlew build --no-daemon
```

This builds all version subprojects and copies JARs to `build/libs/` in the root directory.

#### Build Specific Version

```bash
./gradlew :versions:1.20.4:build --no-daemon
```

- Use `--no-daemon` to avoid Gradle daemon issues in CI environments
- Build time: ~60-120 seconds on first run (downloading dependencies)
- Output JAR: `versions/<mc_version>/build/libs/orbital_railgun_enhanced-<version>-<mc_version>.jar`
- Sources JAR: `versions/<mc_version>/build/libs/orbital_railgun_enhanced-<version>-<mc_version>-sources.jar`
- Root aggregation: JARs are also copied to `build/libs/` at project root

#### Linting with Checkstyle

```bash
./gradlew checkstyleMain checkstyleClient
```

The project uses Checkstyle 10.12.5 with configuration in `config/checkstyle/checkstyle.xml`.

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

**No automated unit tests exist in this repository.** However, test infrastructure is configured with JUnit Jupiter. Manual testing is required:

1. Build the mod: `./gradlew :versions:1.20.4:build --no-daemon`
2. Place the JAR from `versions/1.20.4/build/libs/` into a Minecraft 1.20.1-1.20.4 instance with Fabric Loader
3. Run Minecraft and test in-game functionality

To run configured tests (if added in the future):
```bash
./gradlew test
```

### Linting

The project uses Checkstyle for code style enforcement. Configuration is in `config/checkstyle/checkstyle.xml`.

Run checkstyle:
```bash
./gradlew checkstyleMain checkstyleClient
```

Checkstyle is configured to:
- Use version 10.12.5
- Not fail the build on violations (`ignoreFailures = true`)
- Show violations in console
- Generate XML and HTML reports

Java code style conventions:
- Use 4 spaces for indentation (configured via Checkstyle)
- Follow existing code patterns in the repository

## Project Structure

### Multi-Module Layout

The project uses Gradle multi-module architecture:

**Root Project** (`build.gradle`):
- Aggregates all version subprojects
- `buildAll` task builds all versions
- `copyJars` task copies built JARs to root `build/libs/`
- Does not contain source code itself

**Version Subprojects** (`versions/<mc_version>/`):
- Each subproject is a complete Fabric mod build for a specific Minecraft version
- Currently: `versions/1.20.4/` (supports MC 1.20.1-1.20.4)
- Each has its own `build.gradle` and `gradle.properties`
- All reference shared source code from root `src/` directory

**Shared Source Code** (Root `src/` directory):
- `src/main/` - Server-side and shared code
- `src/client/` - Client-only code
- Version subprojects configure their source sets to reference these directories

### Source Layout

The mod uses Fabric's split source sets (configured in version subprojects):

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

**Root Level:**
- `build.gradle` - Root project build script (aggregates subprojects)
- `gradle.properties` - Shared properties (mod version, maven group, archives name)
- `settings.gradle` - Gradle settings (plugin repositories, subproject includes)
- `.gitignore` - Git ignore patterns (includes build/, .gradle/, run/, runs/)
- `gradlew`, `gradlew.bat` - Gradle wrapper scripts
- `gradle/wrapper/` - Gradle wrapper JAR and properties

**Version Subprojects** (`versions/<mc_version>/`):
- `build.gradle` - Version-specific build configuration (dependencies, Fabric Loom setup)
- `gradle.properties` - Version-specific properties (Minecraft version, dependency versions)

**Checkstyle:**
- `config/checkstyle/checkstyle.xml` - Checkstyle configuration for code style enforcement

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
- Changes to: `gradle.properties`, `versions/**`, `src/**`, `build.gradle`, `settings.gradle`, workflow file
- Manual workflow_dispatch

**Build Process:**

1. **Check Version Job:**
   - Checks mod version from root `gradle.properties` (`mod_version=`)
   - Scans all version subprojects in `versions/` directory
   - For each version, checks if git tag `v<mod_version>-<mc_version>` exists
   - Creates build matrix with only versions that don't have existing tags
   - If all versions already have tags, skips the build job

2. **Build and Release Job (Matrix Strategy):**
   - Runs in parallel for each Minecraft version that needs building
   - Sets up JDK 21 (Temurin distribution)
   - Runs `./gradlew :versions:<mc_version>:build --no-daemon`
   - Finds the built JAR in `versions/<mc_version>/build/libs/`
   - Determines supported Minecraft version range (e.g., 1.20.4 build supports 1.20-1.20.4)
   - Creates GitHub release with tag `v<mod_version>-<mc_version>`
   - Uploads built JAR as release asset

**To trigger a new release:** Increment `mod_version` in root `gradle.properties` and push to main/master. The workflow will automatically build and release all version subprojects.

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

Mod version is in root `gradle.properties`:

```properties
mod_version=1.3.6
```

Each version subproject has its own Minecraft version in `versions/<mc_version>/gradle.properties`:

```properties
minecraft_version=1.20.1
show_minecraft_version=1.20.4
```

The mod version is automatically substituted into `fabric.mod.json` during build.

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
.github/                 - GitHub configuration (workflows, copilot-instructions.md)
.gitignore              - Git ignore patterns
LICENSE                 - MIT License
README.md               - Project documentation
CHANGELOG.md            - Version history
CONTRIBUTING.md         - Contributing guidelines
TODO.md                 - Project roadmap
build.gradle            - Root Gradle build script (aggregator)
gradle/                 - Gradle wrapper files
gradlew                 - Gradle wrapper script (Linux/Mac)
gradlew.bat             - Gradle wrapper script (Windows)
gradle.properties       - Root project properties (mod version, maven group)
settings.gradle         - Gradle settings (subproject includes)
config/                 - Configuration files (Checkstyle)
src/                    - Shared source code (main/ and client/)
versions/               - Version-specific subprojects (e.g., versions/1.20.4/)
```

## Trust These Instructions

**These instructions are comprehensive and validated.** Only search for additional information if:

- You encounter an error not documented here
- You need to understand specific code logic
- These instructions are incomplete for your specific task

For build issues, ALWAYS check:

1. Java version is 21 (Java 21 toolchain is required even though target is Java 17)
2. Network connectivity for Maven repositories
3. Gradle cache is not corrupted
4. Using the correct Gradle wrapper script (`./gradlew` not `gradle`)

For code changes:

1. Respect the split source sets (main vs client) in shared `src/` directory
2. Follow existing patterns in similar files
3. Test with an actual Minecraft instance when possible
4. When adding support for new Minecraft versions, create a new subproject in `versions/`
