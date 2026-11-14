# Configuration Feature Toggles

Source: https://apereo.github.io/cas/development/configuration/Configuration-Feature-Toggles.html

You can exclude auto-configuration classes by listing their fully-qualified class names, such as `org.apereo.cas.custom.config.SomethingConfigurationClass`, in the Spring Boot setting that excludes auto-configuration classes, in the `cas.properties` file.

The following settings and properties are available from the CAS configuration catalog:

- `spring.autoconfigure.exclude` (Third party; type `List<Class>`): Auto-configuration classes to exclude.

This is a very granular, internal way to control the auto-configuration classes, but it comes with a few important caveats:

- It requires you to know the exact class name of the auto-configuration class you want to exclude.
- The auto-configuration classes are entirely internal to CAS and they are subject to change or removal without notice.
- You may need to hunt down all auto-configuration classes applicable to a feature or behavior and list them for exclusion.

In short, while this method is very powerful, it requires that you know the internal workings of CAS and its auto-configuration classes, and it may be difficult to maintain across CAS releases.

Alternatively, it may be desirable to entirely disable a feature altogether by excluding all applicable auto-configuration classes without having to identify all of them. This can be done using feature toggles that may be set to `true` or `false` in the CAS configuration:

72 features and 315 toggles. Every toggle is on unless marked *off by default*; turning a feature off turns off all of its integrations.

Authentication8 features

19 auto-configuration classes: `AcceptUsersAuthenticationEventExecutionPlanConfiguration` and 18 more

`CasFeatureModule.Authentication.enabled`On by default

`CasFeatureModule.Authentication.accept.enabled`On by default

`CasFeatureModule.Authentication.aws-cognito.enabled`On by default

`CasFeatureModule.Authentication.azuread.enabled`On by default

`CasFeatureModule.Authentication.basic.enabled`On by default

`CasFeatureModule.Authentication.cassandra.enabled`On by default

`CasFeatureModule.Authentication.cloud-directory.enabled`On by default

`CasFeatureModule.Authentication.generic.enabled`On by default

`CasFeatureModule.Authentication.gua.enabled`On by default

`CasFeatureModule.Authentication.jdbc.enabled`On by default

`CasFeatureModule.Authentication.mongo.enabled`On by default

`CasFeatureModule.Authentication.okta.enabled`On by default

`CasFeatureModule.Authentication.qr.enabled`On by default

`CasFeatureModule.Authentication.redis.enabled`On by default

`CasFeatureModule.Authentication.remote.enabled`On by default

`CasFeatureModule.Authentication.rest.enabled`On by default

`CasFeatureModule.Authentication.syncope.enabled`On by default

`CasFeatureModule.Authentication.token.enabled`On by default

`CasFeatureModule.Authentication.trusted.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/integration/Delegate-Authentication.html)11 auto-configuration classes: `CasDelegatedAuthenticationAutoConfiguration` and 10 more

`CasFeatureModule.DelegatedAuthentication.enabled`On by default

`CasFeatureModule.DelegatedAuthentication.cas.enabled`On by default

`CasFeatureModule.DelegatedAuthentication.dynamic-discovery.enabled`On by default

`CasFeatureModule.DelegatedAuthentication.hazelcast.enabled`On by default

`CasFeatureModule.DelegatedAuthentication.jdbc.enabled`Off by default

`CasFeatureModule.DelegatedAuthentication.oidc.enabled`On by default

`CasFeatureModule.DelegatedAuthentication.saml.enabled`On by default

`CasFeatureModule.DelegatedAuthentication.saml-idp.enabled`On by default

`CasFeatureModule.DelegatedAuthentication.saml-jdbc.enabled`Off by default

`CasFeatureModule.DelegatedAuthentication.saml-mongodb.enabled`Off by default

`CasFeatureModule.DelegatedAuthentication.saml-s3.enabled`Off by default

[Documentation](https://apereo.github.io/cas/development/authentication/LDAP-Authentication.html)2 auto-configuration classes: `CasLdapAuthenticationAutoConfiguration` and 1 more

`CasFeatureModule.LDAP.authentication.enabled`On by default

`CasFeatureModule.LDAP.password-sync.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication.html)6 auto-configuration classes: `CasJpaPasswordlessAuthenticationAutoConfiguration` and 5 more

