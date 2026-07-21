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

DOCKER_IMAGE="${DOCKER_IMAGE:-rabbitmq:4-management}"

RABBITMQ_NETWORK="${RABBITMQ_NETWORK:-rabbitmq-cluster}"
RABBITMQ_ERLANG_COOKIE="${RABBITMQ_ERLANG_COOKIE:-CASRABBITMQCLUSTERCOOKIE}"

RABBITMQ_USERNAME="${RABBITMQ_USERNAME:-rabbituser}"
RABBITMQ_PASSWORD="${RABBITMQ_PASSWORD:-bugsbunny}"
RABBITMQ_VHOST="${RABBITMQ_VHOST:-/}"

RABBITMQ_NODE_1="${RABBITMQ_NODE_1:-rabbitmq-server-1}"
RABBITMQ_NODE_2="${RABBITMQ_NODE_2:-rabbitmq-server-2}"
RABBITMQ_NODE_3="${RABBITMQ_NODE_3:-rabbitmq-server-3}"

RABBITMQ_HOST_1="${RABBITMQ_HOST_1:-rabbitmq-1}"
RABBITMQ_HOST_2="${RABBITMQ_HOST_2:-rabbitmq-2}"
RABBITMQ_HOST_3="${RABBITMQ_HOST_3:-rabbitmq-3}"

RABBITMQ_AMQP_PORT_1="${RABBITMQ_AMQP_PORT_1:-56721}"
RABBITMQ_AMQP_PORT_2="${RABBITMQ_AMQP_PORT_2:-56731}"
RABBITMQ_AMQP_PORT_3="${RABBITMQ_AMQP_PORT_3:-56741}"

RABBITMQ_MANAGEMENT_PORT_1="${RABBITMQ_MANAGEMENT_PORT_1:-25672}"
RABBITMQ_MANAGEMENT_PORT_2="${RABBITMQ_MANAGEMENT_PORT_2:-25673}"
RABBITMQ_MANAGEMENT_PORT_3="${RABBITMQ_MANAGEMENT_PORT_3:-25674}"

function remove_existing_cluster() {
    printgreen "Removing existing RabbitMQ containers..."

    docker rm -f \
        "${RABBITMQ_NODE_1}" \
        "${RABBITMQ_NODE_2}" \
        "${RABBITMQ_NODE_3}" \
        >/dev/null 2>&1 || true

    docker network rm "${RABBITMQ_NETWORK}" \
        >/dev/null 2>&1 || true

}

function create_network() {
    printgreen "Creating Docker network ${RABBITMQ_NETWORK}..."

    docker network create "${RABBITMQ_NETWORK}" >/dev/null
}

function start_node() {
    local container_name="$1"
    local hostname="$2"
    local amqp_port="$3"
    local management_port="$4"

    printgreen "Starting ${container_name}..."

    docker run \
        --detach \
        --name "${container_name}" \
        --hostname "${hostname}" \
        --network "${RABBITMQ_NETWORK}" \
        --publish "${amqp_port}:5672" \
        --publish "${management_port}:15672" \
        --env "RABBITMQ_NODENAME=rabbit@${hostname}" \
        --env "RABBITMQ_DEFAULT_USER=${RABBITMQ_USERNAME}" \
        --env "RABBITMQ_DEFAULT_PASS=${RABBITMQ_PASSWORD}" \
        --env "RABBITMQ_DEFAULT_VHOST=${RABBITMQ_VHOST}" \
        --user root \
        --entrypoint /bin/bash \
        "${DOCKER_IMAGE}" \
        -ec "
            mkdir -p /var/lib/rabbitmq
            printf '%s' '${RABBITMQ_ERLANG_COOKIE}' \
                > /var/lib/rabbitmq/.erlang.cookie
            chown rabbitmq:rabbitmq /var/lib/rabbitmq/.erlang.cookie
            chmod 400 /var/lib/rabbitmq/.erlang.cookie

            exec /usr/local/bin/docker-entrypoint.sh rabbitmq-server
        " \
        >/dev/null
}

function container_is_running() {
    local container_name="$1"

    [[ "$(
        docker inspect \
            --format '{{.State.Running}}' \
            "${container_name}" \
            2>/dev/null || true
    )" == "true" ]]
}

function print_container_failure() {
    local container_name="$1"

    local status
    local exit_code
    local error

    status="$(
        docker inspect \
            --format '{{.State.Status}}' \
            "${container_name}" \
            2>/dev/null || printf "unknown"
    )"

    exit_code="$(
        docker inspect \
            --format '{{.State.ExitCode}}' \
            "${container_name}" \
            2>/dev/null || printf "unknown"
    )"

    error="$(
        docker inspect \
            --format '{{.State.Error}}' \
            "${container_name}" \
            2>/dev/null || true
    )"

    printred "${container_name} stopped unexpectedly."
    printf "Container status: %s\n" "${status}"
    printf "Container exit code: %s\n" "${exit_code}"

    if [[ -n "${error}" ]]; then
        printf "Docker error: %s\n" "${error}"
    fi

    printf "\n===== %s logs =====\n" "${container_name}"
    docker logs "${container_name}" 2>&1 || true
}

