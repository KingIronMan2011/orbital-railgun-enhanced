# Contributing to Orbital Railgun Enhanced

Thank you for your interest in contributing to Orbital Railgun Enhanced! This document provides guidelines and information about how to contribute.

## Getting Started

### Prerequisites

- **Java 21** (Required - project uses Java 21 features)
- **Gradle 9.2.0+** 
- IDE of your choice (IntelliJ IDEA recommended for Minecraft mod development)

### Setting Up the Development Environment

1. **Clone the repository**:

   ```bash
   git clone https://github.com/KingIronMan2011/orbital-railgun-enhanced.git
   cd orbital-railgun-enhanced
   ```

2. **Set up Java 21**:

   ```bash
   export JAVA_HOME=/path/to/your/java21
   export PATH=$JAVA_HOME/bin:$PATH
   java -version  # Verify Java 21 is active
   ```

3. **Build the project**:

   ```bash
   ./gradlew build --no-daemon
   ```

4. **Set up IDE** (IntelliJ IDEA):
   - Open the project folder
   - Import as Gradle project
   - Let Gradle sync complete
   - Run `./gradlew genSources` to generate Minecraft sources

## How to Contribute

### Reporting Bugs

1. Check if the bug has already been reported in [Issues](https://github.com/KingIronMan2011/orbital-railgun-enhanced/issues)
2. If not, create a new issue with:
   - Clear and descriptive title
   - Steps to reproduce the bug
   - Expected behavior vs actual behavior
   - Minecraft version and mod version
   - Any relevant log files or crash reports

### Suggesting Features

1. Check if the feature has already been suggested in [Issues](https://github.com/KingIronMan2011/orbital-railgun-enhanced/issues)
2. Create a new issue with the "enhancement" label
3. Describe the feature and why it would be useful

### Submitting Code Changes

1. **Fork the repository** and create your branch from `main`
2. **Make your changes** following the coding guidelines below
3. **Test your changes** in-game
4. **Create a Pull Request** with:
   - Clear description of changes
   - Reference to any related issues
   - Screenshots/videos if applicable (especially for visual changes)

## Project Structure

The mod uses Fabric's split source sets:

- `src/main/` - Server-side and shared code
  - `java/` - Java source files
  - `resources/` - Shared resources (lang files, etc.)
- `src/client/` - Client-only code
  - `java/` - Client Java source files
  - `resources/` - Client resources (shaders, models, textures)

### Key Directories

| Directory | Purpose |
|-----------|---------|
| `src/main/java/.../orbital_railgun_enhanced/` | Core mod initialization, items, commands |
| `src/client/java/.../client/` | Client-side rendering, shaders, handlers |
| `src/main/resources/assets/` | Language files |
| `src/client/resources/assets/` | Textures, models, shaders |
| `src/main/resources/data/` | Recipes, damage types, data packs |

## Coding Guidelines

### General

- Use 4 spaces for indentation (no tabs)
- Follow existing code patterns in the repository
- Add comments for complex logic
- Keep methods focused and reasonably sized

### Java Code

- Follow standard Java naming conventions
- Use meaningful variable and method names
- Client-only code goes in `src/client/`
- Server/shared code goes in `src/main/`

### Resources

- Language files: JSON format in `src/main/resources/assets/orbital_railgun_enhanced/lang/`
- Shaders: GLSL files in `src/client/resources/assets/orbital_railgun_enhanced/shaders/`
- Models: JSON in `src/client/resources/assets/orbital_railgun_enhanced/models/`

## Translations

We welcome translation contributions! The mod currently supports multiple languages.

### Adding a New Language

1. Create a new JSON file in `src/main/resources/assets/orbital_railgun_enhanced/lang/`
2. Name it using the Minecraft language code (e.g., `fr_fr.json` for French)
3. Copy the contents of `en_us.json` as a template
4. Translate all strings

### Translation Notes

- Machine translations are acceptable as a starting point
- The community often improves translations after release
- Keep the same JSON key structure as `en_us.json`

## Testing

### Before Submitting

1. Build the project: `./gradlew build --no-daemon`
2. Test in a Minecraft instance with required dependencies
3. Verify your changes work as expected
4. Check for any console errors or warnings
   
## Communication

- **GitHub Issues**: For bugs and feature requests
- **Pull Requests**: For code contributions

## License

By contributing to this project, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to Orbital Railgun Enhanced! 🚀
