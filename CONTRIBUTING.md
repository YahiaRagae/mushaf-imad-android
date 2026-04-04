# Contributing to Mushaf Imad Android Library

Thank you for your interest in contributing to the Mushaf Imad Android Library! This document provides guidelines and instructions for contributing to this project.

## Code of Conduct

Please be respectful and considerate of others when contributing to this project. We aim to foster an inclusive and welcoming community.

## How to Contribute

### Reporting Issues

If you find a bug or have a feature request, please check the existing issues first to avoid duplicates. When creating a new issue:

1. Use a clear and descriptive title
2. Provide detailed steps to reproduce the issue
3. Include relevant code snippets, logs, or screenshots
4. Specify your environment (Android version, device, library version)

### Submitting Pull Requests

1. Fork the repository and create your branch from `main`
2. Make your changes following the code style guidelines
3. Add or update tests as necessary
4. Ensure all tests pass
5. Update documentation if needed
6. Submit a pull request with a clear description of changes

## Development Setup

### Prerequisites

- Android Studio (latest stable version)
- JDK 17 or higher
- Android SDK with API level 24+

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
./gradlew test
```

## Code Style Guidelines

### Kotlin Style

We follow the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html). Key points:

- Use 4 spaces for indentation (no tabs)
- Use `camelCase` for variable and function names
- Use `PascalCase` for class and interface names
- Use `UPPER_SNAKE_CASE` for constants
- Prefer `val` over `var` when possible
- Use meaningful and descriptive names

### Android Specific Guidelines

- Follow Android [API guidelines](https://developer.android.com/guide/topics/ui/declaring-layout)
- Use ViewBinding or Compose for UI
- Handle lifecycle appropriately
- Use coroutines for asynchronous operations
- Follow Material Design guidelines

### Architecture

- Follow clean architecture principles
- Keep components modular and testable
- Use dependency injection (preferably Hilt)
- Separate business logic from UI

## Testing Requirements

### Unit Tests

- Write unit tests for all business logic
- Use JUnit 5 for testing
- Mock dependencies using MockK
- Aim for high test coverage (minimum 80%)

### Integration Tests

- Test integration between modules
- Use Espresso for UI testing
- Test on multiple API levels

### Running Tests

All tests must pass before submitting a pull request:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Pull Request Template

When submitting a pull request, please use the following template:

```markdown
## Description

Please include a summary of the changes and the related issue. Include relevant motivation and context.

## Type of Change

- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update
- [ ] Code refactoring
- [ ] Performance improvement

## Testing

Please describe the tests you ran to verify your changes:

- [ ] Unit tests
- [ ] Integration tests
- [ ] UI tests
- [ ] Manual testing

## Checklist

- [ ] My code follows the style guidelines of this project
- [ ] I have performed a self-review of my code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix/feature works
- [ ] New and existing unit tests pass locally with my changes
- [ ] Any dependent changes have been merged and published

## Screenshots (if applicable)

Add screenshots to help explain your changes.

## Additional Notes

Add any additional information about the pull request here.
```

## Commit Guidelines

We follow [Conventional Commits](https://www.conventionalcommits.org/) specification:

- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `style:` Code style changes (formatting, missing semi-colons, etc.)
- `refactor:` Code refactoring
- `test:` Adding or updating tests
- `chore:` Maintenance tasks

Example:
```
feat: add audio playback controls
fix: resolve memory leak in page rendering
docs: update installation instructions
```

## Review Process

1. All pull requests require at least one review
2. Reviewers will check for code quality, tests, and documentation
3. Address review comments promptly
4. Once approved, maintainers will merge the pull request

## Getting Help

If you need help or have questions:

- Check the existing documentation
- Look at similar issues or pull requests
- Ask in the issue comments

## License

By contributing to this project, you agree that your contributions will be licensed under the project's license.

Thank you for contributing to Mushaf Imad Android Library!
