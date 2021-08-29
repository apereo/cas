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
    def from = args[0]
    def to = args[1]
    def message = args[2]
    def logger = args[3]

    logger.debug("Sending message ${message} to ${to} from ${from}")
    true
}
```

{% include_cached casproperties.html properties="cas.sms-provider.groovy" %}

