#!/usr/bin/env bash

set -euo pipefail

GREEN="\033[32m"
RED="\033[31m"
YELLOW="\033[33m"
ENDCOLOR="\033[0m"

function printgreen() {
    printf "🍀 ${GREEN}%s${ENDCOLOR}\n" "$1"
}

function printred() {
    printf "🚨 ${RED}%s${ENDCOLOR}\n" "$1"
}

function printyellow() {
    printf "⚠️  ${YELLOW}%s${ENDCOLOR}\n" "$1"
}

DOCKER_IMAGE="mongo:8.3"
MONGO_CONTAINER="${MONGO_CONTAINER:-mongodb-server-clustered}"
MONGO_REPLICA_SET="${MONGO_REPLICA_SET:-rs0}"

MONGO_ROOT_USERNAME="${MONGO_ROOT_USERNAME:-root}"
MONGO_ROOT_PASSWORD="${MONGO_ROOT_PASSWORD:-secret}"
MONGO_DATABASE="${MONGO_DATABASE:-cas}"

MONGO_PORT_1="${MONGO_PORT_1:-37017}"
MONGO_PORT_2="${MONGO_PORT_2:-37018}"
MONGO_PORT_3="${MONGO_PORT_3:-37019}"

SCRIPT_DIRECTORY="$(
    cd "$(dirname "${BASH_SOURCE[0]}")"
    pwd
)"

MONGO_WORK_DIRECTORY="${SCRIPT_DIRECTORY}/build/mongodb-replica-set"
MONGO_CONTAINER_SCRIPT="${MONGO_WORK_DIRECTORY}/start-mongodb.sh"
MONGO_KEYFILE="${MONGO_WORK_DIRECTORY}/mongodb-keyfile"

MONGO_URI="mongodb://${MONGO_ROOT_USERNAME}:${MONGO_ROOT_PASSWORD}"
MONGO_URI+="@localhost:${MONGO_PORT_1}"
MONGO_URI+=",localhost:${MONGO_PORT_2}"
MONGO_URI+=",localhost:${MONGO_PORT_3}"
MONGO_URI+="/${MONGO_DATABASE}"
MONGO_URI+="?authSource=admin"
MONGO_URI+="&replicaSet=${MONGO_REPLICA_SET}"

function cleanup_failed_startup() {
    local exit_code=$?

    if [[ "${exit_code}" -ne 0 ]]; then
        printred "MongoDB replica-set startup failed."

        docker logs "${MONGO_CONTAINER}" 2>/dev/null || true
        docker rm -f "${MONGO_CONTAINER}" >/dev/null 2>&1 || true
    fi

    exit "${exit_code}"
}

trap cleanup_failed_startup EXIT

function remove_existing_container() {
    printgreen "Removing the existing MongoDB container..."

    docker rm -f "${MONGO_CONTAINER}" >/dev/null 2>&1 || true
}

function prepare_work_directory() {
    printgreen "Preparing MongoDB replica-set files..."

    rm -rf "${MONGO_WORK_DIRECTORY}"
    mkdir -p "${MONGO_WORK_DIRECTORY}"

    openssl rand -base64 756 > "${MONGO_KEYFILE}"
    chmod 400 "${MONGO_KEYFILE}"
}

