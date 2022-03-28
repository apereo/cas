#!/bin/bash

# semicolon list of paths not to mess with
export MSYS2_ARG_CONV_EXCL=/keystore.jwks

PUPPETEER_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
SCENARIOS_FOLDER=${PUPPETEER_DIR}/scenarios
echo $SCENARIOS_FOLDER
mkdir -p $PUPPETEER_DIR/logs
set -o pipefail
export CI="true"
for scenario in `find $SCENARIOS_FOLDER -mindepth 1 -maxdepth 1 -type d -printf '%f '`; do
  if [[ -f "${PUPPETEER_DIR}/logs/0-${scenario}.log" ]]; then
    echo "Skipping $scenario because of previous success "
    continue;
  fi
  echo Running scenario $scenario in ${SCENARIOS_FOLDER}/$scenario
  $PUPPETEER_DIR/run.sh --nc --headless --rebuild --scenario ${SCENARIOS_FOLDER}/$scenario 2>&1 | tee $PUPPETEER_DIR/logs/$scenario.log
  RC=$?
  echo "Scenario ${scenario} completed with return code ${RC}"
  if [[ $RC -ne 0 ]]; then
    echo "$scenario $(date '+%Y%m%d %H:%M:%S')" >> $PUPPETEER_DIR/logs/errorlist.log
  fi
  mv $PUPPETEER_DIR/logs/${scenario}.log $PUPPETEER_DIR/logs/${RC}-${scenario}.log
done
