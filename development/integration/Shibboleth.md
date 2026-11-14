# Overview

Source: https://apereo.github.io/cas/development/integration/Shibboleth.html

CAS can be integrated with the [Shibboleth federated SSO platform](http://shibboleth.net/) by a couple different strategies. It is possible to designate CAS to serve as the authentication provider for the Shibboleth IdP. With such a setup, when user is routed to the IdP, the following may take place:

- If the user has already authenticated to CAS and has a valid CAS SSO session, the IdP will transparently perform the requested action, e.g. attribute release.
- If the user does not have a valid CAS SSO session, the user will be redirected to CAS and must authenticate before the IdP proceeds with the requested action.

> **Note**
>
> Remember that this page is specifically dedicated to integration options with the Shibboleth Identity Provider. If you need CAS to act as a SAML2 identity provider on its own, you should [start here instead](https://apereo.github.io/cas/development/authentication/Configuring-SAML2-Authentication.html).

## SSO for Shibboleth IdP (External)

This is a Shibboleth IdP external authentication plugin that delegates the authentication to CAS. This solution has the ability to utilize a full range of native CAS protocol features such as `renew` and `gateway`.

The plugin is available for both Shibboleth Identity Provider [v2](https://github.com/Unicon/shib-cas-authn2) and [v3](https://github.com/Unicon/shib-cas-authn3) and [v4](https://github.com/Unicon/shib-cas-authn).

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-shibboleth"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.shib-idp.server-url` (Optional; type `String`; default `localhost`): The server url of the shibboleth idp deployment.

### Relying Party EntityId

The authentication plugin is able to pass the relying party’s entity ID over to the CAS server upon authentication requests. The entity ID is passed in form of a url parameter to the CAS server as such:

```
https://sso.example.org/cas/login?service=<authentication-plugin-url>&entityId=<relying-party-entity-id>
```

You can also take advantage of the `entityId` parameter and treat it as a normal CAS service definition, so it can be used for multifactor authentication and authorization.

See [this guide](https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication-Triggers.html) for more info.

## Displaying SAML MDUI

The CAS server is able to recognize the `entityId` parameter and display SAML MDUI on the login page, that is provided by the metadata associated with the relying party.

### Configuration

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-saml-mdui"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.saml-metadata-ui.resources` (Required; type `List<String>`): Metadata resources to load and parse through based on the incoming entity id in order to locate MDUI. Resources can be classpath/file/http resources. If each metadata resource has a signing certificate, they can be added onto the resource with a `::` separator. Example: `classpath:/sp-metadata.xml::classpath:/pub.key`.
- `cas.saml-metadata-ui.max-validity` (Optional; type `Long`; default `0`): If specified, creates a validity filter on the metadata to check for metadata freshness based on the max validity. Value is specified in seconds.
- `cas.saml-metadata-ui.parameter` (Optional; type `String`; default `entityId`): The parameter name that indicates the entity id of the service provider submitted to CAS.
- `cas.saml-metadata-ui.require-signed-root` (Optional; type `Boolean`; default `false`): When parsing metadata, whether the root element is required to be signed.
- `cas.saml-metadata-ui.require-valid-metadata` (Optional; type `Boolean`; default `true`): Whether valid metadata is required when parsing metadata.
- `cas.saml-metadata-ui.schedule.cron-expression` (Optional; type `String`): A cron-like expression, extending the usual UN*X definition to include triggers on the second, minute, hour, day of month, month, and day of week. For example, `0 * * * * MON-FRI` means once per minute on weekdays (at the top of the minute - the 0th second) or `0 0 0 * * *` means every day at midnight. * Note that when cron expressions are defined, the start delay and repeat interval settings are must be removed and set to blank.
- `cas.saml-metadata-ui.schedule.cron-time-zone` (Optional; type `String`): A time zone for which the cron expression will be resolved. By default, this attribute is empty (i.e. the scheduler's time zone will be used).
- `cas.saml-metadata-ui.schedule.enabled` (Optional; type `Boolean`; default `true`): Whether scheduler should be enabled to schedule the job to run.
- `cas.saml-metadata-ui.schedule.enabled-on-host` (Optional; type `String`; default `.*`): Overrides `SchedulingProperties#enabled` property value of true if this property does not match hostname of CAS server. This can be useful if deploying CAS with an image in a statefulset where all names are predictable but where having different configurations for different servers is hard. The value can be an exact hostname or a regular expression that will be used to match the hostname.
- `cas.saml-metadata-ui.schedule.repeat-interval` (Optional; type `String`; default `PT2M`): String representation of a repeat interval of re-loading data for a data store implementation. This is the timeout between consecutive job’s executions.
- `cas.saml-metadata-ui.schedule.start-delay` (Optional; type `String`; default `PT15S`): String representation of a start delay of loading data for a data store implementation. This is the delay between scheduler startup and first job’s execution

### Relying Party Metadata

You may allow CAS to recognize SAML MDUI directly from metadata documents that are fed to CAS via settings. If the metadata for the relying party matches the requested `entityId` and contains MDUI elements, those will be passed onto the login page for decorations. If MDUI is not available in the metadata, the relevant elements from the matching service in the service registry will be used all the same.

### Service Registry Metadata

You may also register the relying party in the CAS service registry as a regular service application and specify a number of MDUI-like elements in the body of the registration record. An example follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "relying-party-entity-id",
  "name" : "Test",
  "id" : 100,
  "description" : "This is the test application.",
  "evaluationOrder" : 10000,
  "logo": "images/logo.png",
  "informationUrl": "https://test.example.org/info",
  "privacyUrl": "https://test.example.org/privacy"
}
```
