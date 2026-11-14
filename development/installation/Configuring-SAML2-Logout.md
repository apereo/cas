# SAML2 Logout & SLO

Source: https://apereo.github.io/cas/development/installation/Configuring-SAML2-Logout.html

SLO is a mechanism that allows a user to log out of one service provider and be simultaneously logged out of all other service providers and the identity provider. SLO ensures that a user’s session is terminated across all participating entities when they log out of one service.

The following endpoints in CAS support SAML2 SLO operations:

| Endpoint | Description |
| --- | --- |
| `/idp/profile/SAML2/POST/SLO` | Handles SLO requests for `POST` bindings. |
| `/idp/profile/SAML2/Redirect/SLO` | Handles SLO requests for `Redirect` bindings. |

You will also find the above endpoints in the SAML2 identity provider metadata generated and managed by CAS:

```xml
<IDPSSODescriptor ...>
    <SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" 
        Location="https://sso.example.org/cas/idp/profile/SAML2/POST/SLO"/>
        
    <SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect" 
        Location="https://sso.example.org/cas/idp/profile/SAML2/Redirect/SLO" />
</IDPSSODescriptor>
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.saml-idp.logout.force-signed-logout-requests` (Optional; type `Boolean`; default `true`): Whether SLO logout requests are required to be signed.
- `cas.authn.saml-idp.logout.logout-request-bindings` (Optional; type `List<String>`): The order in which the logout request bindings should be tried (if available at the SP level).
- `cas.authn.saml-idp.logout.logout-response-binding` (Optional; type `String`): Whether SLO logout responses should be sent using a forced binding. Accepted values are: - `urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST` - `urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect` If no binding is defined, all available SLO endpoints found in the metadata will be consulted for logout responses.
- `cas.authn.saml-idp.logout.send-logout-response` (Optional; type `Boolean`; default `true`): Whether SLO logout responses should be sent to service providers.
- `cas.authn.saml-idp.logout.sign-logout-response` (Optional; type `Boolean`; default `false`): Whether SLO logout responses are required to be signed.
- `cas.authn.saml-idp.logout.single-logout-callbacks-disabled` (Optional; type `Boolean`; default `false`): Whether SAML SLO is enabled and processed.

## Logout Response

SAML2 logout response bindings are decided based on the following rules:

- The binding can be explicitly defined and overridden for a service provider:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "https://sp.example.org",
  "name" : "SAML",
  "id" : 1,
  "metadataLocation" : "/path/to/sp/metadata.xml",
  "logoutResponseBinding": "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
}
```

- The service provider metadata is consulted and CAS may pick the `SingleLogoutService` defined in the metadata. Order of `SingleLogoutService` elements in the metadata is important and CAS typically will choose the first entry.
- A global binding value can then be chosen and defined in CAS configuration properties.

```xml
<md:SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect" 
    Location="http://sp.example.org/slo/redirect"/>
<md:SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" 
    Location="http://sp.example.org/slo/post"/>
```

- CAS may choose `urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect` as the fallback binding.
