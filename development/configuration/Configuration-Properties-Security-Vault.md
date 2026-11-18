# Configuration Security - Vault

Source: https://apereo.github.io/cas/development/configuration/Configuration-Properties-Security-Vault.html

You can also store sensitive settings inside [Vault](https://www.vaultproject.io/). Vault can store your existing secrets, or it can dynamically generate new secrets to control access to third-party resources or provide time-limited credentials for your infrastructure. To learn more about Vault and its installation process, please visit the project website.

Once vault is accessible and configured inside CAS, support is provided via the following dependency:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-configuration-cloud-vault"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `spring.cloud.vault.app-role.app-role-path` (Third party; type `String`; default `approle`): Mount path of the AppRole authentication backend.
- `spring.cloud.vault.app-role.role` (Third party; type `String`): Name of the role, optional, used for pull-mode.
- `spring.cloud.vault.app-role.role-id` (Third party; type `String`): The RoleId.
- `spring.cloud.vault.app-role.secret-id` (Third party; type `String`): The SecretId.
- `spring.cloud.vault.application-name` (Third party; type `String`; default `application`): Application name for AppId authentication.
- `spring.cloud.vault.authentication` (Third party; type `VaultProperties.AuthenticationMethod`; default `token`)
- `spring.cloud.vault.aws-ec2.aws-ec2-path` (Third party; type `String`; default `aws-ec2`): Mount path of the AWS-EC2 authentication backend.
- `spring.cloud.vault.aws-ec2.identity-document` (Third party; type `URI`; default `http://169.254.169.254/latest/dynamic/instance-identity/pkcs7`): URL of the AWS-EC2 PKCS7 identity document.
- `spring.cloud.vault.aws-ec2.nonce` (Third party; type `String`): Nonce used for AWS-EC2 authentication. An empty nonce defaults to nonce generation.
- `spring.cloud.vault.aws-ec2.role` (Third party; type `String`): Name of the role, optional.
- `spring.cloud.vault.aws-iam.aws-path` (Third party; type `String`; default `aws`): Mount path of the AWS authentication backend.
- `spring.cloud.vault.aws-iam.endpoint-uri` (Third party; type `URI`): STS server URI. @since 2.2
- `spring.cloud.vault.aws-iam.region` (Third party; type `String`): Name of the region, optional. Inferred by AWS defaults if not set. @since 4.0.1
- `spring.cloud.vault.aws-iam.role` (Third party; type `String`): Name of the role, optional. Defaults to the friendly IAM name if not set.
- `spring.cloud.vault.aws-iam.server-name` (Third party; type `String`): Name of the server used to set `X-Vault-AWS-IAM-Server-ID` header in the headers of login requests.
- `spring.cloud.vault.azure-msi.azure-path` (Third party; type `String`; default `azure`): Mount path of the Azure MSI authentication backend.
- `spring.cloud.vault.azure-msi.identity-token-service` (Third party; type `URI`): Identity token service URI. @since 3.0
- `spring.cloud.vault.azure-msi.metadata-service` (Third party; type `URI`): Instance metadata service URI. @since 3.0
- `spring.cloud.vault.azure-msi.role` (Third party; type `String`): Name of the role.
- `spring.cloud.vault.config.lifecycle.enabled` (Third party; type `Boolean`; default `true`): Enable lifecycle management.
- `spring.cloud.vault.config.lifecycle.expiry-threshold` (Third party; type `Duration`): The expiry threshold. `Lease` is renewed the given `Duration` before it expires. @since 2.2
- `spring.cloud.vault.config.lifecycle.lease-endpoints` (Third party; type `LeaseEndpoints`): Set the `LeaseEndpoints` to delegate renewal/revocation calls to. `LeaseEndpoints` encapsulates differences between Vault versions that affect the location of renewal/revocation endpoints. Can be `LeaseEndpoints#Leases` for version 0.8 or above of Vault or `LeaseEndpoints#Legacy` for older versions (the default). @since 2.2
- `spring.cloud.vault.config.lifecycle.lease-strategy` (Third party; type `VaultProperties.PredefinedLeaseStrategy`): Sets the `LeaseStrategy` to be used with `org.springframework.vault.core.lease.SecretLeaseContainer#setLeaseStrategy(LeaseStrategy)` to retain or drop tokens on renewal errors. @since 4.1
- `spring.cloud.vault.config.lifecycle.min-renewal` (Third party; type `Duration`): The time period that is at least required before renewing a lease. @since 2.2
- `spring.cloud.vault.config.order` (Third party; type `Integer`; deprecation `WARNING`, no replacement; default `0`): Used to set a `org.springframework.core.env.PropertySource` priority. This is useful to use Vault as an override on other property sources. @see org.springframework.core.PriorityOrdered
- `spring.cloud.vault.connection-timeout` (Third party; type `Integer`; default `5000`): Connection timeout.
- `spring.cloud.vault.discovery.enabled` (Third party; type `Boolean`; default `false`): Flag to indicate that Vault server discovery is enabled (vault server URL will be looked up via discovery).
- `spring.cloud.vault.discovery.service-id` (Third party; type `String`; default `vault`): Service id to locate Vault.
- `spring.cloud.vault.enabled` (Third party; type `Boolean`; default `true`): Enable Vault config server.
- `spring.cloud.vault.fail-fast` (Third party; type `Boolean`; default `false`): Fail fast if data cannot be obtained from Vault.
- `spring.cloud.vault.gcp-gce.gcp-path` (Third party; type `String`; default `gcp`): Mount path of the Kubernetes authentication backend.
- `spring.cloud.vault.gcp-gce.role` (Third party; type `String`): Name of the role against which the login is being attempted.
- `spring.cloud.vault.gcp-gce.service-account` (Third party; type `String`): Optional service account id. Using the default id if left unconfigured.
- `spring.cloud.vault.gcp-iam.credentials.encoded-key` (Third party; type `String`): The base64 encoded contents of an OAuth2 account private key in JSON format.
- `spring.cloud.vault.gcp-iam.credentials.location` (Third party; type `Resource`): Location of the OAuth2 credentials private key. Since this is a Resource, the private key can be in a multitude of locations, such as a local file system, classpath, URL, etc.
- `spring.cloud.vault.gcp-iam.gcp-path` (Third party; type `String`; default `gcp`): Mount path of the Kubernetes authentication backend.
- `spring.cloud.vault.gcp-iam.jwt-validity` (Third party; type `Duration`; default `15m`): Validity of the JWT token.
- `spring.cloud.vault.gcp-iam.project-id` (Third party; type `String`): Overrides the GCP project Id.
- `spring.cloud.vault.gcp-iam.role` (Third party; type `String`): Name of the role against which the login is being attempted.
- `spring.cloud.vault.gcp-iam.service-account-id` (Third party; type `String`): Overrides the GCP service account Id.
- `spring.cloud.vault.github.github-path` (Third party; type `String`; default `github`): Mount path of the GitHub authentication backend.
- `spring.cloud.vault.github.token` (Third party; type `String`): GitHub personal token.
- `spring.cloud.vault.host` (Third party; type `String`; default `localhost`): Vault server host.
- `spring.cloud.vault.kubernetes.kubernetes-path` (Third party; type `String`; default `kubernetes`): Mount path of the Kubernetes authentication backend.
- `spring.cloud.vault.kubernetes.role` (Third party; type `String`): Name of the role against which the login is being attempted.
- `spring.cloud.vault.kubernetes.service-account-token-file` (Third party; type `String`; default `/var/run/secrets/kubernetes.io/serviceaccount/token`): Path to the service account token file.
- `spring.cloud.vault.kv.application-name` (Third party; type `String`; default `application`): Application name to be used for the context.
- `spring.cloud.vault.kv.backend` (Third party; type `String`; default `secret`): Name of the default backend.
- `spring.cloud.vault.kv.backend-version` (Third party; type `Integer`; deprecation `WARNING`, no replacement; default `2`): Key-Value backend version. Currently supported versions are: - Version 1 (unversioned key-value backend). - Version 2 (versioned key-value backend).
- `spring.cloud.vault.kv.default-context` (Third party; type `String`; default `application`): Name of the default context.
- `spring.cloud.vault.kv.enabled` (Third party; type `Boolean`; default `true`): Enable the key-value backend.
- `spring.cloud.vault.kv.profile-separator` (Third party; type `String`; default `/`): Profile-separator to combine application name and profile.
- `spring.cloud.vault.kv.profiles` (Third party; type `List<String>`): List of active profiles. @since 3.0
- `spring.cloud.vault.namespace` (Third party; type `String`): Vault namespace (requires Vault Enterprise).
- `spring.cloud.vault.pcf.instance-certificate` (Third party; type `Resource`): Path to the instance certificate (PEM). Defaults to `CF_INSTANCE_CERT` env variable.
- `spring.cloud.vault.pcf.instance-key` (Third party; type `Resource`): Path to the instance key (PEM). Defaults to `CF_INSTANCE_KEY` env variable.
- `spring.cloud.vault.pcf.pcf-path` (Third party; type `String`; default `pcf`): Mount path of the Kubernetes authentication backend.
- `spring.cloud.vault.pcf.role` (Third party; type `String`): Name of the role against which the login is being attempted.
- `spring.cloud.vault.port` (Third party; type `Integer`; default `8200`): Vault server port.
- `spring.cloud.vault.reactive.enabled` (Third party; type `Boolean`; default `true`): Flag to indicate that reactive discovery is enabled.
- `spring.cloud.vault.read-timeout` (Third party; type `Integer`; default `15000`): Read timeout.
- `spring.cloud.vault.scheme` (Third party; type `String`; default `https`): Protocol scheme. Can be either "http" or "https".
- `spring.cloud.vault.session.lifecycle.enabled` (Third party; type `Boolean`; default `true`): Enable session lifecycle management.
- `spring.cloud.vault.session.lifecycle.expiry-threshold` (Third party; type `Duration`; default `7s`): The expiry threshold for a `LoginToken`. The threshold represents a minimum TTL duration to consider a login token as valid. Tokens with a shorter TTL are considered expired and are not used anymore. Should be greater than `refreshBeforeExpiry` to prevent token expiry.
- `spring.cloud.vault.session.lifecycle.refresh-before-expiry` (Third party; type `Duration`; default `5s`): The time period that is at least required before renewing the `LoginToken`.
- `spring.cloud.vault.ssl.bundle` (Third party; type `Map<String,VaultSslBundlesProperties.VaultSslBundle>`): Map of named Vault-managed SSL bundles to issue private keys along with certificates.
- `spring.cloud.vault.ssl.cert-auth-path` (Third party; type `String`; default `cert`): Mount path of the TLS cert authentication backend.
- `spring.cloud.vault.ssl.enabled-cipher-suites` (Third party; type `List<String>`): List of enabled SSL/TLS cipher suites. @since 3.0.2
- `spring.cloud.vault.ssl.enabled-protocols` (Third party; type `List<String>`): List of enabled SSL/TLS protocol. @since 3.0.2
- `spring.cloud.vault.ssl.key-store` (Third party; type `Resource`): Trust store that holds certificates and private keys.
- `spring.cloud.vault.ssl.key-store-password` (Third party; type `String`): Password used to access the key store.
- `spring.cloud.vault.ssl.key-store-type` (Third party; type `String`): Type of the key store. @since 3.0
- `spring.cloud.vault.ssl.lifecycle.expiry-threshold` (Third party; type `Duration`; default `1m`): Expiry threshold to trigger certificate renewal before actual expiry.
- `spring.cloud.vault.ssl.lifecycle.pki-mount` (Third party; type `String`; default `pki`): Mount path of the PKI backend in Vault.
- `spring.cloud.vault.ssl.lifecycle.store.enabled` (Third party; type `Boolean`; default `false`): Whether to enable caching of certificates using a versioned key/value secret backend.
- `spring.cloud.vault.ssl.lifecycle.store.path` (Third party; type `String`; default `secret`): Path to the certificate store used to store the certificate bundle.
- `spring.cloud.vault.ssl.role` (Third party; type `String`): Name of the role against which the login is being attempted. @since 5.0
- `spring.cloud.vault.ssl.trust-bundle` (Third party; type `Map<String,VaultSslBundlesProperties.VaultIssuerCertificateBundle>`): Map of named Vault-managed trust bundles that contain CA issuer certificates for trust anchor usage.
- `spring.cloud.vault.ssl.trust-store` (Third party; type `Resource`): Trust store that holds SSL certificates.
- `spring.cloud.vault.ssl.trust-store-password` (Third party; type `String`): Password used to access the trust store.
- `spring.cloud.vault.ssl.trust-store-type` (Third party; type `String`): Type of the trust store. @since 3.0
- `spring.cloud.vault.token` (Third party; type `String`): Static vault token. Required if `#authentication` is `TOKEN`.
- `spring.cloud.vault.uri` (Third party; type `String`): Vault URI. Can be set with scheme, host and port.

With CAS, secrets are picked up at startup of the application server. CAS uses the data and settings from the application name (i.e. `cas`) and active profiles to determine contexts paths in which secrets should be stored and later fetched.

These context paths typically are:

```bash
/secret/{application}/{profile}
/secret/{application}
```

As an example, you may write the following CAS setting to Vault:

```bash
vault write secret/cas/native <setting-name>=<value>
```

CAS will execute the equivalent of the following command to read settings later when needed:

```bash
vault read secret/cas/native
```

All settings and secrets that are stored inside Vault may be reloaded at any given time. To learn more about how CAS allows you to reload configuration changes, please [review this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Reload.html). To learn more about how configuration is managed and profiled by CAS, please [review this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management.html).

# Troubleshooting

To enable additional logging, modify the logging configuration file to add the following:

```xml
<Logger name="org.springframework.cloud.vault" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
```
