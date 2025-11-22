#!/usr/bin/env sh
set -e

# --- Bitnami-like env semantics -------------------------------------------

# Name of the master "set" (cluster name)
MASTER_SET="${REDIS_MASTER_SET:-mymaster}"

# Master address Sentinel should track
MASTER_HOST="${REDIS_MASTER_HOST:-localhost}"
MASTER_PORT="${REDIS_MASTER_PORT_NUMBER:-6379}"

# Sentinel quorum
SENTINEL_QUORUM="${REDIS_SENTINEL_QUORUM:-2}"

# Password FOR CLIENTS TALKING TO SENTINEL (requirepass)
SENTINEL_PASSWORD="${REDIS_SENTINEL_PASSWORD:-}"

# Password Sentinel uses to talk to the Redis master
# (Bitnami calls this REDIS_MASTER_PASSWORD now, but your legacy
#  setup used REDIS_PASSWORD, so we support both.)
MASTER_PASSWORD="${REDIS_MASTER_PASSWORD:-$REDIS_PASSWORD}"

CONF_DIR="/usr/local/etc/redis"
CONF_FILE="${CONF_DIR}/sentinel.conf"

mkdir -p "${CONF_DIR}"

# --- Build sentinel.conf ---------------------------------------------------

{
  echo "port 26379"
  echo "dir /tmp"

  # Make Sentinel return hostnames such as 'localhost' instead of IPs,
  # like Bitnami does by default.
  echo "sentinel resolve-hostnames yes"

  # Require password to talk to Sentinel if configured
  if [ -n "${SENTINEL_PASSWORD}" ]; then
    echo "requirepass ${SENTINEL_PASSWORD}"
  fi

  # Monitor the master
  echo "sentinel monitor ${MASTER_SET} ${MASTER_HOST} ${MASTER_PORT} ${SENTINEL_QUORUM}"

  # If a master password is configured, use it
  if [ -n "${MASTER_PASSWORD}" ]; then
    echo "sentinel auth-pass ${MASTER_SET} ${MASTER_PASSWORD}"
  fi

  # Reasonable defaults (very close to Bitnami’s)
  echo "sentinel down-after-milliseconds ${MASTER_SET} 5000"
  echo "sentinel failover-timeout ${MASTER_SET} 60000"
  echo "sentinel parallel-syncs ${MASTER_SET} 1"
} > "${CONF_FILE}"

# --- Run Redis in sentinel mode -------------------------------------------

exec redis-server "${CONF_FILE}" --sentinel
