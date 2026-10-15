#!/bin/bash
#
# Narrows a JSON array of matrix entries (test categories or puppeteer scenarios) to those whose jobs failed, timed out
# or were cancelled in the latest attempt of the previous completed run of the current workflow. The file is narrowed in
# place and the selection is written to the step summary. Matrix jobs must be named exactly after their entry.
#
#   select-failed-jobs.sh <entries.json>
#
# Requires the gh CLI with a token that can read Actions (the calling job needs actions: read).

set -euo pipefail

entries="${1:?Usage: select-failed-jobs.sh <entries.json>}"

workflow_file="$(basename "${GITHUB_WORKFLOW_REF%@*}")"
previous_run="$(gh api "repos/${GITHUB_REPOSITORY}/actions/workflows/${workflow_file}/runs?status=completed&per_page=20" \
  --jq "first(.workflow_runs[] | select(.id != ${GITHUB_RUN_ID})) | {id, html_url, conclusion}")"
if [[ -z "${previous_run}" ]]; then
  echo "::error::No completed run of ${workflow_file} was found to retry"
  exit 1
fi
previous_run_id="$(jq -r .id <<< "${previous_run}")"
previous_run_url="$(jq -r .html_url <<< "${previous_run}")"
echo "Selecting failed jobs from ${previous_run_url} ($(jq -r .conclusion <<< "${previous_run}"))"
gh api --paginate "repos/${GITHUB_REPOSITORY}/actions/runs/${previous_run_id}/jobs?filter=latest&per_page=100" \
  --jq '.jobs[] | select(.conclusion == "failure" or .conclusion == "timed_out" or .conclusion == "cancelled") | .name' \
  | jq -R . | jq -s 'unique' > "${RUNNER_TEMP}/failed-jobs.json"
jq -c --slurpfile failed "${RUNNER_TEMP}/failed-jobs.json" '[.[] | select(IN($failed[0][]))]' \
  "${entries}" > "${RUNNER_TEMP}/failed-entries.json"
mv "${RUNNER_TEMP}/failed-entries.json" "${entries}"
count="$(jq length "${entries}")"
if [[ "${count}" -eq 0 ]]; then
  echo "::notice::No failed jobs found in ${previous_run_url}; nothing to retry"
fi
{
  echo "### Retrying ${count} failed job(s) from [run ${previous_run_id}](${previous_run_url})"
  jq -r '.[] | "- \(.)"' "${entries}"
} >> "${GITHUB_STEP_SUMMARY}"
