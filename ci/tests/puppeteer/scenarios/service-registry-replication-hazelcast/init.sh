#!/bin/bash
set -e

echo "Recreating services directory at ${TMPDIR}/cas-services"
rm -rf "${TMPDIR}/cas-services"
mkdir "${TMPDIR}/cas-services"