function create_container_startup_script() {
    printgreen "Creating the MongoDB container startup script..."

    cat > "${MONGO_CONTAINER_SCRIPT}" <<'CONTAINER_SCRIPT'
#!/usr/bin/env bash

set -euo pipefail

MONGO_REPLICA_SET="${MONGO_REPLICA_SET:?MONGO_REPLICA_SET is required}"
MONGO_ROOT_USERNAME="${MONGO_ROOT_USERNAME:?MONGO_ROOT_USERNAME is required}"
MONGO_ROOT_PASSWORD="${MONGO_ROOT_PASSWORD:?MONGO_ROOT_PASSWORD is required}"
MONGO_DATABASE="${MONGO_DATABASE:?MONGO_DATABASE is required}"

MONGO_PORT_1="${MONGO_PORT_1:?MONGO_PORT_1 is required}"
MONGO_PORT_2="${MONGO_PORT_2:?MONGO_PORT_2 is required}"
MONGO_PORT_3="${MONGO_PORT_3:?MONGO_PORT_3 is required}"

KEYFILE_SOURCE="/run/mongodb-source/mongodb-keyfile"
KEYFILE="/run/mongodb/mongodb-keyfile"

DATA_ROOT="/data/replica-set"
LOG_ROOT="/var/log/mongodb"
PID_ROOT="/run/mongodb"

function log() {
    printf "[mongodb-cluster] %s\n" "$1"
}

function print_logs() {
    printf "\n===== MongoDB member 1 =====\n"
    cat "${LOG_ROOT}/member-1.log" 2>/dev/null || true

    printf "\n===== MongoDB member 2 =====\n"
    cat "${LOG_ROOT}/member-2.log" 2>/dev/null || true

    printf "\n===== MongoDB member 3 =====\n"
    cat "${LOG_ROOT}/member-3.log" 2>/dev/null || true
}

function stop_members() {
    log "Stopping MongoDB replica-set members..."

    for port in \
        "${MONGO_PORT_1}" \
        "${MONGO_PORT_2}" \
        "${MONGO_PORT_3}"; do

        mongosh \
            --quiet \
            --host localhost \
            --port "${port}" \
            --username "${MONGO_ROOT_USERNAME}" \
            --password "${MONGO_ROOT_PASSWORD}" \
            --authenticationDatabase admin \
            --eval '
                try {
                    db.adminCommand({shutdown: 1, force: true});
                } catch (error) {
                    // A successful shutdown normally disconnects mongosh.
                }
            ' >/dev/null 2>&1 || true
    done
}

function handle_exit() {
    stop_members
    exit 0
}

trap handle_exit SIGTERM SIGINT
trap print_logs ERR

function prepare_directories() {
    log "Preparing data, log, PID, and key-file directories..."

    mkdir -p \
        "${DATA_ROOT}/member-1" \
        "${DATA_ROOT}/member-2" \
        "${DATA_ROOT}/member-3" \
        "${LOG_ROOT}" \
        "${PID_ROOT}"

    cp "${KEYFILE_SOURCE}" "${KEYFILE}"

    chown -R mongodb:mongodb \
        "${DATA_ROOT}" \
        "${LOG_ROOT}" \
        "${PID_ROOT}"

    chmod 400 "${KEYFILE}"
    chown mongodb:mongodb "${KEYFILE}"
}

function start_member() {
    local member_number="$1"
    local port="$2"

    local db_path="${DATA_ROOT}/member-${member_number}"
    local log_path="${LOG_ROOT}/member-${member_number}.log"
    local pid_path="${PID_ROOT}/member-${member_number}.pid"

    log "Starting member ${member_number} on port ${port}..."

    gosu mongodb mongod \
        --port "${port}" \
        --bind_ip_all \
        --dbpath "${db_path}" \
        --logpath "${log_path}" \
        --pidfilepath "${pid_path}" \
        --replSet "${MONGO_REPLICA_SET}" \
        --keyFile "${KEYFILE}" \
        --auth \
        --fork
}

function wait_for_member_without_authentication() {
    local port="$1"

    log "Waiting for MongoDB on port ${port}..."

    for attempt in $(seq 1 120); do
        if mongosh \
            --quiet \
            --host localhost \
            --port "${port}" \
            --eval 'db.runCommand({ping: 1}).ok' \
            2>/dev/null | grep -qx "1"; then

            return 0
        fi

        sleep 1
    done

    log "MongoDB on port ${port} did not become ready."
    return 1
}

function initiate_replica_set() {
    log "Initializing replica set ${MONGO_REPLICA_SET}..."

    mongosh \
        --quiet \
        --host localhost \
        --port "${MONGO_PORT_1}" \
        --eval "
            const result = rs.initiate({
                _id: '${MONGO_REPLICA_SET}',
                members: [
                    {
                        _id: 0,
                        host: 'localhost:${MONGO_PORT_1}',
                        priority: 2
                    },
                    {
                        _id: 1,
                        host: 'localhost:${MONGO_PORT_2}',
                        priority: 1
                    },
                    {
                        _id: 2,
                        host: 'localhost:${MONGO_PORT_3}',
                        priority: 1
                    }
                ]
            });

            printjson(result);

            if (result.ok !== 1) {
                quit(1);
            }
        "
}

function wait_for_primary_without_authentication() {
    log "Waiting for the initial primary election..."

    for attempt in $(seq 1 120); do
        local is_primary

        is_primary="$(
            mongosh \
                --quiet \
                --host localhost \
                --port "${MONGO_PORT_1}" \
                --eval 'db.hello().isWritablePrimary' \
                2>/dev/null || true
        )"

        if [[ "${is_primary}" == "true" ]]; then
            log "Member on port ${MONGO_PORT_1} is primary."
            return 0
        fi

        sleep 1
    done

    log "The replica set did not elect an initial primary."
    return 1
}

