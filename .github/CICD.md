# CI/CD Workflows

This repository includes GitHub Actions workflows for automated building and releasing of the QR Code Scanner app.

## Workflows

### 1. Build Test (`build.yml`)
- **Triggers**: Pull requests and pushes to main/master branches
- **Purpose**: Validates that the code builds successfully and runs tests
- **Outputs**: Debug APK as an artifact

### 2. Build and Release APK (`release.yml`)
- **Triggers**: 
  - When a release is published on GitHub
  - When a tag starting with 'v' is pushed (e.g., `v1.0.0`)
- **Purpose**: Builds the release APK and attaches it to the GitHub release
- **Outputs**: 
  - Release APK attached to the GitHub release
  - Release APK as an artifact for tag pushes

## Creating a Release

### Option 1: Using the Release Script (Recommended)

The repository includes a helper script to streamline the release process:

```bash
./scripts/release.sh v1.0.0-alpha-06
```

This script will:
- Validate the version format
- Check if you're on the main/master branch
- Optionally update the version in `app/build.gradle.kts`
- Create and push the git tag
- Provide links to track the build progress

### Option 2: Manual Process

To create a new release manually:

1. **Create and push a tag**:
   ```bash
   git tag v1.0.0-alpha-06
   git push origin v1.0.0-alpha-06
   ```

2. **Create a GitHub release**:
   - Go to the repository's Releases page
   - Click "Create a new release"
   - Select the tag you just created
   - Fill in the release title and description
   - Click "Publish release"

3. **Automatic APK build**:
   - The workflow will automatically trigger
   - Build the release APK
   - Attach the APK to the release

## APK Naming

The generated APK will be named: `qr-code-scanner-{version}.apk`

Where `{version}` is extracted from either:
- The git tag (if triggered by tag push)
- The `versionName` from `app/build.gradle.kts`

## Build Environment

The workflows use:
- Ubuntu latest
- Java 17 (Temurin distribution)
- Android SDK (via android-actions/setup-android)
- Gradle caching for faster builds