---
layout: default
title: CAS - YubiKey Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Custom YubiKey Registration

If you wish to plug in a custom registry implementation that would determine
which users are allowed to use their YubiKey accounts for authentication, you 
may plug in a custom implementation of the `YubiKeyAccountRegistry` that 
allows you to provide a mapping between usernames and YubiKey public keys.


```java
package org.apereo.cas.support.yubikey;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyYubiKeyConfiguration {

  @Bean
  public YubiKeyAccountRegistry yubiKeyAccountRegistry() {
      ...
  }
}
```