function create_root_user() {
    log "Creating the MongoDB root user..."

    mongosh \
        --quiet \
        --host localhost \
        --port "${MONGO_PORT_1}" \
        admin \
        --eval "
            const adminDatabase = db.getSiblingDB('admin');

            adminDatabase.createUser({
                user: '${MONGO_ROOT_USERNAME}',
                pwd: '${MONGO_ROOT_PASSWORD}',
                roles: [
                    {
                        role: 'root',
                        db: 'admin'
                    }
                ]
            });
        "
}

function wait_for_authenticated_connection() {
    log "Waiting for authenticated MongoDB access..."

    for attempt in $(seq 1 120); do
        if mongosh \
            --quiet \
            --host localhost \
            --port "${MONGO_PORT_1}" \
            --username "${MONGO_ROOT_USERNAME}" \
            --password "${MONGO_ROOT_PASSWORD}" \
            --authenticationDatabase admin \
            --eval 'db.runCommand({ping: 1}).ok' \
            2>/dev/null | grep -qx "1"; then

            return 0
        fi

        sleep 1
    done

    log "Authenticated MongoDB access did not become available."
    return 1
}

function wait_for_all_members() {
    log "Waiting for all three replica-set members..."

    for attempt in $(seq 1 180); do
        local healthy_members

        healthy_members="$(
            mongosh \
                --quiet \
                --host localhost \
                --port "${MONGO_PORT_1}" \
                --username "${MONGO_ROOT_USERNAME}" \
                --password "${MONGO_ROOT_PASSWORD}" \
                --authenticationDatabase admin \
                --eval '
                    try {
                        const members = rs.status().members ?? [];

                        print(
                            members.filter(member =>
                                member.health === 1 &&
                                (
                                    member.stateStr === "PRIMARY" ||
                                    member.stateStr === "SECONDARY"
                                )
                            ).length
                        );
                    } catch (error) {
                        print(0);
                    }
                ' \
                2>/dev/null || printf "0"
        )"

        if [[ "${healthy_members}" == "3" ]]; then
            log "All three replica-set members are healthy."
            return 0
        fi

        sleep 1
    done

    log "Not all replica-set members became healthy."
    return 1
}

function run_optional_initialization_script() {
    local init_script="/docker-entrypoint-initdb.d/mongo-init.sh"

    if [[ ! -f "${init_script}" ]]; then
        log "No optional mongo-init.sh script was mounted."
        return 0
    fi

    log "Running ${init_script}..."

    export MONGO_INITDB_ROOT_USERNAME="${MONGO_ROOT_USERNAME}"
    export MONGO_INITDB_ROOT_PASSWORD="${MONGO_ROOT_PASSWORD}"
    export MONGO_INITDB_DATABASE="${MONGO_DATABASE}"

    export MONGO_REPLICA_SET
    export MONGO_PORT_1
    export MONGO_PORT_2
    export MONGO_PORT_3

    bash "${init_script}"
}

function print_status() {
    log "Replica-set status:"

    mongosh \
        --quiet \
        --host localhost \
        --port "${MONGO_PORT_1}" \
        --username "${MONGO_ROOT_USERNAME}" \
        --password "${MONGO_ROOT_PASSWORD}" \
        --authenticationDatabase admin \
        --eval '
            const status = rs.status();

            printjson(
                status.members.map(member => ({
                    id: member._id,
                    name: member.name,
                    health: member.health,
                    state: member.stateStr,
                    self: member.self ?? false,
                    uptime: member.uptime,
                    pingMs: member.pingMs ?? null,
                    lastHeartbeatMessage:
                        member.lastHeartbeatMessage ?? null
                }))
            );
        '
}

function monitor_members() {
    log "MongoDB replica set is ready."

    while true; do
        for member_number in 1 2 3; do
            local pid_file="${PID_ROOT}/member-${member_number}.pid"

            if [[ ! -f "${pid_file}" ]]; then
                log "Missing PID file for member ${member_number}."
                print_logs
                exit 1
            fi

            local pid
            pid="$(cat "${pid_file}")"

            if ! kill -0 "${pid}" 2>/dev/null; then
                log "MongoDB member ${member_number} stopped unexpectedly."
                print_logs
                exit 1
            fi
        done

        sleep 2
    done
}

prepare_directories

start_member 1 "${MONGO_PORT_1}"
start_member 2 "${MONGO_PORT_2}"
start_member 3 "${MONGO_PORT_3}"

wait_for_member_without_authentication "${MONGO_PORT_1}"
wait_for_member_without_authentication "${MONGO_PORT_2}"
wait_for_member_without_authentication "${MONGO_PORT_3}"

