---
layout: default
title: CAS - YubiKey Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# YubiKey Authentication

Yubico is a cloud-based service that enables strong, easy-to-use and affordable 
two-factor authentication with one-time passwords through their 
flagship product, YubiKey. Once Yubico `client-id` and `secret-key` 
are obtained, then the configuration options available to 
use YubiKey devices as a primary authentication 
source that CAS server could use to authenticate users.

To configure YubiKey accounts and obtain API keys, [refer to the documentation](https://upgrade.yubico.com/getapikey/).

[YubiKey](https://www.yubico.com/products/yubikey-hardware) authentication 
components are enabled by including the following dependencies in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-yubikey" %}

## Actuator Endpoints
             
The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="yubikeyAccountRepository" %}

## Configuration

{% include_cached casproperties.html properties="cas.authn.mfa.yubikey" %}

By default, all YubiKey accounts for users are allowed to authenticate. Devices that 
need to be authorized for authentication need to have followed an out-of-band 
registration process where the record for them is found in one of the following 
storage backends. Upon authentication, CAS will begin to search the configured 
registration database for matching record for the authenticated user and device 
in order to allow for a successful authentication event.

| Storage          | Description                                         
|------------------------------------------------------------------------------------
| JSON              | [See this guide](YubiKey-Authentication-Registration-JSON.html).
| REST              | [See this guide](YubiKey-Authentication-Registration-Rest.html).
| Permissive        | [See this guide](YubiKey-Authentication-Registration-Permissive.html).
| JPA               | [See this guide](YubiKey-Authentication-Registration-JPA.html).
| CouchDb           | [See this guide](YubiKey-Authentication-Registration-CouchDb.html).
| Redis             | [See this guide](YubiKey-Authentication-Registration-Redis.html).
| DynamoDb          | [See this guide](YubiKey-Authentication-Registration-DynamoDb.html).
| MongoDb           | [See this guide](YubiKey-Authentication-Registration-MongoDb.html).
| Custom            | [See this guide](YubiKey-Authentication-Registration-Custom.html).

## Device/Account Validation

In the event that a new YubiKey should be registered, it may be desirable to 
execute additional validation processes before the account is registered with 
the underlying store. By default, the device registration step only verifies 
the device token. If you wish to extend this behavior, you can design your 
own validator that cross-checks the account against alternative 
sources and databases for validity and authorization:

```java
package org.apereo.cas.support.yubikey;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyYubiKeyConfiguration {

  @Bean
  public YubiKeyAccountValidator yubiKeyAccountValidator() {
      ...
  }
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more 
about how to register configurations into the CAS runtime.

## REST Protocol Credential Extraction 

In the event that the [CAS REST Protocol](../protocol/REST-Protocol.html) is turned on, 
a special credential extractor is injected into the REST authentication engine in 
order to recognize YubiKey credentials and authenticate them as part of the REST 
request. The expected parameter name in the request body is `yubikeyotp`.
