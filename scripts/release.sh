#!/bin/bash

# Release script for QR Code Scanner
# Usage: ./scripts/release.sh <version>
# Example: ./scripts/release.sh v1.0.0-alpha-06

set -e

if [ $# -eq 0 ]; then
    echo "Usage: $0 <version>"
    echo "Example: $0 v1.0.0-alpha-06"
    exit 1
fi

VERSION=$1

# Validate version format
if [[ ! $VERSION =~ ^v[0-9]+\.[0-9]+\.[0-9]+.*$ ]]; then
    echo "Error: Version must start with 'v' and follow semantic versioning (e.g., v1.0.0-alpha-06)"
    exit 1
fi

echo "Creating release for version: $VERSION"

# Check if tag already exists
if git rev-parse "$VERSION" >/dev/null 2>&1; then
    echo "Error: Tag $VERSION already exists"
    exit 1
fi

# Get current branch
CURRENT_BRANCH=$(git branch --show-current)
echo "Current branch: $CURRENT_BRANCH"

# Ensure we're on main/master branch
if [[ "$CURRENT_BRANCH" != "main" && "$CURRENT_BRANCH" != "master" ]]; then
    echo "Warning: You're not on main/master branch. Continue? (y/N)"
    read -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Aborted"
        exit 1
    fi
fi

# Ensure working directory is clean
if ! git diff-index --quiet HEAD --; then
    echo "Error: Working directory is not clean. Please commit your changes first."
    exit 1
fi

# Update version in build.gradle.kts if needed
echo "Current version in build.gradle.kts:"
grep 'versionName' app/build.gradle.kts || echo "Version not found"

echo "Do you want to update the version in build.gradle.kts? (y/N)"
read -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    # Extract version without 'v' prefix
    VERSION_NUMBER=${VERSION#v}
    sed -i.bak "s/versionName = \".*\"/versionName = \"$VERSION_NUMBER\"/" app/build.gradle.kts
    echo "Updated versionName to $VERSION_NUMBER"
    
    # Commit version update
    git add app/build.gradle.kts
    git commit -m "Bump version to $VERSION_NUMBER"
fi

# Create and push tag
echo "Creating tag: $VERSION"
git tag -a "$VERSION" -m "Release $VERSION"

echo "Pushing tag to origin..."
git push origin "$VERSION"

echo "✅ Tag $VERSION has been pushed!"
echo "🚀 GitHub Actions will now build the APK automatically."
echo "📱 You can find the APK in the releases page once the build completes."
echo "🔗 https://github.com/$(git config --get remote.origin.url | sed 's/.*github.com[:/]\(.*\)\.git/\1/')/releases"