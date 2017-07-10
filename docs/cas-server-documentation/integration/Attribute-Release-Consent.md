---
layout: default
title: CAS - Attribute Release Consent
---

# Attribute Consent

CAS provides the ability to enforce user-informed consent upon attribute release. Practically, this means that prior to accessing the target application, the user will be presented with a collection of attributes allowed to be released to the application with options to either proceed or deny the release of said attributes. There are also additional options to indicate how should underlying changes in the attribute release policy be considered by the consent engine. Users are also provied the ability to set up reminders in the event that no change is detected in the attribute release policy.

Consent attribute records stored in the configured repository are signed and encrypted.

Support is enabled by including the following module in the Overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-consent-webflow</artifactId>
     <version>${cas.version}</version>
</dependency>
```

## Storage

User consent decisions may be stored and remembered using one of the following options.

### JDBC

Support is enabled by including the following module in the Overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-consent-jdbc</artifactId>
     <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#jpa-attribute-consent).


### REST

Support is enabled by including the following module in the Overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-consent-rest</artifactId>
     <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#rest-attribute-consent).

### Custom

You may also inject your own implementation for attribute consent management into CAS that would itself handle storing consent decisions, etc. In order to do this, you will need to design a configuration class that roughly matches the following: 

```java
package org.apereo.cas.pm;

@Configuration("MyConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyConfiguration {

    @Bean
    public ConsentRepository consentRepository() {
        ...
    }
}
```

[See this guide](../installation/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.