# CAS Bill of Materials (BOM)

Source: https://apereo.github.io/cas/development/installation/BOM-Dependency-Management.html

Each release of CAS provides a curated list of dependencies it supports. In practice, you do not need to provide a version for any of these dependencies in your build configuration as the CAS distribution is managing that for you. When you upgrade CAS itself, these dependencies will be upgraded as well in a consistent way.

The curated list of dependencies contains a refined list of third party libraries. The list is available as a standard Bills of Materials (BOM). Not everyone likes inheriting from the BOM. You may have your own corporate standard parent that you need to use, or you may just prefer to explicitly declare all your configuration.

Support for BOM is enabled by including the following dependency in the WAR overlay:

```groovy
implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
```

Please note that the [CAS Initializr](https://apereo.github.io/cas/development/installation/WAR-Overlay-Initializr.html) is preconfigured to use the CAS BOM.