`CasFeatureModule.PasswordlessAuthn.enabled`On by default

`CasFeatureModule.PasswordlessAuthn.captcha.enabled`On by default

`CasFeatureModule.PasswordlessAuthn.jpa.enabled`On by default

`CasFeatureModule.PasswordlessAuthn.ldap.enabled`On by default

`CasFeatureModule.PasswordlessAuthn.mongo.enabled`On by default

`CasFeatureModule.PasswordlessAuthn.syncope.enabled`On by default

1 auto-configuration class: `CasRadiusAutoConfiguration`

`CasFeatureModule.Radius.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/authentication/SPNEGO-Authentication.html)2 auto-configuration classes: `CasSpnegoAutoConfiguration` and 1 more

`CasFeatureModule.SPNEGO.enabled`On by default

`CasFeatureModule.SPNEGO.ldap.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/authentication/Surrogate-Authentication.html)4 auto-configuration classes: `CasSurrogateJdbcAuthenticationAutoConfiguration` and 3 more

`CasFeatureModule.SurrogateAuthentication.enabled`On by default

`CasFeatureModule.SurrogateAuthentication.jdbc.enabled`On by default

`CasFeatureModule.SurrogateAuthentication.ldap.enabled`On by default

`CasFeatureModule.SurrogateAuthentication.rest.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/authentication/X509-Authentication.html)2 auto-configuration classes: `CasX509AuthenticationAutoConfiguration` and 1 more

`CasFeatureModule.X509.enabled`On by default

`CasFeatureModule.X509.rest.enabled`On by default

Multifactor7 features

[Documentation](https://apereo.github.io/cas/development/mfa/GoogleAuthenticator-Authentication.html)6 auto-configuration classes: `CasGoogleAuthenticatorAutoConfiguration` and 5 more

`CasFeatureModule.GoogleAuthenticator.enabled`On by default

`CasFeatureModule.GoogleAuthenticator.dynamodb.enabled`On by default

`CasFeatureModule.GoogleAuthenticator.jpa.enabled`On by default

`CasFeatureModule.GoogleAuthenticator.ldap.enabled`On by default

`CasFeatureModule.GoogleAuthenticator.mongo.enabled`On by default

`CasFeatureModule.GoogleAuthenticator.redis.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/mfa/Multifactor-TrustedDevice-Authentication.html)11 auto-configuration classes: `CasDynamoDbMultifactorAuthenticationTrustAutoConfiguration` and 10 more

`CasFeatureModule.MultifactorAuthenticationTrustedDevices.enabled`On by default

`CasFeatureModule.MultifactorAuthenticationTrustedDevices.dynamodb.enabled`On by default

`CasFeatureModule.MultifactorAuthenticationTrustedDevices.gauth.enabled`On by default

`CasFeatureModule.MultifactorAuthenticationTrustedDevices.inwebo.enabled`On by default

`CasFeatureModule.MultifactorAuthenticationTrustedDevices.jdbc.enabled`On by default

`CasFeatureModule.MultifactorAuthenticationTrustedDevices.mongo.enabled`On by default

`CasFeatureModule.MultifactorAuthenticationTrustedDevices.radius.enabled`On by default

`CasFeatureModule.MultifactorAuthenticationTrustedDevices.redis.enabled`On by default

`CasFeatureModule.MultifactorAuthenticationTrustedDevices.rest.enabled`On by default

`CasFeatureModule.MultifactorAuthenticationTrustedDevices.webauthn.enabled`On by default

`CasFeatureModule.MultifactorAuthenticationTrustedDevices.yubikey.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication.html)6 auto-configuration classes: `CasCoreMultifactorAuthenticationAutoConfiguration` and 5 more

`CasFeatureModule.MultifactorAuthentication.enabled`On by default

`CasFeatureModule.MultifactorAuthentication.duo.enabled`On by default

`CasFeatureModule.MultifactorAuthentication.grouper.enabled`On by default

`CasFeatureModule.MultifactorAuthentication.inwebo.enabled`On by default

`CasFeatureModule.MultifactorAuthentication.otp.enabled`On by default

`CasFeatureModule.MultifactorAuthentication.twilio.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/mfa/RADIUS-Authentication.html)1 auto-configuration class: `CasRadiusMultifactorAuthenticationAutoConfiguration`