function wait_for_node() {
    local container_name="$1"

    printgreen "Waiting for ${container_name}..."

    for attempt in $(seq 1 120); do
        if ! docker inspect "${container_name}" >/dev/null 2>&1; then
            printred "Container ${container_name} does not exist."
            return 1
        fi

        if ! container_is_running "${container_name}"; then
            print_container_failure "${container_name}"
            return 1
        fi

        if docker exec "${container_name}" \
            rabbitmq-diagnostics -q ping \
            >/dev/null 2>&1; then

            if docker exec "${container_name}" \
                rabbitmqctl await_startup \
                >/dev/null 2>&1; then

                printgreen "${container_name} is ready."
                return 0
            fi
        fi

        sleep 1
    done

    printred "${container_name} did not become ready."
    docker logs "${container_name}" 2>&1 || true
    return 1
}

function join_node_to_cluster() {
    local container_name="$1"

    printgreen "Joining ${container_name} to rabbit@${RABBITMQ_HOST_1}..."

    docker exec "${container_name}" rabbitmqctl stop_app
    docker exec "${container_name}" rabbitmqctl reset
    docker exec "${container_name}" \
        rabbitmqctl join_cluster "rabbit@${RABBITMQ_HOST_1}"
    docker exec "${container_name}" rabbitmqctl start_app
}

function wait_for_cluster_size() {
    local expected_size="$1"

    printgreen "Waiting for ${expected_size} RabbitMQ cluster members..."

    for attempt in $(seq 1 120); do
        local member_count

        member_count="$(
            docker exec "${RABBITMQ_NODE_1}" \
                rabbitmqctl -q cluster_status \
                --formatter json \
                2>/dev/null |
                tr -d '\n' |
                grep -o '"rabbit@[^"]*"' |
                sort -u |
                wc -l |
                tr -d ' '
        )"

        if [[ "${member_count}" == "${expected_size}" ]]; then
            printgreen "RabbitMQ cluster contains ${expected_size} members."
            return 0
        fi

        sleep 1
    done

    printred "RabbitMQ cluster did not reach ${expected_size} members."
    docker exec "${RABBITMQ_NODE_1}" rabbitmqctl cluster_status || true
    return 1
}

function wait_for_all_nodes_running() {
    printgreen "Waiting for all RabbitMQ nodes to report as running..."

    for attempt in $(seq 1 120); do
        local output

        output="$(
            docker exec "${RABBITMQ_NODE_1}" \
                rabbitmqctl -q cluster_status \
                --formatter json \
                2>/dev/null || true
        )"

        if printf "%s" "${output}" |
            grep -q "rabbit@${RABBITMQ_HOST_1}" &&
            printf "%s" "${output}" |
                grep -q "rabbit@${RABBITMQ_HOST_2}" &&
            printf "%s" "${output}" |
                grep -q "rabbit@${RABBITMQ_HOST_3}"; then

            if docker exec "${RABBITMQ_NODE_1}" rabbitmq-diagnostics -q ping \
                    >/dev/null 2>&1 &&
                docker exec "${RABBITMQ_NODE_2}" rabbitmq-diagnostics -q ping \
                    >/dev/null 2>&1 &&
                docker exec "${RABBITMQ_NODE_3}" rabbitmq-diagnostics -q ping \
                    >/dev/null 2>&1; then

                printgreen "All RabbitMQ cluster members are running."
                return 0
            fi
        fi

        sleep 1
    done

    printred "Not all RabbitMQ nodes became healthy."
    return 1
}

function verify_management_api() {
    printgreen "Verifying RabbitMQ management API..."

    for attempt in $(seq 1 60); do
        if curl \
            --silent \
            --fail \
            --user "${RABBITMQ_USERNAME}:${RABBITMQ_PASSWORD}" \
            "http://localhost:${RABBITMQ_MANAGEMENT_PORT_1}/api/nodes" \
            >/dev/null; then

            printgreen "RabbitMQ management API is ready."
            return 0
        fi

        sleep 1
    done

    printred "RabbitMQ management API did not become ready."
    return 1
}

function print_cluster_status() {
    printgreen "RabbitMQ cluster status:"

    docker exec "${RABBITMQ_NODE_1}" \
        rabbitmqctl cluster_status
}

