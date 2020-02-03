---
layout: default
title: CAS - Multiphase Authentication
category: Authentication
---

# Multiphase Authentication

Multiphase Authentication describes an authentication flow wherein users are
required to enter an identifier; i.e., their username, before being prompted to
enter the corresponding password. This may be desirable in situations where
some users will authenticate against a 
[delegated authentication provider](../integration/Delegate-Authentication.html),
but it is not acceptable to present available delegation options on the main 
login page, or where you wish to make use of
[Passwordless](../installation/Passwordless-Authentication.html) or
[Graphical](../installation/GUA-Authentication.html) authentication.

## Overview

Support is enabled by including the following module in the overlay:

```xml
<dependency>
	<groupId>org.apereo.cas</groupId>
	<artifactId>cas-server-support-multiphase-webflow</artifactId>
	<version>${cas.version}</version>
</dependency>
```
## Webflow Event Resolution

TODO
