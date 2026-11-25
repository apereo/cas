# SAML2 Delegated Authentication - Amazon S3 Service Provider Metadata

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-SAML2-Metadata-AmazonS3.html

SAML2 metadata for CAS as the SAML2 service provider may also be managed inside an Amazon S3 bucket. A single bucket is created by default automatically that is able to store different objects for each service provider and metadata entity.

To activate this feature, you need to start by including the following module in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-aws"
}
```

**Feature Activation**

To activate this feature, the following feature toggle(s) must be turned on:

```properties
CasFeatureModule.DelegatedAuthentication.saml-s3.enabled=true
```

To learn more about configuration feature toggles, please [see this page](https://apereo.github.io/cas/development/configuration/Configuration-Feature-Toggles.html).

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.credential-access-key` (Required; type `String`): Use access-key provided by AWS to authenticate.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.credential-secret-key` (Required; type `String`): Use secret key provided by AWS to authenticate.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.endpoint` (Required; type `String`): AWS custom endpoint.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.region` (Required; type `String`): AWS region used.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.client-execution-timeout` (Optional; type `String`): Client execution timeout.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.connection-timeout` (Optional; type `String`): Connection timeout.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.local-address` (Optional; type `String`): Local address.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.max-connections` (Optional; type `int`): Maximum connections setting.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.profile-name` (Optional; type `String`): Profile name to use.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.profile-path` (Optional; type `String`): Profile path.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.proxy-host` (Optional; type `String`): Optionally specifies the proxy host to connect through.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.proxy-password` (Optional; type `String`): Optionally specifies the proxy password to connect through.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.proxy-username` (Optional; type `String`): Optionally specifies the proxy username to connect through.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.retry-mode` (Optional; type `String`): Outline the requested retry mode. Accepted values are `STANDARD, LEGACY`.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.socket-timeout` (Optional; type `String`): Socket timeout.
- `cas.authn.pac4j.saml[0].metadata.service-provider.amazon-s3.use-reaper` (Optional; type `boolean`): Flag that indicates whether to use reaper.