`CasFeatureModule.RadiusMFA.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/mfa/Simple-Multifactor-Authentication.html)1 auto-configuration class: `CasSimpleMultifactorAuthenticationAutoConfiguration`

`CasFeatureModule.SimpleMFA.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/mfa/FIDO2-WebAuthn-Authentication.html)7 auto-configuration classes: `CasDynamoDbWebAuthnAutoConfiguration` and 6 more

`CasFeatureModule.WebAuthn.enabled`On by default

`CasFeatureModule.WebAuthn.dynamodb.enabled`On by default

`CasFeatureModule.WebAuthn.jpa.enabled`On by default

`CasFeatureModule.WebAuthn.ldap.enabled`On by default

`CasFeatureModule.WebAuthn.mongo.enabled`On by default

`CasFeatureModule.WebAuthn.redis.enabled`On by default

`CasFeatureModule.WebAuthn.rest.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/mfa/YubiKey-Authentication.html)5 auto-configuration classes: `CasDynamoDbYubiKeyAutoConfiguration` and 4 more

`CasFeatureModule.YubiKey.enabled`On by default

`CasFeatureModule.YubiKey.dynamodb.enabled`On by default

`CasFeatureModule.YubiKey.jpa.enabled`On by default

`CasFeatureModule.YubiKey.mongo.enabled`On by default

`CasFeatureModule.YubiKey.redis.enabled`On by default

Protocols13 features

[Documentation](https://apereo.github.io/cas/development/integration/ACME-Integration.html)1 auto-configuration class: `CasAcmeAutoConfiguration`

`CasFeatureModule.ACME.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/protocol/OAuth-Protocol.html)2 auto-configuration classes: `CasOAuth20AuthenticationServiceSelectionStrategyConfiguration` and 1 more

`CasFeatureModule.OAuth.enabled`On by default

`CasFeatureModule.OAuth.uma.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/protocol/OIDC-Protocol.html)8 auto-configuration classes: `CasOidcAutoConfiguration` and 7 more

`CasFeatureModule.OpenIDConnect.enabled`On by default

`CasFeatureModule.OpenIDConnect.audit.enabled`On by default

`CasFeatureModule.OpenIDConnect.client-jwks-registration.enabled`Off by default

`CasFeatureModule.OpenIDConnect.federation.enabled`On by default

`CasFeatureModule.OpenIDConnect.jpa.enabled`On by default

`CasFeatureModule.OpenIDConnect.jwks-mongodb.enabled`On by default

`CasFeatureModule.OpenIDConnect.mongodb.enabled`On by default

`CasFeatureModule.OpenIDConnect.verifiable-credentials.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/protocol/REST-Protocol.html)2 auto-configuration classes: `CasCoreRestAutoConfiguration` and 1 more

`CasFeatureModule.RestProtocol.enabled`On by default

`CasFeatureModule.RestProtocol.tokens.enabled`On by default

2 auto-configuration classes: `AmazonS3SamlMetadataConfiguration` and 1 more

`CasFeatureModule.SAML.enabled`On by default

`CasFeatureModule.SAML.aws-s3.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/authentication/Configuring-SAML2-Authentication.html)2 auto-configuration classes: `CasSamlIdPAutoConfiguration` and 1 more

`CasFeatureModule.SAMLIdentityProvider.enabled`On by default

`CasFeatureModule.SAMLIdentityProvider.redis.enabled`On by default

7 auto-configuration classes: `AmazonS3SamlIdPMetadataConfiguration` and 6 more

`CasFeatureModule.SAMLIdentityProviderMetadata.aws-s3.enabled`On by default

`CasFeatureModule.SAMLIdentityProviderMetadata.dynamodb.enabled`On by default

`CasFeatureModule.SAMLIdentityProviderMetadata.gcp.enabled`On by default

`CasFeatureModule.SAMLIdentityProviderMetadata.git.enabled`On by default

`CasFeatureModule.SAMLIdentityProviderMetadata.jpa.enabled`On by default

`CasFeatureModule.SAMLIdentityProviderMetadata.mongo.enabled`On by default

`CasFeatureModule.SAMLIdentityProviderMetadata.rest.enabled`On by default

7 auto-configuration classes: `SamlIdPAmazonS3RegisteredServiceMetadataConfiguration` and 6 more

`CasFeatureModule.SAMLServiceProviderMetadata.aws-s3.enabled`On by default

`CasFeatureModule.SAMLServiceProviderMetadata.dynamodb.enabled`On by default

`CasFeatureModule.SAMLServiceProviderMetadata.git.enabled`On by default

`CasFeatureModule.SAMLServiceProviderMetadata.jpa.enabled`On by default

`CasFeatureModule.SAMLServiceProviderMetadata.mongo.enabled`On by default

`CasFeatureModule.SAMLServiceProviderMetadata.redis.enabled`On by default

`CasFeatureModule.SAMLServiceProviderMetadata.rest.enabled`On by default

1 auto-configuration class: `CasTokenCoreAutoConfiguration`

`CasFeatureModule.Tokens.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/protocol/OAuth-UMA-Protocol.html)1 auto-configuration class: `CasOAuthUmaJpaAutoConfiguration`

