#!/bin/bash
set -e
if [[ -z $TMPDIR ]]; then
  TMPDIR=${TEMP:-/tmp}
fi
echo "Recreating services directory at ${TMPDIR}/cas-services"
rm -rf "${TMPDIR}/cas-services"
mkdir "${TMPDIR}/cas-services"
