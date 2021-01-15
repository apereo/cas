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

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-yubikey" %}

## Administrative Endpoints

The following endpoints are provided by CAS:
 
| Endpoint                 | Description
|--------------------------|------------------------------------------------
| `yubikeyAccountRepository`    | Manage and control [Google Authenticator account records](YubiKey-Authentication.html). A `GET` operation produces a list of all account records. A `DELETE` operation will delete all account records. A `GET` operation produces with a parameter selector of `/{username}` will list the record assigned to the user. A `DELETE` operation produces with a parameter selector of `/{username}` will remove the record assigned to the user.

## Configuration

{% include casproperties.html
modules="cas-server-support-yubikey"
properties="cas.authn.mfa.yubikey.bypass,cas.authn.mfa.yubikey.crypto" %}

By default, all YubiKey accounts for users are allowed to authenticate. Devices that 
need to be authorized for authentication need to have followed an out-of-band 
registration process where the record for them is found in one of the following 
storage backends. Upon authentication, CAS will begin to search the configured 
registration database for matching record for the authenticated user and device 
in order to allow for a successful authentication event.

### JSON

Please [see this guide](YubiKey-Authentication-Registration-JSON.html) for more info.


### REST

Please [see this guide](YubiKey-Authentication-Registration-Rest.html) for more info.

### Permissive

Please [see this guide](YubiKey-Authentication-Registration-Permissive.html) for more info.

### JPA

Please [see this guide](YubiKey-Authentication-Registration-JPA.html) for more info.

### CouchDb

Please [see this guide](YubiKey-Authentication-Registration-CouchDb.html) for more info.

### Redis

Please [see this guide](YubiKey-Authentication-Registration-Redis.html) for more info.

### DynamoDb

Please [see this guide](YubiKey-Authentication-Registration-DynamoDb.html) for more info.

### MongoDb

Please [see this guide](YubiKey-Authentication-Registration-MongoDb.html) for more info.


### Custom

Please [see this guide](YubiKey-Authentication-Registration-Custom.html) for more info.

## Device/Account Validation

In the event that a new YubiKey should be registered, it may be desirable to 
execute additional validation processes before the account is registered with 
the underlying store. By default, the device registration step only verifies 
the device token. If you wish to extend this behavior, you can design your 
own validator that cross-checks the account against alternative 
sources and databases for validity and authorization:

```java
package org.apereo.cas.support.yubikey;

@Configuration("myYubiKeyConfiguration")
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