initiate_replica_set
wait_for_primary_without_authentication
create_root_user
wait_for_authenticated_connection
wait_for_all_members
run_optional_initialization_script
print_status
monitor_members
CONTAINER_SCRIPT

    chmod 755 "${MONGO_CONTAINER_SCRIPT}"
}

function start_container() {
    printgreen "Starting three MongoDB members in one container..."

    local docker_arguments=(
        run
        --quiet
        --rm
        --detach
        --name "${MONGO_CONTAINER}"
        --publish "${MONGO_PORT_1}:${MONGO_PORT_1}"
        --publish "${MONGO_PORT_2}:${MONGO_PORT_2}"
        --publish "${MONGO_PORT_3}:${MONGO_PORT_3}"
        --env "MONGO_REPLICA_SET=${MONGO_REPLICA_SET}"
        --env "MONGO_ROOT_USERNAME=${MONGO_ROOT_USERNAME}"
        --env "MONGO_ROOT_PASSWORD=${MONGO_ROOT_PASSWORD}"
        --env "MONGO_DATABASE=${MONGO_DATABASE}"
        --env "MONGO_PORT_1=${MONGO_PORT_1}"
        --env "MONGO_PORT_2=${MONGO_PORT_2}"
        --env "MONGO_PORT_3=${MONGO_PORT_3}"
        --volume "${MONGO_KEYFILE}:/run/mongodb-source/mongodb-keyfile:ro"
        --volume "${MONGO_CONTAINER_SCRIPT}:/usr/local/bin/start-mongodb-replica-set:ro"
    )
      
    docker_arguments+=(
        --entrypoint /usr/local/bin/start-mongodb-replica-set
        "${DOCKER_IMAGE}"
    )

    docker "${docker_arguments[@]}" >/dev/null
}

function wait_for_container() {
    printgreen "Waiting for the MongoDB replica set..."

    for attempt in $(seq 1 180); do
        if ! docker inspect \
            --format '{{.State.Running}}' \
            "${MONGO_CONTAINER}" \
            2>/dev/null | grep -qx "true"; then

            printred "The MongoDB container stopped unexpectedly."
            docker logs "${MONGO_CONTAINER}" || true
            return 1
        fi

        local healthy_members

        healthy_members="$(
            docker exec "${MONGO_CONTAINER}" mongosh \
                --quiet \
                --host localhost \
                --port "${MONGO_PORT_1}" \
                --username "${MONGO_ROOT_USERNAME}" \
                --password "${MONGO_ROOT_PASSWORD}" \
                --authenticationDatabase admin \
                --eval '
                    try {
                        const members = rs.status().members ?? [];

                        print(
                            members.filter(member =>
                                member.health === 1 &&
                                (
                                    member.stateStr === "PRIMARY" ||
                                    member.stateStr === "SECONDARY"
                                )
                            ).length
                        );
                    } catch (error) {
                        print(0);
                    }
                ' \
                2>/dev/null || printf "0"
        )"

        if [[ "${healthy_members}" == "3" ]]; then
            printgreen "All MongoDB replica-set members are healthy."
            return 0
        fi

        sleep 1
    done

    printred "The MongoDB replica set did not become ready."
    docker logs "${MONGO_CONTAINER}" || true
    return 1
}

function print_replica_set_status() {
    printgreen "MongoDB replica-set status:"

    docker exec "${MONGO_CONTAINER}" mongosh \
        --quiet \
        --host localhost \
        --port "${MONGO_PORT_1}" \
        --username "${MONGO_ROOT_USERNAME}" \
        --password "${MONGO_ROOT_PASSWORD}" \
        --authenticationDatabase admin \
        --eval '
            const status = rs.status();

            printjson(
                status.members.map(member => ({
                    id: member._id,
                    name: member.name,
                    health: member.health,
                    state: member.stateStr,
                    self: member.self ?? false,
                    uptime: member.uptime,
                    pingMs: member.pingMs ?? null,
                    lastHeartbeatMessage:
                        member.lastHeartbeatMessage ?? null
                }))
            );
        '
}

function runscript() {
    local javascript="$1"

    docker exec "${MONGO_CONTAINER}" mongosh \
        --quiet \
        --host localhost \
        --port "${MONGO_PORT_1}" \
        --username "${MONGO_ROOT_USERNAME}" \
        --password "${MONGO_ROOT_PASSWORD}" \
        --authenticationDatabase admin \
        "${MONGO_DATABASE}" \
        --eval "${javascript}"
}

function main() {
    remove_existing_container
    prepare_work_directory
    create_container_startup_script
    start_container
    wait_for_container
    print_replica_set_status

    # Startup succeeded, so do not run the failure cleanup.
    trap - EXIT
}

main "$@"
