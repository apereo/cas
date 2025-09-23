---
layout: default
title: CAS - SMS Messaging
category: Notifications
---

{% include variables.html %}

# Groovy SMS Messaging

Send text messages using an external Groovy script.

```groovy
import java.util.*

def run(Object[] args) {
    def (from,to,message,logger) = args
    logger.debug("Sending message ${message} to ${to} from ${from}")
    true
}
```

{% include_cached casproperties.html properties="cas.sms-provider.groovy" %}

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
