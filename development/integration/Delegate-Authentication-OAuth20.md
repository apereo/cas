# OAuth20

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-OAuth20.html

Let users log in through any OAuth 2.0 provider that has no dedicated integration, by describing its endpoints and profile attributes in CAS settings.

For an overview of the delegated authentication flow, please [see this guide](https://apereo.github.io/cas/development/integration/Delegate-Authentication.html).

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-pac4j-oidc"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.oauth2[0].auth-url` (Required; type `String`): Authorization endpoint of the provider.
- `cas.authn.pac4j.oauth2[0].id` (Required; type `String`): The client id.
- `cas.authn.pac4j.oauth2[0].profile-url` (Required; type `String`): Profile endpoint of the provider.
- `cas.authn.pac4j.oauth2[0].secret` (Required; type `String`): The client secret.
- `cas.authn.pac4j.oauth2[0].token-url` (Required; type `String`): Token endpoint of the provider.
- `cas.authn.pac4j.oauth2` (Optional; type `List<Pac4jOAuth20ClientProperties>`): Settings that deal with having OAuth2-capable providers as an external delegated-to authentication provider.
- `cas.authn.pac4j.oauth2[0].auto-redirect-type` (Optional; type `DelegationAutoRedirectTypes`; default `NONE`): Auto-redirect to this client. Available values are as follows: - `SERVER`: Redirect on the server side, typically making CAS invisible. - `CLIENT`: Redirect on the client side using browser redirects, etc. - `NONE`: Do nothing and let the selection take place manually.
- `cas.authn.pac4j.oauth2[0].callback-url` (Optional; type `String`): Callback URL to use to return the flow back to the CAS server one the identity provider is successfully done. This may be used at the discretion of the client and its type to build service parameters, redirect URIs, etc. If none is specified, the CAS server's login endpoint will be used as the basis of the final callback url.
- `cas.authn.pac4j.oauth2[0].callback-url-type` (Optional; type `Pac4jBaseClientProperties.CallbackUrlTypes`; default `QUERY_PARAMETER`): Determine how the callback url should be resolved. Default is `CallbackUrlTypes#QUERY_PARAMETER`. Available values are as follows: - `PATH_PARAMETER`: Path parameter callback url. The client name is added to the path of the callback URL. - `QUERY_PARAMETER`: Query parameter callback url. The client name is added to the path of a query parameter. - `NONE`: No callback url. No name is added to the callback URL to be able to distinguish the client.
- `cas.authn.pac4j.oauth2[0].client-authentication-method` (Optional; type `String`; default `basicAuth`): The client authentication method: `basicAuth` (default) or `requestBody`.
- `cas.authn.pac4j.oauth2[0].client-name` (Optional; type `String`): Name of the client mostly for UI purposes and uniqueness. This name, with 'non-word' characters converted to '-' (e.g. "This Org (New)" becomes "This-Org--New-") is added to the "class" attribute of the redirect link on the login page, to allow for custom styling of individual IdPs (e.g. for an organization logo).
- `cas.authn.pac4j.oauth2[0].css-class` (Optional; type `String`): CSS class that should be assigned to this client.
- `cas.authn.pac4j.oauth2[0].custom-params` (Optional; type `Map<String, String>`): Custom parameters in form of key-value pairs sent along in authZ requests, etc.
- `cas.authn.pac4j.oauth2[0].display-name` (Optional; type `String`): Indicate the title or display name of the client for decoration and client presentation purposes. If left blank, the client original name would be used by default.
- `cas.authn.pac4j.oauth2[0].enabled` (Optional; type `boolean`; default `true`): Whether the client/external identity provider should be considered active and enabled for integration purposes.
- `cas.authn.pac4j.oauth2[0].principal-id-attribute` (Optional; type `String`): The attribute found in the identity provider response that may be used to establish the authenticated user and build a profile for CAS.
- `cas.authn.pac4j.oauth2[0].profile-attrs` (Optional; type `Map<String, String>`): Profile attributes to request and collect in form of key-value pairs. Key is the attribute name, and value is the mapped attribute name, if necessary. If remapping is not required, key and value should match. It's also possible to define values as `CONVERTER|mapped-attribute`. `CONVERTER` should be the attribute converter specified by its acceptable type and when acceptable, the converter attempts to transform the provided attribute value. Accepted converters are `Locale, Integer, Color, Date, Gender, Boolean, Long, String, Url`. CAS can also provide a special attribute converter that does the transformation and conversion based on an *inline groovy script*. This special groovy converter can be specified using this example syntax for the value, `groovy { return attribute + '-test'`|mapped-attribute}.
- `cas.authn.pac4j.oauth2[0].profile-verb` (Optional; type `String`; default `POST`): Http method to use when asking for profile.
- `cas.authn.pac4j.oauth2[0].propagate-logout` (Optional; type `boolean`; default `true`): Indicate whether the logout should be propagated from the CAS server to the delegated identity server.
- `cas.authn.pac4j.oauth2[0].response-type` (Optional; type `String`; default `code`): Response type determines the authentication flow on the Authentication Server.
- `cas.authn.pac4j.oauth2[0].scope` (Optional; type `String`): The scope requested from the identity provider.
- `cas.authn.pac4j.oauth2[0].with-state` (Optional; type `boolean`): Whether a state value should be generated when sending authentication requests to the provider.
