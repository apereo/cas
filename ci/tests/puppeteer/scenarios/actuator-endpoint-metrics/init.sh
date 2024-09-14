#!/bin/bash

echo "Running Prometheus docker container..."
docker stop prom-server || true && docker rm prom-server || true
docker run --rm -d -p 9090:9090 --name "prom-server" \
  -v "${SCENARIO_FOLDER}/prometheus.yml":/etc/prometheus/prometheus.yml \
  prom/prometheus
sleep 1
docker ps | grep "prom-server"
retVal=$?
if [ $retVal == 0 ]; then
    PROM_SERVER_IP=$(docker inspect prom-server | jq '.[].NetworkSettings.Networks.bridge.IPAddress')
    PROM_SERVER_IP=`sed -e 's/^"//' -e 's/"$//' <<<"$PROM_SERVER_IP"`
    echo "Prometheus docker container is running on ${PROM_SERVER_IP}"
else
    echo "Prometheus docker container failed to start."
    exit $retVal
fi

echo "Running Grafana docker container..."
docker stop grafana-server || true && docker rm grafana-server || true
docker run --rm -d -p 3000:3000 --name "grafana-server" \
  -e GF_SECURITY_ADMIN_PASSWORD=admin \
  -e GF_SERVER_DOMAIN=localhost \
  -e GF_INSTALL_PLUGINS=grafana-clock-panel,grafana-simple-json-datasource \
  -e "GF_LOG_MODE=console file" \
  grafana/grafana-oss
sleep 20
docker ps | grep "grafana-server"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Grafana docker container is running"
else
    echo "Grafana docker container failed to start"
    exit $retVal
fi

echo "Setting up Grafana data source"
generate_post_data()
{
  cat <<EOF
{
  "id": 1,
  "uid": "7VAxQRY4z",
  "orgId": 1,
  "name": "Prometheus",
  "type": "prometheus",
  "typeName": "Prometheus",
  "typeLogoUrl": "public/app/plugins/datasource/prometheus/img/prometheus_logo.svg",
  "access": "proxy",
  "url": "http://$PROM_SERVER_IP:9090",
  "basicAuth": false,
  "isDefault": true,
  "jsonData": {
    "httpMethod": "POST",
    "timeout": 5
  },
  "readOnly": false
}
EOF
}

generate_post_data
curl -v -X POST http://admin:admin@localhost:3000/api/datasources \
  -H 'Content-Type: application/json' \
  -d "$(generate_post_data)"
retVal=$?
if [ $retVal == 0 ]; then
    echo "Grafana data source setup failed"
    exit $retVal
fi
