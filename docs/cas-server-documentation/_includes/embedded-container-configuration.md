The following properties are related to the embedded containers that ship with CAS.

```properties
server.servlet.context-path=/cas
server.port=8443
server.ssl.key-store=file:/etc/cas/thekeystore
server.ssl.key-store-password=changeit
server.ssl.key-password=changeit

# server.ssl.enabled=true
# server.ssl.ciphers=
# server.ssl.key-alias=
# server.ssl.key-store-provider=
# server.ssl.key-store-type=
# server.ssl.protocol=

# server.max-http-header-size=2097152
# server.use-forward-headers=true
# server.connection-timeout=20000
```

You may also control the CAS web application session behavior as it's treated 
by the underlying servlet container engine.

```properties
# server.servlet.session.timeout=PT30S
# server.servlet.session.cookie.http-only=true
# server.servlet.session.tracking-modes=COOKIE
```