`CasFeatureModule.UMA.jpa.enabled`On by default

1 auto-configuration class: `CasCoreValidationAutoConfiguration`

`CasFeatureModule.Validation.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/protocol/WS-Federation-Protocol.html)1 auto-configuration class: `CasWsFederationAuthenticationAutoConfiguration`

`CasFeatureModule.WsFederation.enabled`On by default

1 auto-configuration class: `CasWsSecurityIdentityProviderAutoConfiguration`

`CasFeatureModule.WsFederationIdentityProvider.enabled`On by default

Registries & storage7 features

[Documentation](https://apereo.github.io/cas/development/configuration/Configuration-Server-Management.html)10 auto-configuration classes: `CasAmazonS3BucketsCloudConfigBootstrapAutoConfiguration` and 9 more

`CasFeatureModule.CasConfiguration.enabled`On by default

`CasFeatureModule.CasConfiguration.aws-s3.enabled`On by default

`CasFeatureModule.CasConfiguration.aws-secretsmanager.enabled`On by default

`CasFeatureModule.CasConfiguration.aws-ssm.enabled`On by default

`CasFeatureModule.CasConfiguration.dynamodb.enabled`On by default

`CasFeatureModule.CasConfiguration.etcd.enabled`On by default

`CasFeatureModule.CasConfiguration.gcp-secretsmanager.enabled`On by default

`CasFeatureModule.CasConfiguration.jdbc.enabled`On by default

`CasFeatureModule.CasConfiguration.mongo.enabled`On by default

`CasFeatureModule.CasConfiguration.rest.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/installation/JDBC-Drivers.html)1 auto-configuration class: `CasConditionalDataSourceAutoConfiguration`

`CasFeatureModule.JDBC.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/services/Service-Management.html)14 auto-configuration classes: `CasAmazonS3ServiceRegistryAutoConfiguration` and 13 more

`CasFeatureModule.ServiceRegistry.enabled`On by default

`CasFeatureModule.ServiceRegistry.cassandra.enabled`On by default

`CasFeatureModule.ServiceRegistry.dynamodb.enabled`On by default

`CasFeatureModule.ServiceRegistry.gcp-firestore.enabled`On by default

`CasFeatureModule.ServiceRegistry.gcp-storage.enabled`On by default

`CasFeatureModule.ServiceRegistry.gcp-storage-pubsub.enabled`Off by default

`CasFeatureModule.ServiceRegistry.git.enabled`On by default

`CasFeatureModule.ServiceRegistry.jpa.enabled`On by default

`CasFeatureModule.ServiceRegistry.json.enabled`On by default

`CasFeatureModule.ServiceRegistry.ldap.enabled`On by default

`CasFeatureModule.ServiceRegistry.mongo.enabled`On by default

`CasFeatureModule.ServiceRegistry.redis.enabled`On by default

`CasFeatureModule.ServiceRegistry.rest.enabled`On by default

`CasFeatureModule.ServiceRegistry.s3.enabled`On by default

`CasFeatureModule.ServiceRegistry.yaml.enabled`On by default

2 auto-configuration classes: `CasServicesStreamingAutoConfiguration` and 1 more

`CasFeatureModule.ServiceRegistryStreaming.enabled`On by default

`CasFeatureModule.ServiceRegistryStreaming.kafka.enabled`On by default

4 auto-configuration classes: `CasHazelcastSessionAutoConfiguration` and 3 more

`CasFeatureModule.SessionManagement.hazelcast.enabled`On by default

`CasFeatureModule.SessionManagement.mongo.enabled`On by default

`CasFeatureModule.SessionManagement.redis.enabled`On by default

`CasFeatureModule.SessionManagement.ticket-registry.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/ticketing/Configuring-Ticketing-Components.html)16 auto-configuration classes: `CasAMQPTicketRegistryAutoConfiguration` and 15 more

