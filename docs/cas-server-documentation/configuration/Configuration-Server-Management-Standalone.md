---
layout: default
title: CAS - Configuration Server
category: Configuration
---

{% include variables.html %}

# Configuration Server - Standalone Profile

This is the default configuration mode which indicates that CAS does **NOT** require connections to an external configuration server
and will run in an embedded *standalone mode*. When this option is turned on, CAS by default will attempt to locate settings and properties
inside a pre-defined directories and files and otherwise falls back to typically using `/etc/cas/config` as the configuration directory.

Similar to the Spring Cloud external configuration server, the contents of this directory include `(cas|application).(yml|properties)`
files that can be used to control CAS behavior. Also, note that this configuration directory can be monitored by CAS to auto-pick up changes
and refresh the application context as needed. Please [review this guide](Configuration-Management-Reload.html#reload-strategy) to learn more.

Note that by default, all CAS settings and configuration is controlled via the embedded `application.properties` file in the CAS server
web application. There is also an embedded `application.yml` file that allows you to override all defaults if you wish to ship the
configuration inside the main CAS web application and not rely on externalized configuration files. If you prefer
properties to yaml, then `application-standalone.properties` will override `application.properties` as well.

Settings found in external configuration files are and will be able to override the defaults 
provided by CAS. The naming of the configuration files inside the CAS configuration directory follows the below pattern:

- An `application.(properties|yml|yaml)` file is always loaded, if found.
- Settings located inside `properties|yml|yaml` files whose name matches the value of `spring.application.name` are loaded (i.e `cas.properties`) Note: `spring.application.name` defaults to uppercase `CAS` but the lowercase name will also be loaded.
- Settings located inside `properties|yml|yaml` files whose name matches the value of `spring.profiles.active` are loaded (i.e `ldap.properties`).
- Profile-specific application properties outside of your packaged web application (`application-{profile}.properties|yml|yaml`)
  This allows you to, if needed, split your settings into multiple property files and then locate them by assigning their name
  to the list of active profiles (i.e. `spring.profiles.active=standalone,testldap,stagingMfa`)

Configuration files are loaded in the following order where `spring.profiles.active=standalone,profile1,profile2`. Note
that the last configuration file loaded will override any duplicate properties from configuration files loaded earlier:

1. `application.(properties|yml|yaml) `
2. (lower case) `spring.application.name.(properties|yml|yaml)`
3. `spring.application.name.(properties|yml|yaml)`
4. `application-standalone.(properties|yml|yaml)`
5. `standalone.(properties|yml|yaml)`
6. `application-profile1.(properties|yml|yaml)`
7. `profile1.(properties|yml|yaml)`
8. `application-profile2.(properties|yml|yaml)`
9. `profile2.(properties|yml|yaml)`

If two configuration files with same base name and different extensions exist, they are processed in the order
of `properties`, `yml` and then `yaml` and then `groovy` (last one processed wins where duplicate properties exist). These
external configuration files will override files located in the classpath (e.g. files from `src/main/resources` in
your CAS overlay that end up in `WEB-INF/classes`) but the internal files are loaded per
the [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/) rules
which differ from the CAS standalone configuration rules described here (e.g. `<profile>.properties`
would not be loaded from classpath but `application-<profile>.properties` would).
  
## Sources

CAS by default will attempt to locate settings and properties using:

1. `/etc/cas/config`
2. `/opt/cas/config`
3. `/var/cas/config`

CAS has the ability to also load a Groovy file for loading settings. The file
is expected to be found at the above matching directory and should be named `${cas-application-name}.groovy`, such as `cas.groovy`. The
script is able to combine conditional settings for active profiles and common settings that are applicable to all environments and profiles into one location with a structure that is similar to the below example:

```groovy
// Settings may be filtered by individual profiles
profiles {
    standalone {
        cas.some.setting="value"
    }
}

// This applies to all profiles and environments
cas.common.setting="value"
``` 

You can also use a dedicated configuration file to directly feed a collection of properties
to CAS in form of a file or classpath resource. This is specially useful in cases
where a bare CAS server is deployed in the cloud without
the extra ceremony of a configuration server or an external directory for
that matter and the deployer wishes to avoid overriding embedded configuration files.

{% include_cached casproperties.html properties="cas.standalone." excludes="configuration-security" %}

## Handling Overrides

<div class="alert alert-warning"><strong>Remember</strong><p>You are advised to not overlay or otherwise
modify the built in <code>application.properties</code> or <code>bootstrap.properties</code> files. 
This will only complicate and weaken your deployment.
Instead try to comply with the CAS defaults and bootstrap CAS as much as possible via the defaults, 
override via <code>application.yml</code>, <code>application-standalone.properties</code> or
use the <a href="Configuration-Management.html#overview">outlined strategies</a>. Likewise, try to instruct CAS to locate
configuration files external to its own. Premature optimization will only lead to chaos.</p></div>

{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.config" %}

