---
layout: default
title: CAS - Bill of Materials
category: Authentication
---
{% include variables.html %}


# CAS Bill of Materials  (BOM)

Each release of CAS provides a curated list of dependencies it supports. In practice, you do not need 
to provide a version for any of these dependencies in your build configuration as the CAS distribution is managing that for you. 
When you upgrade CAS itself, these dependencies will be upgraded as well in a consistent way.

The curated list of dependencies contains a refined list of third party libraries. The list is
available as a standard Bills of Materials (BOM). Not everyone likes inheriting from the BOM.
You may have your own corporate standard parent that you need to use, or 
you may just prefer to explicitly declare all your configuration.

Support for BOM is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-bom" ignoreBOM="true" %}

Please [use this guide](https://plugins.gradle.org/plugin/io.spring.dependency-management)
and configure the Gradle build accordingly.

Please note that the [CAS Initializr](WAR-Overlay-Initializr.html) is preconfigured to use the CAS BOM.
