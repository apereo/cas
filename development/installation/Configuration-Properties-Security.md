---
layout: default
title: CAS - Securing Configuration Properties
---

# Configuration Encryption

The CAS configuration server exposes the `/encrypt` and `/decrypt` endpoints to allow for encrypting and decrypting values.
You can send a `POST` message to these endpoints to secure keys. CAS will auto-decrypt at the appropriate moment.

To see the relevant list of CAS properties for this feature, please [review this guide](Configuration-Properties.html).
Securing CAS settings and decrypting them is entirely handled by the [Spring Cloud](https://github.com/spring-cloud/spring-cloud-config) project.

<div class="alert alert-warning"><strong>JCE Requirements</strong><p>to use the encryption and decryption 
features you need the full-strength "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files" 
installed in your JVM version (if it’s not there by default).</p></div>

To encrypt a given setting, use:

```bash
curl https://sso.example.org/cas/configserver/encrypt -d sensitiveValue
```

Then, copy the encrypted setting into your CAS configuration using the method specified below.

<div class="alert alert-warning"><strong>URL Encoding</strong><p>Be careful with <code>curl</code>.
You may have to use <code>--data-urlencode</code> or set an explicit <code>Content-Type: text/plain</code>
to account for special characters such as <code>+</code>.</p></div>

If you wish to manually encrypt and decrypt settings to ensure the functionality is sane, use:

```bash
export ENCRYPTED=`curl https://sso.example.org/cas/configserver/encrypt -d sensitiveValue | python -c 'import sys,urllib;print urllib.quote(sys.stdin.read().strip())'`
echo $ENCRYPTED
curl https//sso.exampple.org/cas/configserver/decrypt -d $ENCRYPTED | python -c 'import sys,urllib;print urllib.quote(sys.stdin.read().strip())'
```

Properties that are prefixed with `{cipher}` are automatically decrypted by the CAS configuration server at runtime, such as:

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

You can safely push this plain text to a shared git repository and the secret password is protected.
