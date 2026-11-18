# Configuration Security - Spring Cloud

Source: https://apereo.github.io/cas/development/configuration/Configuration-Properties-Security-SpringCloud.html

Securing CAS settings and decrypting them is entirely handled by the [Spring Cloud](https://github.com/spring-cloud/spring-cloud-config) project as [described in this guide](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management.html).

The Spring Cloud configuration server exposes `/encrypt` and `/decrypt` endpoints to support encrypting and decrypting values. Both endpoints accept a `POST` payload; you can use `/encrypt` to secure and encrypt settings and place them inside your CAS configuration. CAS will auto-decrypt at the appropriate moment.

The following settings and properties are available from the CAS configuration catalog:

- `encrypt.key-store.alias` (Third party; type `String`): Alias for a key in the store.
- `encrypt.key-store.location` (Third party; type `Resource`): Location of the key store file, e.g. classpath:/keystore.jks.
- `encrypt.key-store.password` (Third party; type `String`): Password that locks the keystore.
- `encrypt.key-store.secret` (Third party; type `String`): Secret protecting the key (defaults to the same as the password).
- `encrypt.key-store.type` (Third party; type `String`; default `jks`): The KeyStore type. Defaults to jks.

> **JCE Requirements**
>
> To use the encryption and decryption features you need the full-strength "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files" installed in your JVM version (if it’s not there by default).

To encrypt a given setting, use:

```bash
curl -u casuser:Mellon https://config.server.endpoint/encrypt -d sensitiveValue
```

Then, copy the encrypted setting into your CAS configuration using the method specified below.

> **URL Encoding**
>
> Be careful with `curl`. You may have to use `--data-urlencode` or set an explicit `Content-Type: text/plain` to account for special characters such as `+`.

If you wish to manually encrypt and decrypt settings to ensure the functionality is sane, use:

```bash
export ENCRYPTED=`curl -u casuser:Mellon https://config.server.endpoint/encrypt \
    -d sensitiveValue | python -c 'import sys,urllib;print urllib.quote(sys.stdin.read().strip())'`
echo $ENCRYPTED
curl -u casuser:Mellon https://config.server.endpoint/decrypt \
    -d $ENCRYPTED | python -c 'import sys,urllib;print urllib.quote(sys.stdin.read().strip())'
```

Properties that are prefixed with `{cipher}` are automatically decrypted by the Spring Cloud configuration server at runtime, such as:

```yml
cas
    something
        sensitive: '{cipher}FKSAJDFGYOS8F7GLHAKERGFHLSAJ'
```

Or:

```properties
# Note that there are no quotes around the value!
cas.something.sensitive={cipher}FKSAJDFGYOS8F7GLHAKERGFHLSAJ
```

# Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.springframework.cloud" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```