`CasFeatureModule.TicketRegistry.enabled`On by default

`CasFeatureModule.TicketRegistry.amqp.enabled`On by default

`CasFeatureModule.TicketRegistry.cassandra.enabled`On by default

`CasFeatureModule.TicketRegistry.dynamodb.enabled`On by default

`CasFeatureModule.TicketRegistry.gcp.enabled`On by default

`CasFeatureModule.TicketRegistry.gcp-firestore.enabled`On by default

`CasFeatureModule.TicketRegistry.geode.enabled`On by default

`CasFeatureModule.TicketRegistry.hazelcast.enabled`On by default

`CasFeatureModule.TicketRegistry.ignite.enabled`On by default

`CasFeatureModule.TicketRegistry.jpa.enabled`On by default

`CasFeatureModule.TicketRegistry.kafka.enabled`On by default

`CasFeatureModule.TicketRegistry.memcached.enabled`On by default

`CasFeatureModule.TicketRegistry.mongo.enabled`On by default

`CasFeatureModule.TicketRegistry.pulsar.enabled`On by default

`CasFeatureModule.TicketRegistry.redis.enabled`On by default

`CasFeatureModule.TicketRegistry.redis-messaging.enabled`On by default

`CasFeatureModule.TicketRegistry.redis-modules.enabled`On by default

`CasFeatureModule.TicketRegistry.stateless.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/ticketing/Ticket-Registry-Locking.html)3 auto-configuration classes: `CasHazelcastZooKeeperAutoConfiguration$HazelcastTicketRegistryZooKeeperLockingConfiguration` and 2 more

`CasFeatureModule.TicketRegistryLocking.hazelcast.enabled`On by default

`CasFeatureModule.TicketRegistryLocking.jpa.enabled`On by default

`CasFeatureModule.TicketRegistryLocking.redis.enabled`On by default

Accounts & consent11 features

[Documentation](https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP.html)6 auto-configuration classes: `CasAcceptableUsagePolicyJdbcAutoConfiguration` and 5 more

`CasFeatureModule.AcceptableUsagePolicy.enabled`On by default

`CasFeatureModule.AcceptableUsagePolicy.jdbc.enabled`On by default

`CasFeatureModule.AcceptableUsagePolicy.ldap.enabled`On by default

`CasFeatureModule.AcceptableUsagePolicy.mongo.enabled`On by default

`CasFeatureModule.AcceptableUsagePolicy.redis.enabled`On by default

`CasFeatureModule.AcceptableUsagePolicy.rest.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/registration/Account-Management-Overview.html)1 auto-configuration class: `CasAcceptableUsagePolicyWebflowAutoConfiguration`

`CasFeatureModule.AccountManagement.enabled`Off by default

[Documentation](https://apereo.github.io/cas/development/registration/Account-Registration-Overview.html)2 auto-configuration classes: `CasAccountManagementWebflowAutoConfiguration` and 1 more

`CasFeatureModule.AccountRegistration.enabled`On by default

`CasFeatureModule.AccountRegistration.captcha.enabled`On by default

`CasFeatureModule.AccountRegistration.scim.enabled`On by default

`CasFeatureModule.AccountRegistration.syncope.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/integration/Attribute-Release-Consent.html)7 auto-configuration classes: `CasConsentCoreAutoConfiguration` and 6 more

`CasFeatureModule.Consent.enabled`On by default

`CasFeatureModule.Consent.dynamodb.enabled`On by default

`CasFeatureModule.Consent.jdbc.enabled`On by default

`CasFeatureModule.Consent.ldap.enabled`On by default

`CasFeatureModule.Consent.mongo.enabled`On by default

`CasFeatureModule.Consent.redis.enabled`On by default

`CasFeatureModule.Consent.rest.enabled`On by default

1 auto-configuration class: `PasswordManagementForgotUsernameConfiguration`

`CasFeatureModule.ForgotUsername.enabled`On by default

`CasFeatureModule.ForgotUsername.captcha.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/webflow/Webflow-Customization-Interrupt.html)1 auto-configuration class: `CasInterruptAutoConfiguration`

