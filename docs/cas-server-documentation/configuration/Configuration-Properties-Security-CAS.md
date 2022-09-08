---
layout: default
title: CAS - Securing Configuration Properties
category: Configuration
---

{% include variables.html %}

# Configuration Security - CAS

If you are running CAS in standalone mode without the presence of the configuration server,
you can take advantage of built-in [Jasypt](http://www.jasypt.org/) functionality to decrypt 
sensitive CAS settings. Configuration security specified here should apply to all configuration 
files and settings loaded by CAS in all supported formats (i.e. `properties`, `yaml`, `yml`).

Jasypt supplies command-line tools useful for performing encryption, decryption, etc. In 
order to use the tools, you should download the Jasypt distribution. Once unzipped, you will find a `jasypt-$VERSION/bin` 
directory a number of `bat|sh` scripts that you can use for encryption/decryption operations `(encrypt|decrypt).(bat|sh)`.

Encrypted settings need to be placed into CAS configuration files as:

```properties
cas.something.sensitive={cas-cipher}FKSAJDFGYOS8F7GLHAKERGFHLSAJ
```

You also need to instruct CAS to use the proper algorithm, decryption key and other relevant parameters
when attempting to decrypt settings. 
   
{% include_cached casproperties.html properties="cas.standalone.configuration-security" %}

<div class="alert alert-info">
<strong>Usage</strong><br/>The above settings may be passed to CAS at runtime using either OS 
environment variables, system properties or normal command-line arguments. Placing them in a CAS-owned configuration file
will likely result in a dysfunctional setup. The encryption/decryption facade is put together early in the bootstrapping 
process before CAS has had a chance to load any configuration files. So bootstrapping the encryptor/decryptor components 
must happen at runtime so CAS gets a chance to initialize the right set of components before any configuration file can be loaded.
</div>

Encryption and decryption support may also be used inside the [CAS Command-line Shell](../installation/Configuring-Commandline-Shell.html).

# Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.apereo.cas.configuration" level="trace" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
```
