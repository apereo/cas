---
layout: default
title: CAS - SMS Messaging
---

# SMS Messaging

CAS presents the ability to notify users on select actions via SMS messaging. Example actions include notification of risky authentication attempts
or password reset links/tokens. SMS providers supported by CAS are listed below. Note that an active/professional subscription may be required for certain
providers.

## Twillio

To learn more, [visit this site](https://www.twilio.com/).

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-sms-twillio</artifactId>
    <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#twillio).

## TextMagic

To learn more, [visit this site](https://www.textmagic.com/).

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-sms-textmagic</artifactId>
    <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#textmagic).

## Clickatell

To learn more, [visit this site](http://www.clickatell.com/).

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-sms-clickatell</artifactId>
    <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#clickatell).
