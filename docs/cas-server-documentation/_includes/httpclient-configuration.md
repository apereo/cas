Control how CAS should attempt to contact resources on the web
via its own Http Client. This is most commonly used when responding
to ticket validation events and/or single logout.

In the event that local certificates are to be imported into the CAS running environment,
a local truststore is provided by CAS to improve portability of configuration across environments.

```properties
# cas.http-client.connection-timeout=5000
# cas.http-client.async-timeout=5000
# cas.http-client.read-timeout=5000 

# cas.http-client.proxy-host=
# cas.http-client.proxy-port=0 

# cas.http-client.host-name-verifier=NONE|DEFAULT
# cas.http-client.allow-local-logout-urls=false
# cas.http-client.authority-validation-reg-ex=
# cas.http-client.authority-validation-reg-ex-case-sensitive=true

# cas.http-client.truststore.psw=changeit
# cas.http-client.truststore.file=classpath:/truststore.jks
# cas.http-client.truststore.type=
```

The default options are available for hostname verification:

| Type                    | Description
|-------------------------|--------------------------------------
| `NONE`                  | Ignore hostname verification.
| `DEFAULT`               | Enforce hostname verification.