function print_cluster_nodes() {
    printgreen "RabbitMQ cluster nodes:"

    curl \
        --silent \
        --fail \
        --user "${RABBITMQ_USERNAME}:${RABBITMQ_PASSWORD}" \
        "http://localhost:${RABBITMQ_MANAGEMENT_PORT_1}/api/nodes" |
        docker run \
            --rm \
            --interactive \
            ghcr.io/jqlang/jq:latest \
            'map({
                name,
                type,
                running,
                mem_alarm,
                disk_free_alarm,
                partitions
            })' 2>/dev/null || true
}

function print_connection_information() {
    printf "\n"
    printgreen "RabbitMQ cluster is ready."

    printf "\nAMQP endpoints:\n\n"
    printf "amqp://%s:%s@localhost:%s\n" \
        "${RABBITMQ_USERNAME}" \
        "${RABBITMQ_PASSWORD}" \
        "${RABBITMQ_AMQP_PORT_1}"

    printf "amqp://%s:%s@localhost:%s\n" \
        "${RABBITMQ_USERNAME}" \
        "${RABBITMQ_PASSWORD}" \
        "${RABBITMQ_AMQP_PORT_2}"

    printf "amqp://%s:%s@localhost:%s\n" \
        "${RABBITMQ_USERNAME}" \
        "${RABBITMQ_PASSWORD}" \
        "${RABBITMQ_AMQP_PORT_3}"

    printf "\nManagement interfaces:\n\n"
    printf "http://localhost:%s\n" "${RABBITMQ_MANAGEMENT_PORT_1}"
    printf "http://localhost:%s\n" "${RABBITMQ_MANAGEMENT_PORT_2}"
    printf "http://localhost:%s\n" "${RABBITMQ_MANAGEMENT_PORT_3}"

    printf "\nManagement API nodes endpoint:\n\n"
    printf "http://localhost:%s/api/nodes\n" \
        "${RABBITMQ_MANAGEMENT_PORT_1}"

    printf "\nUsername: %s\n" "${RABBITMQ_USERNAME}"
    printf "Password: %s\n" "${RABBITMQ_PASSWORD}"

    printf "\nSpring Boot configuration:\n\n"
    printf "spring.rabbitmq.addresses=localhost:%s,localhost:%s,localhost:%s\n" \
        "${RABBITMQ_AMQP_PORT_1}" \
        "${RABBITMQ_AMQP_PORT_2}" \
        "${RABBITMQ_AMQP_PORT_3}"

    printf "spring.rabbitmq.username=%s\n" "${RABBITMQ_USERNAME}"
    printf "spring.rabbitmq.password=%s\n" "${RABBITMQ_PASSWORD}"
    printf "spring.rabbitmq.virtual-host=%s\n" "${RABBITMQ_VHOST}"

    printf "\nStop the cluster with:\n\n"
    printf "docker rm -f %s %s %s\n" \
        "${RABBITMQ_NODE_1}" \
        "${RABBITMQ_NODE_2}" \
        "${RABBITMQ_NODE_3}"

    printf "\n"
}

function print_logs_on_failure() {
    local exit_code=$?

    if [[ "${exit_code}" -ne 0 ]]; then
        printred "RabbitMQ cluster startup failed."

        for container_name in \
            "${RABBITMQ_NODE_1}" \
            "${RABBITMQ_NODE_2}" \
            "${RABBITMQ_NODE_3}"; do

            printf "\n===== %s =====\n" "${container_name}"
            docker logs "${container_name}" 2>/dev/null || true
        done
    fi

    exit "${exit_code}"
}

function main() {
    trap print_logs_on_failure EXIT

    remove_existing_cluster
    create_network

    start_node \
        "${RABBITMQ_NODE_1}" \
        "${RABBITMQ_HOST_1}" \
        "${RABBITMQ_AMQP_PORT_1}" \
        "${RABBITMQ_MANAGEMENT_PORT_1}"

    start_node \
        "${RABBITMQ_NODE_2}" \
        "${RABBITMQ_HOST_2}" \
        "${RABBITMQ_AMQP_PORT_2}" \
        "${RABBITMQ_MANAGEMENT_PORT_2}"

    start_node \
        "${RABBITMQ_NODE_3}" \
        "${RABBITMQ_HOST_3}" \
        "${RABBITMQ_AMQP_PORT_3}" \
        "${RABBITMQ_MANAGEMENT_PORT_3}"

    wait_for_node "${RABBITMQ_NODE_1}"
    wait_for_node "${RABBITMQ_NODE_2}"
    wait_for_node "${RABBITMQ_NODE_3}"

    join_node_to_cluster "${RABBITMQ_NODE_2}"
    join_node_to_cluster "${RABBITMQ_NODE_3}"

    wait_for_cluster_size 3
    wait_for_all_nodes_running
    verify_management_api

    print_cluster_status
    print_cluster_nodes
#    print_connection_information

    trap - EXIT
}

main "$@"
