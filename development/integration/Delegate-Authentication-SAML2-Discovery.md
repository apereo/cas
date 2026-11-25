# SAML2 Delegated Authentication - Identity Provider Discovery Service

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-SAML2-Discovery.html

> **Note**
>
> Using identity provider discovery requires delegated authentication to be available. This feature cannot be used on its own as a standalone discovery service.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-saml-idp-discovery"
}
```

Identity provider discovery allows CAS to [embed and present a discovery service](https://wiki.shibboleth.net/confluence/display/EDS10/Embedded+Discovery+Service) as part of delegated authentication. Configured SAML2 identity providers in the CAS configuration used for delegated authentication are presented as options for discovery.

The following settings and properties are available from the CAS configuration catalog:

No settings in the configuration catalog match this section.

CAS is also able to directly consume multiple JSON feeds that contain discovery metadata about available identity providers. The discovery JSON feed may be fetched from a URL (i.e. exposed by a Shibboleth Service Provider) or it may directly be consumed as a JSON file with the following structure:

```json
[{
 "entityID": "https://idp.example.net/idp/saml",
 "DisplayNames": [{
  "value": "Example.net",
  "lang": "en"
  }],
 "Descriptions": [{
  "value": "An identity provider for the people, by the people.",
  "lang": "en"
  }],
 "Logos": [{
  "value": "https://example.net/images/logo.png",
  "height": "90",
  "width": "62"
  }]
}]
```

> **Note**
>
> The JSON feed has the ability to overwrite MDUI information found in an existing identity provider's metadata, if any.

## Endpoints

The following endpoints are available:

| Endpoint | Description |
| --- | --- |
| `/idp/discovery` | Identity provider discovery landing page. |
| `/idp/discovery/feed` | Identity provider discovery JSON feed. Accepts an optional `entityID` parameter, treated as a regular expression. |
| `/idp/discovery/redirect` | Return endpoint to let CAS invoke the identity provider after selection. |

Applications may directly invoke the discovery service via `[cas-server-prefix]/idp/discovery`. The discovery service may also be invoked using the discovery protocol via `[cas-server-prefix]/idp/discovery?entityID=[service-provider-entity-id]&return=[cas-server-prefix]/idp/discovery/redirect`. Additional parameters may be included as part of the `return` url and they all must be encoded.