`CasFeatureModule.InterruptNotifications.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/password_management/Password-Management.html)6 auto-configuration classes: `CasJdbcPasswordManagementAutoConfiguration` and 5 more

`CasFeatureModule.PasswordManagement.enabled`On by default

`CasFeatureModule.PasswordManagement.captcha.enabled`On by default

`CasFeatureModule.PasswordManagement.jdbc.enabled`On by default

`CasFeatureModule.PasswordManagement.ldap.enabled`On by default

`CasFeatureModule.PasswordManagement.password-sync.enabled`On by default

`CasFeatureModule.PasswordManagement.rest.enabled`On by default

`CasFeatureModule.PasswordManagement.syncope.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/password_management/Password-Management-History.html)1 auto-configuration class: `JdbcPasswordHistoryManagementConfiguration`

`CasFeatureModule.PasswordManagementHistory.jdbc.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/integration/Attribute-Resolution.html)8 auto-configuration classes: `CasPersonDirectoryAutoConfiguration` and 7 more

`CasFeatureModule.PersonDirectory.enabled`On by default

`CasFeatureModule.PersonDirectory.groovy.enabled`On by default

`CasFeatureModule.PersonDirectory.grouper.enabled`On by default

`CasFeatureModule.PersonDirectory.json.enabled`On by default

`CasFeatureModule.PersonDirectory.okta.enabled`On by default

`CasFeatureModule.PersonDirectory.rest.enabled`On by default

`CasFeatureModule.PersonDirectory.stub.enabled`On by default

`CasFeatureModule.PersonDirectory.syncope.enabled`On by default

3 auto-configuration classes: `DelegatedAuthenticationProvisioningConfiguration` and 2 more

`CasFeatureModule.Provisioning.okta.enabled`On by default

`CasFeatureModule.Provisioning.pac4j.enabled`On by default

`CasFeatureModule.Provisioning.syncope.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/integration/SCIM-Provisioning.html)1 auto-configuration class: `CasScimAutoConfiguration`

`CasFeatureModule.SCIM.enabled`On by default

Operations14 features

[Documentation](https://apereo.github.io/cas/development/audits/Audits.html)8 auto-configuration classes: `CasCoreAuditAutoConfiguration` and 7 more

`CasFeatureModule.Audit.enabled`On by default

`CasFeatureModule.Audit.aws-firehose.enabled`On by default

`CasFeatureModule.Audit.dynamodb.enabled`On by default

`CasFeatureModule.Audit.javers.enabled`On by default

`CasFeatureModule.Audit.jdbc.enabled`On by default

`CasFeatureModule.Audit.mongo.enabled`On by default

`CasFeatureModule.Audit.redis.enabled`On by default

`CasFeatureModule.Audit.rest.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/installation/Service-Discovery-Guide.html)3 auto-configuration classes: `CasConsulDiscoveryClientAutoConfiguration` and 2 more

`CasFeatureModule.Discovery.enabled`On by default

`CasFeatureModule.Discovery.consul.enabled`On by default

`CasFeatureModule.Discovery.eureka.enabled`On by default

1 auto-configuration class: `CasElectronicFenceAutoConfiguration`

`CasFeatureModule.Electrofence.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Events.html)8 auto-configuration classes: `CasCoreEventsAutoConfiguration` and 7 more

`CasFeatureModule.Events.enabled`On by default

`CasFeatureModule.Events.dynamodb.enabled`On by default

`CasFeatureModule.Events.influxdb.enabled`On by default

`CasFeatureModule.Events.jpa.enabled`On by default

`CasFeatureModule.Events.kafka.enabled`On by default

`CasFeatureModule.Events.memory.enabled`On by default

`CasFeatureModule.Events.mongo.enabled`On by default

