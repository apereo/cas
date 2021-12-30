---
layout: default
title: CAS - Configuration Server
category: Configuration
---

{% include variables.html %}

# Spring Cloud Configuration Server - Spring Cloud Apache ZooKeeper

Spring Cloud Configuration Server is able to use [Apache ZooKeeper](https://zookeeper.apache.org/) to locate properties and settings.

Support is provided via the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-configuration-cloud-zookeeper" %}

<div class="alert alert-info mt-3"><strong>Usage</strong><p>The configuration modules provide here may also be used verbatim inside a CAS server overlay and do not exclusively belong to a Spring Cloud Configuration server. While this module is primarily useful when inside the Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay directly to fetch settings from a source.</p></div>

You will need to map CAS settings to ZooKeeper's nodes that contain values. The parent node for all settings should
match the configuration root value provided to CAS. Under the root, you could have folders such
as `cas`, `cas,dev`, `cas,local`, etc where `dev` and `local` are Spring profiles.

To create nodes and values in Apache ZooKeeper, try the following commands
as a sample:

```bash
zookeeper-client -server zookeeper1:2181
create /cas cas
create /cas/config cas
create /cas/config/cas cas
create /cas/config/cas/settingName casuser::Test
```

Creating nodes and directories in Apache ZooKeeper may require providing a value. The above sample commands show that
the value `cas` is provided when creating directories. Always check with the official Apache ZooKeeper guides. You may not need to do that step.

Finally in your CAS properties, the new `settingName` setting can be used as a reference.

```properties
# cas.something.something=${settingName}
```

...where `${settingName}` gets the value of the contents of the Apache ZooKeeper node `cas/config/cas/settingName`.

{% include_cached casproperties.html thirdPartyStartsWith="spring.cloud.zookeeper" %}

