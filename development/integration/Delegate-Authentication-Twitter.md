# X (Twitter)

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-Twitter.html

Delegate authentication to X, the service formerly known as Twitter. The provider is still named `Twitter` in CAS settings and client names.

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

- `cas.authn.pac4j.twitter.id` (Required; type `String`): The client id.
- `cas.authn.pac4j.twitter.secret` (Required; type `String`): The client secret.
- `cas.authn.pac4j.twitter.auto-redirect-type` (Optional; type `DelegationAutoRedirectTypes`; default `none`): Auto-redirect to this client. Available values are as follows: - `SERVER`: Redirect on the server side, typically making CAS invisible. - `CLIENT`: Redirect on the client side using browser redirects, etc. - `NONE`: Do nothing and let the selection take place manually.
- `cas.authn.pac4j.twitter.callback-url` (Optional; type `String`): Callback URL to use to return the flow back to the CAS server one the identity provider is successfully done. This may be used at the discretion of the client and its type to build service parameters, redirect URIs, etc. If none is specified, the CAS server's login endpoint will be used as the basis of the final callback url.
- `cas.authn.pac4j.twitter.callback-url-type` (Optional; type `Pac4jBaseClientProperties.CallbackUrlTypes`; default `QUERY_PARAMETER`): Determine how the callback url should be resolved. Default is `CallbackUrlTypes#QUERY_PARAMETER`. Available values are as follows: - `PATH_PARAMETER`: Path parameter callback url. The client name is added to the path of the callback URL. - `QUERY_PARAMETER`: Query parameter callback url. The client name is added to the path of a query parameter. - `NONE`: No callback url. No name is added to the callback URL to be able to distinguish the client.
- `cas.authn.pac4j.twitter.client-name` (Optional; type `String`): Name of the client mostly for UI purposes and uniqueness. This name, with 'non-word' characters converted to '-' (e.g. "This Org (New)" becomes "This-Org--New-") is added to the "class" attribute of the redirect link on the login page, to allow for custom styling of individual IdPs (e.g. for an organization logo).
- `cas.authn.pac4j.twitter.css-class` (Optional; type `String`): CSS class that should be assigned to this client.
- `cas.authn.pac4j.twitter.display-name` (Optional; type `String`): Indicate the title or display name of the client for decoration and client presentation purposes. If left blank, the client original name would be used by default.
- `cas.authn.pac4j.twitter.enabled` (Optional; type `Boolean`; default `true`): Whether the client/external identity provider should be considered active and enabled for integration purposes.
- `cas.authn.pac4j.twitter.include-email` (Optional; type `Boolean`; default `false`): Set to true to request the user's email address from the Twitter API. For this to have an effect it must first be enabled in the Twitter developer console.
- `cas.authn.pac4j.twitter.principal-id-attribute` (Optional; type `String`): The attribute found in the identity provider response that may be used to establish the authenticated user and build a profile for CAS.
- `cas.authn.pac4j.twitter.propagate-logout` (Optional; type `Boolean`; default `true`): Indicate whether the logout should be propagated from the CAS server to the delegated identity server.
