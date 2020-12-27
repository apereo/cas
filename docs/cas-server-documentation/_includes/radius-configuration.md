### RADIUS Configuration

The following options related to RADIUS support in CAS apply equally to a number of CAS components (authentication, etc):

`server` parameters defines identification values of authenticated service (CAS server), primarily `server.protocol`
for communication to RADIUS server identified by `client`.

`client` parameters defines values for connecting RADIUS server.

Parameter `client.inet-address` has possibility to contain more addresses 
separated by comma to define failover servers when `failover-on-exception` is set.

```properties
# {{ include.configKey }}.server.nas-port-id=-1
# {{ include.configKey }}.server.nas-real-port=-1
# {{ include.configKey }}.server.protocol=EAP_MSCHAPv2
# {{ include.configKey }}.server.retries=3
# {{ include.configKey }}.server.nas-port-type=-1
# {{ include.configKey }}.server.nas-port=-1
# {{ include.configKey }}.server.nas-ip-address=
# {{ include.configKey }}.server.nas-ipv6-address=
# {{ include.configKey }}.server.nas-identifier=-1
# {{ include.configKey }}.client.authentication-port=1812
# {{ include.configKey }}.client.shared-secret=N0Sh@ar3d$ecReT
# {{ include.configKey }}.client.socket-timeout=0
# {{ include.configKey }}.client.inet-address=localhost
# {{ include.configKey }}.client.accounting-port=1813
# {{ include.configKey }}.failover-on-exception=false
# {{ include.configKey }}.failover-on-authentication-failure=false
```
