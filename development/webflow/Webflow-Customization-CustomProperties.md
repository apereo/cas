# Webflow Custom Properties

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-CustomProperties.html

All webflow components and CAS views have access to the entire bundle of CAS settings defined from a variety of configuration sources. This allows one to extend and modify any CAS view or webflow component using the variable `casProperties` to gain access to a specific setting. Remember that this syntax only allows access to settings that are *owned* by CAS, noted by its very own prefix.

The following settings and properties are available from the CAS configuration catalog:

- `cas.custom.properties` (Optional; type `Map<String,String>`): Collection of custom settings that can be utilized for a local deployment. The settings should be available to CAS views and webflows for altering UI and/or introducing custom behavior to any extended customized component without introducing a new property namespace. This is defined as a map, where the key should be the setting name and the value should be the setting value.

> **Usage**
>
> If you are extending CAS to define your own components and business logic and need to find a way to define your own settings, we generally recommend that you define your own setting namespace and prefix using your own `@ConfigurationProperties` conventions. Hijacking a CAS-owned configuration namespace should be seen as a poor design choice and must be avoided.
