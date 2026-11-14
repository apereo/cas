# Delegated Authentication Discovery Selection

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-DiscoverySelection.html

External identity providers available to CAS are presented to the user and available for discovery and selection using the following strategies.

> **Remember**
>
> If you are interested in supporting discovery of SAML2 identity providers using a discovery service, you may want to take a look at [this guide](https://apereo.github.io/cas/development/integration/Delegate-Authentication-SAML2.html).

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.core.discovery-selection.json.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.pac4j.core.discovery-selection.selection-type` (Required; type `Pac4jDelegatedAuthenticationDiscoverySelectionProperties.Pac4jDelegatedAuthenticationSelectionTypes`; default `MENU`): Indicate how the selection and presentation of identity providers would be controlled. Available values are as follows: - `MENU`: Defined identity providers will be listed for the user to select. - `DYNAMIC`: Defined identity providers are pre-built first, and one is chosen dynamically at runtime based on user attributes properties, domain identifier, etc.
- `cas.authn.pac4j.core.discovery-selection.json.principal-attribute` (Optional; type `String`): The name of the principal attribute whose values will be compared against the key pattern defined in the configuration rules. If a match is found, then the provider configuration block will be used as the selected provider. The matching routine will examine all attribute values linked to the principal attribute to find any acceptable match. When this setting left undefined, then the resolved principal id for the given user identifier will be used to locate the provider.

**Menu**

This is the default and most common strategy that allows external identity providers to be listed on the login page, allowing the user to *order one from the menu* and select one from the list of options.

**Dynamic**

Rather than listing all available identity providers, this option allows CAS to auto-select the appropriate identity provider in a dynamic fashion using pre-defined rules and conditions and based on the user identifiers such as username, email address, etc. For example, once the user providers their identifier, i.e. `casuser@example.org`, the discovery strategy can try to select the correct identity provider based on the email domain.

## JSON Rules

By default, the selection rules and conditions can be specified in a JSON file with the following structure:

```json
{
    "@class" : "java.util.HashMap",
    "<key-pattern>" : {
      "@class" : "org.apereo.cas.pac4j.discovery.DelegatedAuthenticationDynamicDiscoveryProvider",
      "clientName" : "SAML2Client",
      "order": 0
    }
}
```

The following parameters are available to the JSON resource:

| Parameter | Description |
| --- | --- |
| `<key-pattern>` | Regular expression pattern matched against the user identifier (or principal attribute values) to locate the provider. i.e. `.+@example.org`. |
| `clientName` | The client name that should be used for this match, found and defined in CAS configuration. |
| `order` | The selection sorting order, used to properly sequence and prioritize entries in case there is overlap. |

**Custom**

If you wish to create your own strategy to dynamically locate identity providers for delegated authentication discovery, you will need to design a component and register it with CAS as such:

```java
@Bean
public DelegatedAuthenticationDynamicDiscoveryProviderLocator delegatedAuthenticationDynamicDiscoveryProviderLocator() {
    return new CustomDelegatedAuthenticationDynamicDiscoveryProviderLocator();
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
