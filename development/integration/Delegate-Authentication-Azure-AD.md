# Microsoft Entra ID

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-Azure-AD.html

Let users log in to CAS with their Microsoft Entra ID (formerly Azure AD) account through OpenID Connect.

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

- `cas.authn.pac4j.oidc[0].azure.discovery-uri` (Required; type `String`): The discovery endpoint to locate the provider metadata.
- `cas.authn.pac4j.oidc[0].azure.federation.jwks.location` (Required; type `Resource`): Specific JWKS for the federation. The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.pac4j.oidc[0].azure.id` (Required; type `String`): The client id.
- `cas.authn.pac4j.oidc[0].azure.private-key-jwt.jwks.location` (Required; type `Resource`): Specific JWKS for the private_key_jwt client authentication method. The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.pac4j.oidc[0].azure.secret` (Required; type `String`): The client secret.
- `cas.authn.pac4j.oidc[0].azure.allow-unsigned-id-tokens` (Optional; type `boolean`): Whether unsigned id tokens issued as plain JWTs are accepted.
- `cas.authn.pac4j.oidc[0].azure.auto-redirect-type` (Optional; type `DelegationAutoRedirectTypes`; default `NONE`): Auto-redirect to this client. Available values are as follows: - `SERVER`: Redirect on the server side, typically making CAS invisible. - `CLIENT`: Redirect on the client side using browser redirects, etc. - `NONE`: Do nothing and let the selection take place manually.
- `cas.authn.pac4j.oidc[0].azure.callback-url` (Optional; type `String`): Callback URL to use to return the flow back to the CAS server one the identity provider is successfully done. This may be used at the discretion of the client and its type to build service parameters, redirect URIs, etc. If none is specified, the CAS server's login endpoint will be used as the basis of the final callback url.
- `cas.authn.pac4j.oidc[0].azure.callback-url-type` (Optional; type `Pac4jBaseClientProperties.CallbackUrlTypes`; default `QUERY_PARAMETER`): Determine how the callback url should be resolved. Default is `CallbackUrlTypes#QUERY_PARAMETER`. Available values are as follows: - `PATH_PARAMETER`: Path parameter callback url. The client name is added to the path of the callback URL. - `QUERY_PARAMETER`: Query parameter callback url. The client name is added to the path of a query parameter. - `NONE`: No callback url. No name is added to the callback URL to be able to distinguish the client.
- `cas.authn.pac4j.oidc[0].azure.client-authentication-method` (Optional; type `String`): The preferred client authentication method that will be chosen for token requests. If none is specified, one will be chosen somewhat randomly based on what the OIDC OP supports.
- `cas.authn.pac4j.oidc[0].azure.client-name` (Optional; type `String`): Name of the client mostly for UI purposes and uniqueness. This name, with 'non-word' characters converted to '-' (e.g. "This Org (New)" becomes "This-Org--New-") is added to the "class" attribute of the redirect link on the login page, to allow for custom styling of individual IdPs (e.g. for an organization logo).
- `cas.authn.pac4j.oidc[0].azure.connect-timeout` (Optional; type `String`): Read timeout of the OIDC client.
- `cas.authn.pac4j.oidc[0].azure.css-class` (Optional; type `String`): CSS class that should be assigned to this client.
- `cas.authn.pac4j.oidc[0].azure.custom-params` (Optional; type `Map<String,String>`): Custom parameters to send along in authZ requests, etc.
- `cas.authn.pac4j.oidc[0].azure.disable-pkce` (Optional; type `boolean`): Disable PKCE support for the provider.
- `cas.authn.pac4j.oidc[0].azure.display-name` (Optional; type `String`): Indicate the title or display name of the client for decoration and client presentation purposes. If left blank, the client original name would be used by default.
- `cas.authn.pac4j.oidc[0].azure.enabled` (Optional; type `boolean`; default `true`): Whether the client/external identity provider should be considered active and enabled for integration purposes.
- `cas.authn.pac4j.oidc[0].azure.expire-session-with-token` (Optional; type `boolean`): Checks if sessions expire with token expiration.
- `cas.authn.pac4j.oidc[0].azure.federation.application-type` (Optional; type `String`; default `web`): Application type for the RP (client) entity statement.
- `cas.authn.pac4j.oidc[0].azure.federation.client-registration-types` (Optional; type `List<String>`; default `explicit,automatic`): Registration types for the RP (client) entity statement.
- `cas.authn.pac4j.oidc[0].azure.federation.contact-emails` (Optional; type `List<String>`): The contact emails for this client.
- `cas.authn.pac4j.oidc[0].azure.federation.contact-name` (Optional; type `String`): The contact name for this client.
- `cas.authn.pac4j.oidc[0].azure.federation.enabled` (Optional; type `boolean`): Whether the federation is enabled.
- `cas.authn.pac4j.oidc[0].azure.federation.grant-types` (Optional; type `List<String>`; default `authorization_code`): Grant types for the RP (client) entity statement.
- `cas.authn.pac4j.oidc[0].azure.federation.jwks.kid` (Optional; type `String`): The key identifier.
- `cas.authn.pac4j.oidc[0].azure.federation.response-types` (Optional; type `List<String>`; default `code`): Response types for the RP (client) entity statement.
- `cas.authn.pac4j.oidc[0].azure.federation.scopes` (Optional; type `List<String>`; default `openid,email,profile`): Scopes for the RP (client) entity statement.
- `cas.authn.pac4j.oidc[0].azure.federation.target-op` (Optional; type `String`): The target OIDC provider in federation.
- `cas.authn.pac4j.oidc[0].azure.federation.trust-anchors` (Optional; type `Map<String, String>`): The trust anchors to use for federation. This is defined as a map; the key is the trust anchor issuer and the value is the JWKS URL of the trust anchor.
- `cas.authn.pac4j.oidc[0].azure.federation.validity-in-days` (Optional; type `int`; default `90`): Validity in days of the RP (client) entity statement.
- `cas.authn.pac4j.oidc[0].azure.include-access-token-claims` (Optional; type `boolean`): If enabled, try to process the access token as a JWT and include its claims in the profile. Only enable this if there is an agreement between the IdP and CAS about the format of the access token. If not, the token format could change at any time.
- `cas.authn.pac4j.oidc[0].azure.logout-url` (Optional; type `String`): Logout url used for this provider.
- `cas.authn.pac4j.oidc[0].azure.mapped-claims` (Optional; type `List<String>`): List arbitrary mappings of claims when fetching user profiles. Uses a "directed list" where the allowed syntax would be `claim->attribute`.
- `cas.authn.pac4j.oidc[0].azure.max-clock-skew` (Optional; type `String`): Clock skew in order to account for drift, when validating id tokens.
- `cas.authn.pac4j.oidc[0].azure.preferred-jws-algorithm` (Optional; type `String`): The JWS algorithm to use forcefully when validating ID tokens. If none is defined, the first algorithm from metadata will be used.
- `cas.authn.pac4j.oidc[0].azure.principal-id-attribute` (Optional; type `String`): The attribute found in the identity provider response that may be used to establish the authenticated user and build a profile for CAS.
- `cas.authn.pac4j.oidc[0].azure.private-key-jwt.jwks.kid` (Optional; type `String`): The key identifier.
- `cas.authn.pac4j.oidc[0].azure.propagate-logout` (Optional; type `boolean`; default `true`): Indicate whether the logout should be propagated from the CAS server to the delegated identity server.
- `cas.authn.pac4j.oidc[0].azure.read-timeout` (Optional; type `String`): Connect timeout of the OIDC client.
- `cas.authn.pac4j.oidc[0].azure.response-mode` (Optional; type `String`): The response mode specifies how the result of the authorization request is formatted. For backward compatibility the default value is empty, which means the default pac4j (empty) response mode is used. Possible values includes "query", "fragment", "form_post", or "web_message"
- `cas.authn.pac4j.oidc[0].azure.response-type` (Optional; type `String`): The response type tells the authorization server which grant to execute. For backward compatibility the default value is empty, which means the default pac4j ("code") response type is used. Possibles values includes "code", "token" or "id_token".
- `cas.authn.pac4j.oidc[0].azure.scope` (Optional; type `String`): Requested scope(s).
- `cas.authn.pac4j.oidc[0].azure.supported-client-authentication-methods` (Optional; type `String`): Control the list of supported client authentication methods that can be accepted and understood by this integration. Multiple methods may be specified and separated via a comma. Example might be `client_secret_basic,client_secret_post,client_secret_jwt`.
- `cas.authn.pac4j.oidc[0].azure.tenant` (Optional; type `String`): Azure AD tenant name. After tenant is configured, `#getDiscoveryUri()` property will be overridden. Azure AD tenant name can take 4 different values: - `common`: Users with both a personal Microsoft account and a work or school account from Azure AD can sign in. - `organizations`: Only users with work or school accounts from Azure AD can sign in. - `consumers`: Only users with a personal Microsoft account can sign in. - Specific tenant domain name or ID: Only user with account under that the specified tenant can login
- `cas.authn.pac4j.oidc[0].azure.token-expiration-advance` (Optional; type `String`): Default time period advance (in seconds) for considering an access token expired.
- `cas.authn.pac4j.oidc[0].azure.use-nonce` (Optional; type `boolean`): Whether an initial nonce should be to used initially for replay attack mitigation.
- `cas.authn.pac4j.oidc[0].azure.validate-logout-token` (Optional; type `boolean`): Controls whether the logout token submitted as a JWT should be validated for the correct signature, etc.
