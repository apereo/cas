---
layout: default
title: CAS - U2F - FIDO Universal 2nd Factor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Groovy U2F - FIDO Universal Registration

Device registrations may be managed via an external Groovy script. The script may be designed as follows:

{% include casproperties.html properties="cas.authn.mfa.u2f.groovy" %}

```groovy
import java.util.*
import org.apereo.cas.adaptors.u2f.storage.*

Map<String, List<U2FDeviceRegistration>> read(final Object... args) {
    def logger = args[0]
    ...
    null
}

Boolean write(final Object... args) {
    List<U2FDeviceRegistration> list = args[0]
    def logger = args[1]
    ...
    true
}

void removeAll(final Object... args) {
    def logger = args[0]
}          

def remove(Object[] args) {
    def device = args[0]
    def logger = args[1]
}
```
