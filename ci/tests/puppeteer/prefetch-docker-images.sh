#!/bin/bash
#
# Pulls the Docker images a puppeteer scenario's bootstrap, init and ready scripts need, in the background, so
# the download overlaps the rest of the job setup instead of happening inside the init scripts.
#
#   prefetch-docker-images.sh <scenario>           start the pulls in the background and return
#   prefetch-docker-images.sh <scenario> --list    print the images and exit
#   prefetch-docker-images.sh --summary            print the background pull log
#
# Images are discovered from the scripts' docker run/pull/build commands, *_IMAGE assignments, the compose
# files and Dockerfiles they use, and the ci/tests scripts they call; images the scripts build are skipped.
# This is best-effort: an image that is missed or still downloading is pulled by the init script as before.

LOG_FILE="${RUNNER_TEMP:-${TMPDIR:-/tmp}}/docker-prefetch.log"

if [[ "$1" == "--summary" ]]; then
  [[ -f "${LOG_FILE}" ]] && cat "${LOG_FILE}"
  exit 0
fi

SCENARIO="${1##*/}"
if [[ -z "${SCENARIO}" ]]; then
  echo "Usage: $0 <scenario> [--list] | --summary"
  exit 1
fi

if [[ "$2" != "--list" ]] && ! docker info >/dev/null 2>&1; then
  echo "Docker engine is not available; skipping image prefetch."
  exit 0
fi

