# Build and Release Workflow

This workflow automatically builds and releases the Orbital Railgun mod when the version changes.

## How It Works

The workflow consists of two jobs:

### 1. Check Version

- Reads the `mod_version` from `gradle.properties`
- Checks if a release tag with that version already exists
- Skips the build if the version hasn't changed

### 2. Build and Release (only if version is new)

- Sets up Java 17 and Gradle
- Builds the mod using the Gradle build system
- Creates a GitHub release with a version tag (e.g., `v1.2`)
- Uploads the built JAR file as a release asset

## Triggering the Workflow

The workflow runs automatically when:

- Code is pushed to the `main` or `master` branch
- Changes are made to:
  - `gradle.properties` (version changes)
  - `src/**` (source code changes)
  - `build.gradle` (build configuration changes)
  - `.github/workflows/build-and-release.yml` (workflow changes)

You can also manually trigger the workflow from the Actions tab in GitHub.

## How to Release a New Version

1. Update the `mod_version` in `gradle.properties`:

   ```properties
   mod_version=1.3
   ```

2. Commit and push your changes to the main/master branch
3. The workflow will automatically:
   - Build the mod
   - Create a release tagged as `v1.3`
   - Upload the JAR file

## Important Notes

- The workflow will **not** create a release if a tag with the current version already exists
- This prevents duplicate releases and unnecessary builds
- If you need to rebuild an existing version, you must delete the existing release and tag first
- The release tag format is always `v{version}` (e.g., `v1.2`)
- The JAR file is named using the pattern: `{archives_base_name}-{version}.jar`
