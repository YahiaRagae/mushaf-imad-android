#!/usr/bin/env bash
# Show JitPack download counts per published version, plus GitHub APK downloads.
#
#   scripts/jitpack-stats.sh                       # this library
#   scripts/jitpack-stats.sh other-owner/other-repo
#
# JitPack's stats endpoint is undocumented (it is what the "Stats" tab on
# jitpack.io uses). Only week and month windows exist - there is no all-time
# total, and no per-module breakdown (module paths always answer 0).
# It counts what resolves through JitPack, so cached or mirrored consumers are
# invisible: read these as a floor, not a true install count.
set -euo pipefail

SLUG="${1:-YahiaRagae/mushaf-imad-android}"
OWNER="${SLUG%%/*}"
REPO="${SLUG##*/}"
GROUP="com.github.${OWNER}"
API="https://jitpack.io/api"

command -v jq >/dev/null || { echo "needs jq: brew install jq" >&2; exit 1; }

get() { curl -sS --max-time 30 "$1"; }

# The versions come from the builds API - the same endpoint that reports whether
# each version built. That is the answer to "how do I list versions": there is no
# separate versions API, and this one is better than git tags because a tag that
# JitPack never built successfully is not actually consumable.
builds_json="$(get "${API}/builds/${GROUP}/${REPO}")"
versions="$(jq -r --arg g "$GROUP" --arg r "$REPO" '.[$g][$r] // {} | keys[]' <<<"$builds_json" 2>/dev/null || true)"

if [ -z "$versions" ]; then
  echo "No published versions found for ${GROUP}:${REPO}" >&2
  exit 1
fi

total="$(get "${API}/stats/${GROUP}/${REPO}")"
echo "JitPack downloads - ${GROUP}:${REPO}"
echo
printf '  %-12s %-8s %8s %8s\n' VERSION BUILD WEEK MONTH
printf '  %-12s %-8s %8s %8s\n' ------------ -------- -------- --------

# Newest first: sort by version segments numerically.
while read -r v; do
  [ -n "$v" ] || continue
  build="$(jq -r --arg g "$GROUP" --arg r "$REPO" --arg v "$v" '.[$g][$r][$v]' <<<"$builds_json")"
  s="$(get "${API}/stats/${GROUP}/${REPO}/${v}")"
  printf '  %-12s %-8s %8s %8s\n' \
    "$v" "$build" \
    "$(jq -r '.week  // 0' <<<"$s")" \
    "$(jq -r '.month // 0' <<<"$s")"
done < <(sort -t. -k1,1nr -k2,2nr -k3,3nr <<<"$versions")

printf '  %-12s %-8s %8s %8s\n' ------------ -------- -------- --------
printf '  %-12s %-8s %8s %8s\n' 'ALL' '' \
  "$(jq -r '.week  // 0' <<<"$total")" \
  "$(jq -r '.month // 0' <<<"$total")"
echo
echo "  Note: per-version figures do not sum to ALL - JitPack counts them"
echo "  differently. Treat the breakdown as indicative."

# APK downloads are a separate metric: GitHub only counts release ASSETS.
if command -v gh >/dev/null; then
  apk="$(gh api "repos/${SLUG}/releases" \
        --jq '[.[] | select(.assets | length > 0)
               | "  \(.tag_name): " + ([.assets[] | "\(.name) = \(.download_count)"] | join(", "))] | join("\n")' \
        2>/dev/null || true)"
  if [ -n "$apk" ]; then
    echo
    echo "GitHub release assets (APK downloads)"
    echo
    echo "$apk"
  fi
fi
