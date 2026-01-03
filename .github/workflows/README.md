# Workflows

This directory contains GitHub Actions workflows for the Orbital Railgun Enhanced mod.

## CodeQL Analysis Workflow

The `codeql-analysis.yml` workflow performs automated security and code quality scanning using GitHub CodeQL.

### How It Works

- **Triggers:**
  - On every push to main/master branches
  - On every pull request to main/master branches
  - Weekly on Sundays at 1:30 AM UTC (scheduled scan)

- **Analysis Process:**
  1. Sets up JDK 21 (required by the project)
  2. Initializes CodeQL with manual build mode for Java
  3. Compiles the Java source code with Gradle to resolve required dependencies
  4. Performs CodeQL analysis with full type information
  5. Uploads results to GitHub Security tab

- **Why Manual Build Mode?**
  - The project uses Fabric Loom with complex dependencies (GeckoLib, owo-lib, Satin)
  - Manual build mode ensures all dependencies are resolved before analysis
  - This achieves >85% call target resolution and >85% type information accuracy
  - Without building, CodeQL defaults to "none" mode with lower quality metrics

## Build and Release Workflow

The `build-and-release.yml` workflow automatically builds and releases the Orbital Railgun mod for all supported Minecraft versions when the mod version changes.

## How It Works

The workflow consists of two jobs:

### 1. Check Version

- Reads the `mod_version` from `gradle.properties`
- Scans the `versions/` directory for all supported Minecraft versions
- For each version, checks if a release tag already exists
- Creates a build matrix for all versions that need to be built

### 2. Build and Release (matrix build)

- Runs in parallel for each Minecraft version that needs building
- Sets up Java 21 and Gradle
- Builds the mod for the specific Minecraft version using Gradle subprojects
- Creates a GitHub release with a version tag (e.g., `v1.3.4-1.20.1`)
- Uploads the built JAR file as a release asset

## Triggering the Workflow

The workflow runs automatically when:

- Code is pushed to the `main` or `master` branch
- Changes are made to:
  - `gradle.properties` (version changes)
  - `versions/**` (version-specific configuration changes)
  - `src/**` (source code changes)
  - `build.gradle` (build configuration changes)
  - `settings.gradle` (project structure changes)
  - `.github/workflows/build-and-release.yml` (workflow changes)

You can also manually trigger the workflow from the Actions tab in GitHub.

## How to Release a New Version

1. Update the `mod_version` in `gradle.properties`:

   ```properties
   mod_version=1.3.5
   ```

2. Commit and push your changes to the main/master branch
3. The workflow will automatically:
   - Build the mod for all supported Minecraft versions (1.20.1, 1.20.2, 1.20.4)
   - Create releases tagged as `v1.3.5-1.20.1`, `v1.3.5-1.20.2`, `v1.3.5-1.20.4`
   - Upload the JAR files for each version

## Adding Support for a New Minecraft Version

1. Create a new directory under `versions/` (e.g., `versions/1.21.1/`)
2. Add `gradle.properties` with version-specific dependency versions
3. Add `build.gradle` (copy from an existing version and adjust if needed)
4. The workflow will automatically detect and include the new version

## Important Notes

- The workflow will **not** create a release if a tag with that version already exists
- This prevents duplicate releases and unnecessary builds
- If you need to rebuild an existing version, you must delete the existing release and tag first
- The release tag format is `v{mod_version}-{minecraft_version}` (e.g., `v1.3.4-1.20.1`)
- The JAR file is named using the pattern: `{archives_base_name}-{minecraft_version}-{mod_version}.jar`
- Builds are run in parallel for all versions that need releasing
