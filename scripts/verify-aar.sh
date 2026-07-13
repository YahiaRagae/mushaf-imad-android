#!/usr/bin/env bash
#
# Verify that the AARs we publish actually contain what a consumer needs.
#
# `publishToMavenLocal` only proves the publish machinery runs. It says nothing
# about what ends up inside the archive. Every assertion below corresponds to
# something that has been wrong at least once:
#
#   - 0.2.1 shipped without the WAKE_LOCK permission the library itself needs,
#     so every consumer crashed the moment audio started. The bundled sample app
#     declared the permission in its own manifest, which hid it from us.
#   - The Quran database, the page images and the consumer ProGuard rules are
#     assets/config a build can silently drop.
#
# Run after:
#   ./gradlew :mushaf-core:publishToMavenLocal :mushaf-ui:publishToMavenLocal -x lint
set -euo pipefail

VERSION="$(sed -n 's/^VERSION_NAME=//p' gradle.properties)"
GROUP_PATH="$(sed -n 's/^GROUP=//p' gradle.properties | tr '.' '/')"
REPO="${HOME}/.m2/repository/${GROUP_PATH}"

CORE_AAR="${REPO}/mushaf-core/${VERSION}/mushaf-core-${VERSION}.aar"
UI_AAR="${REPO}/mushaf-ui/${VERSION}/mushaf-ui-${VERSION}.aar"
UI_POM="${REPO}/mushaf-ui/${VERSION}/mushaf-ui-${VERSION}.pom"

fail() { echo "::error::$1"; exit 1; }
ok()   { echo "  ok  $1"; }

# NOTE: deliberately no `grep -q` on a piped-in variable. `grep -q` exits on the
# first match and closes the pipe, the writer takes SIGPIPE, and `set -o pipefail`
# then reports the whole pipeline as failed - so the check fails on a *correct*
# artifact. Match with bash instead, and count with `grep -c || true`.
contains() { [[ "$1" == *"$2"* ]]; }
count()    { printf '%s\n' "$1" | grep -c -- "$2" || true; }

echo "Verifying published artifacts for ${VERSION}"

for f in "$CORE_AAR" "$UI_AAR" "$UI_POM"; do
  [ -f "$f" ] || fail "missing artifact: $f"
done
ok "both AARs and the POM were produced"

core_list="$(unzip -l "$CORE_AAR")"

# --- The Quran itself ---------------------------------------------------------
contains "$core_list" "assets/quran.realm" \
  || fail "mushaf-core AAR has no assets/quran.realm - the app would ship with no Quran"

realm_bytes="$(printf '%s\n' "$core_list" | awk '$4 == "assets/quran.realm" { print $1 }')"
[ "${realm_bytes:-0}" -gt 5000000 ] \
  || fail "assets/quran.realm is only ${realm_bytes:-0} bytes - it looks truncated"
ok "quran.realm present (${realm_bytes} bytes)"

images="$(count "$core_list" "assets/quran-images/")"
[ "$images" -gt 9000 ] || fail "only ${images} page images in the AAR - expected ~9665"
ok "${images} page images present"

timings="$(count "$core_list" "assets/ayah_timing/read_.*\.json")"
[ "$timings" -eq 18 ] || fail "${timings} reciter timing files in the AAR - expected 18"
ok "${timings} reciter timing files present"

# --- Consumer ProGuard rules --------------------------------------------------
# Without these a consumer's minified release build breaks at runtime on
# Realm/Koin/Media3 reflection - and only in release, which is the worst possible
# place to find out.
contains "$core_list" "proguard.txt" \
  || fail "mushaf-core AAR ships no consumer ProGuard rules (proguard.txt)"
ok "consumer ProGuard rules present"

# --- Manifest: what the LIBRARY must bring with it ----------------------------
tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT
unzip -q -o "$CORE_AAR" AndroidManifest.xml -d "$tmp"
manifest="$(cat "${tmp}/AndroidManifest.xml")"

for perm in WAKE_LOCK FOREGROUND_SERVICE FOREGROUND_SERVICE_MEDIA_PLAYBACK POST_NOTIFICATIONS; do
  contains "$manifest" "android.permission.${perm}" \
    || fail "the AAR manifest does not declare ${perm}. The library needs it, so the library must declare it - a consumer cannot be expected to know."
  ok "manifest declares ${perm}"
done

contains "$manifest" "AudioPlaybackService" \
  || fail "AudioPlaybackService is not declared in the AAR manifest - background audio would never start"
contains "$manifest" "MushafInitProvider" \
  || fail "MushafInitProvider is not declared in the AAR manifest - zero-config init would never run"
ok "AudioPlaybackService and MushafInitProvider are declared"

# --- mushaf-ui must drag mushaf-core in --------------------------------------
ui_pom="$(cat "$UI_POM")"
contains "$ui_pom" "<artifactId>mushaf-core</artifactId>" \
  || fail "mushaf-ui's POM does not depend on mushaf-core - a consumer adding only mushaf-ui would get no data layer"
ok "mushaf-ui's POM pulls mushaf-core transitively"

echo "Published artifacts look right."
