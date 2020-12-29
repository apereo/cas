The Apache Tomcat `SSLValve` is a way to get a client certificate from an SSL proxy (e.g. HAProxy or BigIP F5)
running in front of Tomcat via an HTTP header. If you enable this, make sure your proxy is ensuring
that this header does not originate with the client (e.g. the browser).

```properties
# cas.server.tomcat.ssl-valve.enabled=false
# cas.server.tomcat.ssl-valve.ssl-client-cert-header=ssl_client_cert
# cas.server.tomcat.ssl-valve.ssl-cipher-header=ssl_cipher
# cas.server.tomcat.ssl-valve.ssl-session-id-header=ssl_session_id
# cas.server.tomcat.ssl-valve.ssl-cipher-user-key-size-header=ssl_cipher_usekeysize
```

Example HAProxy Configuration (snippet): Configure SSL frontend
with cert optional, redirect to cas, if cert provided, put it on header.

```
frontend web-vip
  bind 192.168.2.10:443 ssl crt /var/lib/haproxy/certs/www.example.com.pem ca-file /var/lib/haproxy/certs/ca.pem verify optional
  mode http
  acl www-cert ssl_fc_sni if { www.example.com }
  acl empty-path path /
  http-request redirect location /cas/ if empty-path www-cert
  http-request del-header ssl_client_cert unless { ssl_fc_has_crt }
  http-request set-header ssl_client_cert -----BEGIN\ CERTIFICATE-----\ %[ssl_c_der,base64]\ -----END\ CERTIFICATE-----\  if { ssl_fc_has_crt }
  acl cas-path path_beg -i /cas
  reqadd X-Forwarded-Proto:\ https
  use_backend cas-pool if cas-path

backend cas-pool
  option httpclose
  option forwardfor
  cookie SERVERID-cas insert indirect nocache
  server cas-1 192.168.2.10:8080 check cookie cas-1
```
