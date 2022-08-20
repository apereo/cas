---
layout: default
title: CAS - Securing Configuration Properties
category: Configuration
---

{% include variables.html %}

# Configuration Security - Spring Cloud

Securing CAS settings and decrypting them is entirely handled by
the [Spring Cloud](https://github.com/spring-cloud/spring-cloud-config) project
as [described in this guide](Configuration-Server-Management.html).

The Spring Cloud configuration server exposes `/encrypt` and `/decrypt` endpoints to support encrypting and decrypting values.
Both endpoints accept a `POST` payload; you can use `/encrypt` to secure and 
encrypt settings and place them inside your CAS configuration.
CAS will auto-decrypt at the appropriate moment.

{% include_cached casproperties.html
thirdPartyStartsWith="encrypt.key-store"
thirdPartyExactMatch="spring.cloud.config.server.encrypt"
%}

<div class="alert alert-info"><strong>JCE Requirements</strong><p>To use the encryption and decryption
features you need the full-strength "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files"
installed in your JVM version (if it’s not there by default).</p></div>

To encrypt a given setting, use:

```bash
curl -u casuser:Mellon https://config.server.endpoint/encrypt -d sensitiveValue
```

Then, copy the encrypted setting into your CAS configuration using the method specified below.

<div class="alert alert-info"><strong>URL Encoding</strong><p>Be careful with <code>curl</code>.
You may have to use <code>--data-urlencode</code> or set an explicit <code>Content-Type: text/plain</code>
to account for special characters such as <code>+</code>.</p></div>

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
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
```