images=$(python3 - "${PWD}" "${SCENARIO}" <<'PYTHON'
import json, os, re, shlex, sys

root, scenario = sys.argv[1], sys.argv[2]
config_file = os.path.join(root, "ci/tests/puppeteer/scenarios", scenario, "script.json")
if not os.path.isfile(config_file):
    sys.exit(0)
with open(config_file, errors="replace") as f:
    config = json.load(f)
if str(config.get("enabled", True)).lower() == "false":
    sys.exit(0)
required_variables = str((config.get("conditions") or {}).get("env") or "")
if any(not os.environ.get(name.strip()) for name in required_variables.split(",") if name.strip()):
    sys.exit(0)

BOOLEAN_OPTIONS = {"-d", "--detach", "-i", "--interactive", "-t", "--tty", "--rm", "-q", "--quiet", "--privileged",
                   "--init", "-P", "--publish-all", "--read-only", "--no-healthcheck", "--oom-kill-disable"}
SCRIPT_REFERENCE = re.compile(r"ci/tests/[\w.-]+/(?:[\w.-]+/)*[\w.-]+\.sh")
ASSIGNMENT = re.compile(r"^\s*(?:export\s+)?([A-Za-z_][A-Za-z0-9_]*)=(.*)$")
IMAGE_REFERENCE = re.compile(r"^[a-z0-9][a-z0-9._/-]*(:[A-Za-z0-9_][A-Za-z0-9_.-]*)?(@sha256:[a-f0-9]{64})?$")

wanted, built, visited = [], set(), set()

def substitute(value, variables):
    value = value.replace("${PWD}", root).replace("$PWD", root).replace("${SCENARIO}", scenario)
    value = re.sub(r"\$\{(\w+):-([^}]*)\}", lambda m: variables.get(m.group(1), m.group(2)), value)
    return re.sub(r"\$\{(\w+)\}|\$(\w+)", lambda m: variables.get(m.group(1) or m.group(2), m.group(0)), value)

def resolve_path(path):
    return os.path.normpath(path if os.path.isabs(path) else os.path.join(root, path))

def add_image(image):
    if image and "$" not in image and IMAGE_REFERENCE.match(image) and image not in wanted:
        wanted.append(image)

def tokens(command):
    try:
        return shlex.split(command, comments=True)
    except ValueError:
        return []

BUILD_BOOLEAN_OPTIONS = {"-q", "--quiet", "--no-cache", "--pull", "--load", "--push", "--rm", "--force-rm"}

def positionals(args, boolean_options, short_flags):
    found, index = [], 0
    while index < len(args):
        arg = args[index]
        if not arg.startswith("-"):
            found.append(arg)
        else:
            flags = not arg.startswith("--") and set(arg[1:]) <= set(short_flags)
            attached_value = "=" in arg or (not arg.startswith("--") and len(arg) > 2 and not flags)
            if not attached_value and not flags and arg not in boolean_options:
                index += 1
        index += 1
    return found

def first_positional(args):
    found = positionals(args, BOOLEAN_OPTIONS, "dqitP")
    return found[0] if found else None

def option_values(args, names):
    values = []
    for index, arg in enumerate(args):
        for name in names:
            if arg == name and index + 1 < len(args):
                values.append(args[index + 1])
            elif arg.startswith(name + "="):
                values.append(arg.split("=", 1)[1])
    return values

def parse_dockerfile(path):
    if not os.path.isfile(path):
        return
    arguments, stages = {}, {"scratch"}
    with open(path, errors="replace") as f:
        for line in f:
            words = line.split()
            if len(words) >= 2 and words[0].upper() == "ARG" and "=" in words[1]:
                name, value = words[1].split("=", 1)
                arguments.setdefault(name, value.strip("'\""))
            elif len(words) >= 2 and words[0].upper() == "FROM":
                words = [word for word in words[1:] if not word.startswith("--")]
                if words and words[0].lower() not in stages:
                    add_image(substitute(words[0], arguments))
                if len(words) >= 3 and words[1].upper() == "AS":
                    stages.add(words[2].lower())

def parse_compose(path):
    if not os.path.isfile(path):
        return
    services, current = {}, None
    with open(path, errors="replace") as f:
        for line in f:
            service = re.match(r"^  ([\w.-]+):\s*$", line)
            if service:
                current = services.setdefault(service.group(1), {})
            elif current is not None and re.match(r"^\S", line):
                current = None
            elif current is not None:
                entry = re.match(r"^    (image|build):\s*['\"]?([^'\"#]*)", line)
                if entry:
                    current[entry.group(1)] = entry.group(2).strip()
    for definition in services.values():
        if "build" in definition:
            built.add(definition.get("image", ""))
        else:
            add_image(definition.get("image"))

def parse_script(path):
    path = resolve_path(path)
    if path in visited or not os.path.isfile(path):
        return
    visited.add(path)
    variables = {"SCENARIO": scenario, "SCENARIO_FOLDER": os.path.dirname(config_file)}
    with open(path, errors="replace") as f:
        lines = re.sub(r"\\\n", " ", f.read()).splitlines()
    for line in lines:
        if line.lstrip().startswith("#"):
            continue
        assignment = ASSIGNMENT.match(line)
        if assignment:
            value = tokens(substitute(assignment.group(2), variables))
            if value:
                variables[assignment.group(1)] = value[0]
                if assignment.group(1).endswith("COMPOSE_FILE"):
                    parse_compose(resolve_path(value[0]))
                elif assignment.group(1).endswith("IMAGE"):
                    add_image(value[0])
        for command in re.split(r"&&|\|\||;|\|", line):
            words = tokens(substitute(command, variables))
            if len(words) < 3 or words[0] != "docker":
                continue
            if words[1] in ("run", "create", "pull"):
                add_image(first_positional(words[2:]))
            elif words[1] == "build" or words[1:3] == ["buildx", "build"]:
                args = words[2:] if words[1] == "build" else words[3:]
                built.update(option_values(args, ["-t", "--tag"]))
                context = next(iter(positionals(args, BUILD_BOOLEAN_OPTIONS, "q")), None)
                dockerfiles = option_values(args, ["-f", "--file"])
                if dockerfiles:
                    parse_dockerfile(resolve_path(dockerfiles[0]))
                elif context:
                    parse_dockerfile(os.path.join(resolve_path(context), "Dockerfile"))
            elif words[1] == "compose":
                for compose_file in option_values(words, ["-f", "--file"]):
                    parse_compose(resolve_path(compose_file))
        for reference in SCRIPT_REFERENCE.findall(substitute(line, variables)):
            parse_script(reference)

for key in ("bootstrapScript", "initScript", "readyScript"):
    for script in str(config.get(key) or "").split(","):
        if script.strip():
            parse_script(substitute(script.strip(), {}))

for image in wanted:
    name = image.split("@")[0].rsplit(":", 1)[0] if ":" in image.split("/")[-1] else image
    if image not in built and name + ":latest" not in built and name not in built and not name.startswith("cas-" + scenario):
        print(image)
PYTHON
)
if [[ $? -ne 0 ]]; then
  echo "Unable to determine the Docker images for ${SCENARIO}; skipping image prefetch."
  exit 0
fi

if [[ "$2" == "--list" ]]; then
  [[ -n "${images}" ]] && echo "${images}"
  exit 0
fi

if [[ -z "${images}" ]]; then
  echo "Scenario ${SCENARIO} does not need any Docker images."
  exit 0
fi

echo "Prefetching Docker images for ${SCENARIO} in the background (log: ${LOG_FILE}):"
echo "${images}" | sed 's/^/  /'

function pullImage() {
  local start=${SECONDS}
  if docker pull --quiet "$1" >/dev/null 2>&1; then
    echo "Pulled $1 in $((SECONDS - start))s"
  else
    echo "Failed to pull $1 after $((SECONDS - start))s; the init script will pull it."
  fi
}
export -f pullImage

nohup bash -c 'start=${SECONDS}; xargs -r -P 4 -I {} bash -c "pullImage {}"; echo "Prefetch finished in $((SECONDS - start))s"' \
  <<<"${images}" >"${LOG_FILE}" 2>&1 &
disown
exit 0