`CasFeatureModule.Events.redis.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/authentication/GeoTracking-Authentication-Requests.html)5 auto-configuration classes: `CasGeoLocationAutoConfiguration` and 4 more

`CasFeatureModule.GeoLocation.enabled`On by default

`CasFeatureModule.GeoLocation.azure.enabled`On by default

`CasFeatureModule.GeoLocation.googlemaps.enabled`On by default

`CasFeatureModule.GeoLocation.ip.enabled`On by default

`CasFeatureModule.GeoLocation.maxmind.enabled`On by default

2 auto-configuration classes: `CasAmazonCloudWatchLoggingConfiguration` and 1 more

`CasFeatureModule.Logging.cloudwatch.enabled`On by default

`CasFeatureModule.Logging.gcp.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/monitoring/Configuring-Metrics.html)1 auto-configuration class: `CasGoogleAnalyticsAutoConfiguration`

`CasFeatureModule.Metrics.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/monitoring/Configuring-Monitoring.html)12 auto-configuration classes: `CasAmazonCloudWatchAutoConfiguration` and 11 more

`CasFeatureModule.Monitoring.enabled`On by default

`CasFeatureModule.Monitoring.aws.enabled`On by default

`CasFeatureModule.Monitoring.elastic.enabled`On by default

`CasFeatureModule.Monitoring.hazelcast.enabled`On by default

`CasFeatureModule.Monitoring.jaeger.enabled`On by default

`CasFeatureModule.Monitoring.javamelody.enabled`On by default

`CasFeatureModule.Monitoring.jdbc.enabled`On by default

`CasFeatureModule.Monitoring.ldap.enabled`On by default

`CasFeatureModule.Monitoring.memcached.enabled`On by default

`CasFeatureModule.Monitoring.mongo.enabled`On by default

`CasFeatureModule.Monitoring.redis.enabled`On by default

`CasFeatureModule.Monitoring.sentry.enabled`On by default

17 auto-configuration classes: `CasAPNMessagingAutoConfiguration` and 16 more

`CasFeatureModule.Notifications.enabled`On by default

`CasFeatureModule.Notifications.apn.enabled`On by default

`CasFeatureModule.Notifications.aws-sns.enabled`On by default

`CasFeatureModule.Notifications.clickatell.enabled`On by default

`CasFeatureModule.Notifications.fcm.enabled`On by default

`CasFeatureModule.Notifications.mailgun.enabled`On by default

`CasFeatureModule.Notifications.mailjet.enabled`On by default

`CasFeatureModule.Notifications.mailjet-email.enabled`On by default

`CasFeatureModule.Notifications.mailjet-sms.enabled`On by default

`CasFeatureModule.Notifications.microsoft.enabled`On by default

`CasFeatureModule.Notifications.nexmo.enabled`On by default

`CasFeatureModule.Notifications.sendgrid.enabled`On by default

`CasFeatureModule.Notifications.ses.enabled`On by default

`CasFeatureModule.Notifications.slack.enabled`On by default

`CasFeatureModule.Notifications.smsmode.enabled`On by default

`CasFeatureModule.Notifications.textmagic.enabled`On by default

`CasFeatureModule.Notifications.twilio.enabled`On by default

2 auto-configuration classes: `CasPalantirAutoConfiguration` and 1 more

`CasFeatureModule.Palantir.enabled`On by default

`CasFeatureModule.Palantir.cas-authentication.enabled`Off by default

1 auto-configuration class: `CasReportsAutoConfiguration`

`CasFeatureModule.Reports.enabled`On by default

1 auto-configuration class: `CasSpringBootAdminAutoConfiguration`

`CasFeatureModule.SpringBootAdmin.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Throttling.html)7 auto-configuration classes: `CasBucket4jThrottlingAutoConfiguration` and 6 more

`CasFeatureModule.Throttling.enabled`On by default

`CasFeatureModule.Throttling.bucket4j.enabled`On by default

`CasFeatureModule.Throttling.hazelcast.enabled`On by default

`CasFeatureModule.Throttling.jdbc.enabled`On by default

`CasFeatureModule.Throttling.ldap.enabled`On by default

`CasFeatureModule.Throttling.mongo.enabled`On by default

`CasFeatureModule.Throttling.redis.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/monitoring/Configuring-Tracing.html)2 auto-configuration classes: `CasCoreTracingAutoConfiguration` and 1 more

`CasFeatureModule.Tracing.enabled`On by default

`CasFeatureModule.Tracing.otel.enabled`On by default

Platform11 features

[Documentation](https://apereo.github.io/cas/development/installation/Configuring-Servlet-Container-Embedded.html)1 auto-configuration class: `CasEmbeddedContainerTomcatAutoConfiguration`

`CasFeatureModule.ApacheTomcat.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/authorization/Authorization-Overview.html)1 auto-configuration class: `CasHeimdallAutoConfiguration`

