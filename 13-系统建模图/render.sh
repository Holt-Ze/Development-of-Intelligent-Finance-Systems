#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOT_DIR="${ROOT_DIR}/dot"
SVG_DIR="${ROOT_DIR}/svg"

mkdir -p "${SVG_DIR}"

DOT_FILES=(
  "04-activity.dot"
  "05-state-machine.dot"
  "06-component.dot"
  "07-deployment.dot"
  "08-object.dot"
  "10-package.dot"
  "11-dfd.dot"
  "13-flowchart.dot"
  "14-architecture.dot"
)

for name in "${DOT_FILES[@]}"; do
  file="${DOT_DIR}/${name}"
  if [[ ! -f "${file}" ]]; then
    echo "skip missing ${file}"
    continue
  fi
  name="$(basename "${file}" .dot)"
  dot -Tsvg "${file}" -o "${SVG_DIR}/${name}.svg"
  echo "generated ${SVG_DIR}/${name}.svg"
done

echo "Graphviz SVG files generated for simple diagrams."
echo "manual SVG diagrams are kept as-is: 01-use-case, 02-class, 03-sequence, 09-communication, 12-er."
