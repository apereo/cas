---
layout: default
title: CAS - Securing Configuration Properties
category: Configuration
---

{% include variables.html %}

# Configuration Security - Vault

You can also store sensitive settings inside [Vault](https://www.vaultproject.io/).
Vault can store your existing secrets, or it can dynamically generate new secrets
to control access to third-party resources or provide time-limited credentials for your infrastructure.
To learn more about Vault and its installation process, please visit the project website.

Once vault is accessible and configured inside CAS, support is provided via the following dependency:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-configuration-cloud-vault" %}

{% include_cached casproperties.html
thirdPartyStartsWith="spring.cloud.vault"
%}

With CAS, secrets are picked up at startup of the application server. CAS uses the data and settings
from the application name (i.e. `cas`) and active profiles to determine contexts paths in
which secrets should be stored and later fetched.

These context paths typically are:

```bash
/secret/{application}/{profile}
/secret/{application}
```

As an example, you may write the following CAS setting to Vault:

```bash
vault write secret/cas/native <setting-name>=<value>
```

CAS will execute the equivalent of the following command to read settings later when needed:

```bash
vault read secret/cas/native
```

All settings and secrets that are stored inside Vault may be reloaded at any given time.
To learn more about how CAS allows you to reload
configuration changes, please [review this guide](Configuration-Management-Reload.html).
To learn more about how configuration is managed and profiled
by CAS, please [review this guide](Configuration-Management.html).

# Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.springframework.cloud.vault" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
```