`CasFeatureModule.Authorization.enabled`On by default

1 auto-configuration class: `CasCaptchaAutoConfiguration`

`CasFeatureModule.CAPTCHA.enabled`On by default

4 auto-configuration classes: `CasAmazonCoreAutoConfiguration` and 3 more

`CasFeatureModule.Core.enabled`On by default

`CasFeatureModule.Core.aws.enabled`On by default

`CasFeatureModule.Core.dynamodb.enabled`On by default

`CasFeatureModule.Core.jmx.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/installation/Configuring-Servlet-Container-Embedded-Jetty.html)1 auto-configuration class: `CasEmbeddedContainerJettyAutoConfiguration`

`CasFeatureModule.Jetty.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/installation/Logout-Single-Signout.html)1 auto-configuration class: `CasCoreLogoutAutoConfiguration`

`CasFeatureModule.Logout.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/multitenancy/Multitenancy-Overview.html)1 auto-configuration class: `CasAcceptableUsagePolicyMongoDbAutoConfiguration$CasAcceptableUsagePolicyMongoDbMultitenancyRepositoryConfiguration`

`CasFeatureModule.Multitenancy.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html)1 auto-configuration class: `CasCoreScriptingAutoConfiguration`

`CasFeatureModule.Scripting.enabled`On by default

[Documentation](https://apereo.github.io/cas/development/ux/User-Interface-Thymeleaf.html)1 auto-configuration class: `CasThemesAutoConfiguration`

`CasFeatureModule.Thymeleaf.enabled`On by default

1 auto-configuration class: `CasFiltersConfiguration`

`CasFeatureModule.WebApplication.enabled`On by default

1 auto-configuration class: `CasCoreMultifactorAuthenticationWebflowAutoConfiguration`

`CasFeatureModule.Webflow.enabled`On by default

Other1 feature

1 auto-configuration class: `CasMcpAutoConfiguration`

`CasFeatureModule.AI.mcp.enabled`On by default

`CasFeatureModule.AI.mcp-audit.enabled`On by default

`CasFeatureModule.AI.mcp-consent.enabled`On by default

`CasFeatureModule.AI.mcp-delegated.enabled`On by default

`CasFeatureModule.AI.mcp-electrofence.enabled`On by default

`CasFeatureModule.AI.mcp-heimdall.enabled`On by default

`CasFeatureModule.AI.mcp-interrupt.enabled`On by default

`CasFeatureModule.AI.mcp-oauth.enabled`On by default

`CasFeatureModule.AI.mcp-oidc.enabled`On by default

`CasFeatureModule.AI.mcp-otp.enabled`On by default

`CasFeatureModule.AI.mcp-passwordless.enabled`On by default

`CasFeatureModule.AI.mcp-saml-idp.enabled`On by default

`CasFeatureModule.AI.mcp-settings.enabled`On by default

`CasFeatureModule.AI.mcp-surrogate.enabled`On by default

`CasFeatureModule.AI.mcp-trusted-mfa.enabled`On by default

`CasFeatureModule.AI.mcp-webauthn.enabled`On by default

`CasFeatureModule.AI.mcp-write.enabled`Off by default

No features match this filter.

> **Usage**
>
> Note that not every single CAS feature may be registered in the *Feature Catalog* and as such regarded as a standalone feature. The catalog continues to grow throughout the CAS release lifecycle to recognize more modules as grouped distinct features, allowing for a one-shop store to disable or enable a given CAS feature.

Feature toggles may be specified in the CAS configuration source(s), i.e. `cas.properties` or `cas.yml` file like any other property, or they may be specified as environment variables or system properties. They are treated exactly the same as any other property and are processed during application context initialization.

Note that the above setting enforces conditional access to the auto-configuration class where a whole suite of `@Bean`s would be included or excluded in the application context upon initialization and startup. Conditional inclusion or exclusion of beans generally has consequences when it comes to `@RefreshScope` and [supporting refreshable beans](https://apereo.github.io/cas/development/configuration/Configuration-Management-Reload.html). Note that feature modules are *not refreshable* at this point; they are processed on startup and will either be included in the assembled application context or skipped entirely, depending on the result of the enforced condition.
