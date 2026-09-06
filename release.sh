#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

PROPS="$ROOT/gradle.properties"
CHANGELOG="$ROOT/CHANGELOG.md"

CURRENT_MOD_VERSION=$(grep -E '^[[:space:]]*mod_version=' "$PROPS" | cut -d= -f2)

echo "Current mod version: $CURRENT_MOD_VERSION"
echo ""
read -rp "New mod version (e.g. 0.2.14+jade.1.21.11): " NEW_MOD_VERSION

if [[ -z "$NEW_MOD_VERSION" ]]; then
	echo "Aborted: no version entered."
	exit 1
fi

TAG="v${NEW_MOD_VERSION}"

# Read content under ## [Current]
CHANGELOG_CONTENT=$(awk '
    /^## \[Current\]/ { flag=1; next }
    /^## \[/          { if (flag) exit }
    flag              { print }
' "$CHANGELOG")

if [[ -z "$(echo "$CHANGELOG_CONTENT" | tr -d '[:space:]')" ]]; then
	echo ""
	echo "## [Current] section is empty. Add your changelog entries first."
	exit 1
fi

echo ""
echo "Changelog for $TAG:"
echo "---"
echo "$CHANGELOG_CONTENT"
echo "---"
echo ""
echo "Will create tag: $TAG"
read -rp "Confirm? [y/N] " CONFIRM
[[ "$CONFIRM" =~ ^[Yy]$ ]] || {
	echo "Aborted."
	exit 1
}

TODAY=$(date +%Y-%m-%d)

# Replace ## [Current] with ## [Current]\n\n## [VERSION] - DATE
# The existing content stays under the new versioned header; a fresh empty [Current] sits on top.
sed -i "s/^## \[Current\]/## [Current]\n\n## [${NEW_MOD_VERSION}] - ${TODAY}/" "$CHANGELOG"

# Update gradle.properties (preserves leading tab indentation used in this file)
sed -i -E "s/^([[:space:]]*)mod_version=.*/\1mod_version=${NEW_MOD_VERSION}/" "$PROPS"

echo ""
echo "Updated CHANGELOG.md and gradle.properties"

cd "$ROOT"
git add "$CHANGELOG" "$PROPS"
git commit -m "chore: bump version to ${NEW_MOD_VERSION}"
git tag "$TAG"
git push origin HEAD
git push origin "$TAG"

echo ""
echo "Pushed tag $TAG - release CI is now running."
