#!/bin/bash
# Starts etcd and seeds the settings CAS reads at startup under /cas/config/default (EtcdPropertySource).
"${PWD}/ci/tests/httpbin/run-httpbin-server.sh" || return 1
"${PWD}/ci/tests/etcd/run-etcd-server.sh" || return 1

echo "Waiting for etcd to get ready..."
for _ in $(seq 1 30); do
  if docker exec etcd-server etcdctl endpoint health >/dev/null 2>&1; then
    break
  fi
  sleep 1
done
docker exec etcd-server etcdctl endpoint health || return 1

docker exec etcd-server etcdctl put /cas/config/default/cas.authn.accept.users "etcduser::Mellon" || return 1
docker exec etcd-server etcdctl put /cas/config/default/cas.logout.follow-service-redirects "false" || return 1
docker exec etcd-server etcdctl get --prefix /cas/config
echo "etcd is ready"
