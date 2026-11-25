# Spring Cloud Configuration Server - Spring Cloud Azure KeyVault

Source: https://apereo.github.io/cas/development/configuration/Configuration-Server-Management-SpringCloud-AzureKeyVault.html

Spring Cloud Configuration Server is able to use Microsoft Azure’s KeyVault Secrets to locate properties and settings. Support is provided via the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-configuration-cloud-azure-keyvault"
}
```

**IMPORTANT**: The allowed name pattern in Azure Key Vault is `^[0-9a-zA-Z-]+$`. For properties that contain `.` in the name (i.e. `cas.some.property`), replace `.` with `-` when you store the setting in Azure Key Vault (i.e. `cas-some-property`). The module will handle the transformation for you.

> **Usage**
>
> The configuration modules provided here may also be used verbatim inside a CAS server overlay and do not exclusively belong to a Spring Cloud Configuration server. While this module is primarily useful when inside the Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay directly to fetch settings from a source.

The following settings and properties are available from the CAS configuration catalog:

- `spring.cloud.azure.keyvault.secret.challenge-resource-verification-enabled` (Third party; type `Boolean`; default `true`): Whether to enable the Azure Key Vault challenge resource verification, default: true. Calls the disableChallengeResourceVerification method of the Azure Key Vault Client Builder when set to false.
- `spring.cloud.azure.keyvault.secret.client.application-id` (Third party; type `String`): Represents current application and is used for telemetry/monitoring purposes.
- `spring.cloud.azure.keyvault.secret.client.connect-timeout` (Third party; type `Duration`): Amount of time(Duration) the request attempts to connect to the remote host and the connection is resolved.
- `spring.cloud.azure.keyvault.secret.client.connection-idle-timeout` (Third party; type `Duration`): Amount of time(Duration) before an idle connection.
- `spring.cloud.azure.keyvault.secret.client.headers` (Third party; type `List<HeaderProperties>`): List of headers applied to each request sent with client. For instance, '"myCustomHeader", "myStaticValue"'.
- `spring.cloud.azure.keyvault.secret.client.headers[0].name` (Third party; type `String`): The name of the header.
- `spring.cloud.azure.keyvault.secret.client.headers[0].values` (Third party; type `List<String>`): List of values of the header.
- `spring.cloud.azure.keyvault.secret.client.logging.allowed-header-names` (Third party; type `Set<String>`): Comma-delimited list of allowlist headers that should be logged. The default value is `"x-ms-request-id","x-ms-client-request-id","x-ms-return-client-request-id","traceparent","MS-CV","Accept","Cache-Control","Connection","Content-Length","Content-Type","Date","ETag","Expires","If-Match","If-Modified-Since","If-None-Match","If-Unmodified-Since","Last-Modified","Pragma","Request-Id","Retry-After","Server","Transfer-Encoding","User-Agent","WWW-Authenticate"`.
- `spring.cloud.azure.keyvault.secret.client.logging.allowed-query-param-names` (Third party; type `Set<String>`): Comma-delimited list of allowlist query parameters. The default value is `"api-version"`.
- `spring.cloud.azure.keyvault.secret.client.logging.level` (Third party; type `HttpLogDetailLevel`): The level of detail to log on HTTP messages. Supported types are: NONE, BASIC, HEADERS, BODY, BODY_AND_HEADERS. The default value is `NONE`.
- `spring.cloud.azure.keyvault.secret.client.logging.pretty-print-body` (Third party; type `Boolean`): Whether to pretty print the message bodies. The default value is `false`.
- `spring.cloud.azure.keyvault.secret.client.maximum-connection-pool-size` (Third party; type `Integer`): Maximum connection pool size used by the underlying HTTP client.
- `spring.cloud.azure.keyvault.secret.client.read-timeout` (Third party; type `Duration`): Amount of time(Duration) used when reading the server response.
- `spring.cloud.azure.keyvault.secret.client.response-timeout` (Third party; type `Duration`): Amount of time(Duration) used when waiting for a server to reply.
- `spring.cloud.azure.keyvault.secret.client.write-timeout` (Third party; type `Duration`): Amount of time(Duration) each request being sent over the wire.
- `spring.cloud.azure.keyvault.secret.credential.client-certificate-password` (Third party; type `String`): Password of the certificate file.
- `spring.cloud.azure.keyvault.secret.credential.client-certificate-path` (Third party; type `String`): Path of a PEM certificate file to use when performing service principal authentication with Azure.
- `spring.cloud.azure.keyvault.secret.credential.client-id` (Third party; type `String`): Client ID to use when performing service principal authentication with Azure.
- `spring.cloud.azure.keyvault.secret.credential.client-secret` (Third party; type `String`): Client secret to use when performing service principal authentication with Azure.
- `spring.cloud.azure.keyvault.secret.credential.managed-identity-enabled` (Third party; type `Boolean`; default `false`): Whether to enable managed identity to authenticate with Azure. If true and the client-id is set, will use the client ID as user assigned managed identity client ID.
- `spring.cloud.azure.keyvault.secret.credential.password` (Third party; type `String`): Password to use when performing username/password authentication with Azure.
- `spring.cloud.azure.keyvault.secret.credential.token-credential-bean-name` (Third party; type `String`): The bean name of type 'com.azure.core.credential.TokenCredential' to use when performing authentication with Azure.
- `spring.cloud.azure.keyvault.secret.credential.username` (Third party; type `String`): Username to use when performing username/password authentication with Azure.
- `spring.cloud.azure.keyvault.secret.enabled` (Third party; type `Boolean`; default `true`): Whether an Azure Service is enabled.
- `spring.cloud.azure.keyvault.secret.endpoint` (Third party; type `String`): Azure Key Vault endpoint. For instance, 'https://{your-unique-keyvault-name}.vault.azure.net/'.
- `spring.cloud.azure.keyvault.secret.profile.cloud-type` (Third party; type `AzureProfileOptionsProvider.CloudType`): Name of the Azure cloud to connect to. Supported types are: 'AZURE', 'AZURE_CHINA', 'AZURE_US_GOVERNMENT', 'OTHER'. The default value is 'AZURE'.
- `spring.cloud.azure.keyvault.secret.profile.environment.active-directory-endpoint` (Third party; type `String`): The Microsoft Entra endpoint to connect to.
- `spring.cloud.azure.keyvault.secret.profile.environment.active-directory-graph-api-version` (Third party; type `String`): The Azure Active Directory Graph API version.
- `spring.cloud.azure.keyvault.secret.profile.environment.active-directory-graph-endpoint` (Third party; type `String`): The Azure Active Directory Graph endpoint.
- `spring.cloud.azure.keyvault.secret.profile.environment.active-directory-resource-id` (Third party; type `String`): The Microsoft Entra resource ID.
- `spring.cloud.azure.keyvault.secret.profile.environment.azure-application-insights-endpoint` (Third party; type `String`): The Azure Application Insights endpoint.
- `spring.cloud.azure.keyvault.secret.profile.environment.azure-data-lake-analytics-catalog-and-job-endpoint-suffix` (Third party; type `String`): The Data Lake analytics catalog and job endpoint suffix.
- `spring.cloud.azure.keyvault.secret.profile.environment.azure-data-lake-store-file-system-endpoint-suffix` (Third party; type `String`): The Data Lake storage file system endpoint suffix.
- `spring.cloud.azure.keyvault.secret.profile.environment.azure-log-analytics-endpoint` (Third party; type `String`): The Azure Log Analytics endpoint.
- `spring.cloud.azure.keyvault.secret.profile.environment.data-lake-endpoint-resource-id` (Third party; type `String`): The Data Lake endpoint.
- `spring.cloud.azure.keyvault.secret.profile.environment.gallery-endpoint` (Third party; type `String`): The gallery endpoint.
- `spring.cloud.azure.keyvault.secret.profile.environment.key-vault-dns-suffix` (Third party; type `String`): The Key Vault DNS suffix.
- `spring.cloud.azure.keyvault.secret.profile.environment.management-endpoint` (Third party; type `String`): The management service endpoint.
- `spring.cloud.azure.keyvault.secret.profile.environment.microsoft-graph-endpoint` (Third party; type `String`): The Microsoft Graph endpoint.
- `spring.cloud.azure.keyvault.secret.profile.environment.portal` (Third party; type `String`): The management portal URL.
- `spring.cloud.azure.keyvault.secret.profile.environment.publishing-profile` (Third party; type `String`): The publishing settings file URL.
- `spring.cloud.azure.keyvault.secret.profile.environment.resource-manager-endpoint` (Third party; type `String`): The resource management endpoint.
- `spring.cloud.azure.keyvault.secret.profile.environment.service-bus-domain-name` (Third party; type `String`): The domain name for Service Bus.
- `spring.cloud.azure.keyvault.secret.profile.environment.sql-management-endpoint` (Third party; type `String`): The SQL management endpoint.
- `spring.cloud.azure.keyvault.secret.profile.environment.sql-server-hostname-suffix` (Third party; type `String`): The SQL Server hostname suffix.
- `spring.cloud.azure.keyvault.secret.profile.environment.storage-endpoint-suffix` (Third party; type `String`): The Storage endpoint suffix.
- `spring.cloud.azure.keyvault.secret.profile.subscription-id` (Third party; type `String`): Subscription ID to use when connecting to Azure resources.
- `spring.cloud.azure.keyvault.secret.profile.tenant-id` (Third party; type `String`): Tenant ID for Azure resources. The values allowed for 'tenant-id' are: 'common', 'organizations', 'consumers', or the tenant ID.
- `spring.cloud.azure.keyvault.secret.property-source-enabled` (Third party; type `Boolean`; default `true`): Whether to enable the Key Vault property source.
- `spring.cloud.azure.keyvault.secret.property-sources` (Third party; type `List<AzureKeyVaultPropertySourceProperties>`): List of Azure Key Vault property sources. For instance, ' property-sources[0].name=key-vault-property-source-1, property-sources[0].endpoint={ENDPOINT_1}, property-sources[1].name=key-vault-property-source-2, property-sources[1].endpoint={ENDPOINT_2} '.
- `spring.cloud.azure.keyvault.secret.property-sources[0].case-sensitive` (Third party; type `Boolean`): Whether to enable case-sensitive for secret keys. The default value is `false`.
- `spring.cloud.azure.keyvault.secret.property-sources[0].challenge-resource-verification-enabled` (Third party; type `Boolean`; default `true`): Whether to enable the Azure Key Vault challenge resource verification, default: true. Calls the disableChallengeResourceVerification method of the Azure Key Vault Client Builder when set to false.
- `spring.cloud.azure.keyvault.secret.property-sources[0].client.application-id` (Third party; type `String`): Represents current application and is used for telemetry/monitoring purposes.
- `spring.cloud.azure.keyvault.secret.property-sources[0].client.connect-timeout` (Third party; type `Duration`): Amount of time(Duration) the request attempts to connect to the remote host and the connection is resolved.
- `spring.cloud.azure.keyvault.secret.property-sources[0].client.connection-idle-timeout` (Third party; type `Duration`): Amount of time(Duration) before an idle connection.
- `spring.cloud.azure.keyvault.secret.property-sources[0].client.headers[0].name` (Third party; type `String`): The name of the header.
- `spring.cloud.azure.keyvault.secret.property-sources[0].client.headers[0].values` (Third party; type `List<String>`): List of values of the header.
- `spring.cloud.azure.keyvault.secret.property-sources[0].client.logging.allowed-header-names` (Third party; type `Set<String>`): Comma-delimited list of allowlist headers that should be logged. The default value is `'x-ms-request-id','x-ms-client-request-id','x-ms-return-client-request-id','traceparent','MS-CV','Accept','Cache-Control','Connection','Content-Length','Content-Type','Date','ETag','Expires','If-Match','If-Modified-Since','If-None-Match','If-Unmodified-Since','Last-Modified','Pragma','Request-Id','Retry-After','Server','Transfer-Encoding','User-Agent','WWW-Authenticate'`.
- `spring.cloud.azure.keyvault.secret.property-sources[0].client.logging.allowed-query-param-names` (Third party; type `Set<String>`): Comma-delimited list of allowlist query parameters. The default value is `'api-version'`.
- `spring.cloud.azure.keyvault.secret.property-sources[0].client.logging.level` (Third party; type `HttpLogDetailLevel`): The level of detail to log on HTTP messages. Supported types are: NONE, BASIC, HEADERS, BODY, BODY_AND_HEADERS. The default value is `NONE`.
- `spring.cloud.azure.keyvault.secret.property-sources[0].client.logging.pretty-print-body` (Third party; type `Boolean`): Whether to pretty print the message bodies. The default value is `false`.
- `spring.cloud.azure.keyvault.secret.property-sources[0].client.maximum-connection-pool-size` (Third party; type `Integer`): Maximum connection pool size used by the underlying HTTP client.
- `spring.cloud.azure.keyvault.secret.property-sources[0].client.read-timeout` (Third party; type `Duration`): Amount of time(Duration) used when reading the server response.
- `spring.cloud.azure.keyvault.secret.property-sources[0].client.response-timeout` (Third party; type `Duration`): Amount of time(Duration) used when waiting for a server to reply.
- `spring.cloud.azure.keyvault.secret.property-sources[0].client.write-timeout` (Third party; type `Duration`): Amount of time(Duration) each request being sent over the wire.
- `spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-certificate-password` (Third party; type `String`): Password of the certificate file.
- `spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-certificate-path` (Third party; type `String`): Path of a PEM certificate file to use when performing service principal authentication with Azure.
- `spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-id` (Third party; type `String`): Client ID to use when performing service principal authentication with Azure.
- `spring.cloud.azure.keyvault.secret.property-sources[0].credential.client-secret` (Third party; type `String`): Client secret to use when performing service principal authentication with Azure.
- `spring.cloud.azure.keyvault.secret.property-sources[0].credential.managed-identity-enabled` (Third party; type `Boolean`; default `false`): Whether to enable managed identity to authenticate with Azure. If true and the client-id is set, will use the client ID as user assigned managed identity client ID.
- `spring.cloud.azure.keyvault.secret.property-sources[0].credential.password` (Third party; type `String`): Password to use when performing username/password authentication with Azure.
- `spring.cloud.azure.keyvault.secret.property-sources[0].credential.username` (Third party; type `String`): Username to use when performing username/password authentication with Azure.
- `spring.cloud.azure.keyvault.secret.property-sources[0].enabled` (Third party; type `Boolean`; default `true`): Whether an Azure Service is enabled.
- `spring.cloud.azure.keyvault.secret.property-sources[0].endpoint` (Third party; type `String`): Azure Key Vault endpoint. For instance, 'https://{your-unique-keyvault-name}.vault.azure.net/'.
- `spring.cloud.azure.keyvault.secret.property-sources[0].name` (Third party; type `String`): Name of this property source.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.cloud-type` (Third party; type `AzureProfileOptionsProvider.CloudType`): Name of the Azure cloud to connect to. Supported types are: AZURE, AZURE_CHINA, AZURE_US_GOVERNMENT, OTHER. The default value is `AZURE`.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.active-directory-endpoint` (Third party; type `String`): The Microsoft Entra endpoint to connect to.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.active-directory-graph-api-version` (Third party; type `String`): The Azure Active Directory Graph API version.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.active-directory-graph-endpoint` (Third party; type `String`): The Azure Active Directory Graph endpoint.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.active-directory-resource-id` (Third party; type `String`): The Microsoft Entra resource ID.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.azure-application-insights-endpoint` (Third party; type `String`): The Azure Application Insights endpoint.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.azure-data-lake-analytics-catalog-and-job-endpoint-suffix` (Third party; type `String`): The Data Lake analytics catalog and job endpoint suffix.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.azure-data-lake-store-file-system-endpoint-suffix` (Third party; type `String`): The Data Lake storage file system endpoint suffix.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.azure-log-analytics-endpoint` (Third party; type `String`): The Azure Log Analytics endpoint.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.data-lake-endpoint-resource-id` (Third party; type `String`): The Data Lake endpoint.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.gallery-endpoint` (Third party; type `String`): The gallery endpoint.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.key-vault-dns-suffix` (Third party; type `String`): The Key Vault DNS suffix.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.management-endpoint` (Third party; type `String`): The management service endpoint.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.microsoft-graph-endpoint` (Third party; type `String`): The Microsoft Graph endpoint.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.portal` (Third party; type `String`): The management portal URL.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.publishing-profile` (Third party; type `String`): The publishing settings file URL.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.resource-manager-endpoint` (Third party; type `String`): The resource management endpoint.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.sql-management-endpoint` (Third party; type `String`): The SQL management endpoint.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.sql-server-hostname-suffix` (Third party; type `String`): The SQL Server hostname suffix.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.environment.storage-endpoint-suffix` (Third party; type `String`): The Storage endpoint suffix.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.subscription-id` (Third party; type `String`): Subscription ID to use when connecting to Azure resources.
- `spring.cloud.azure.keyvault.secret.property-sources[0].profile.tenant-id` (Third party; type `String`): Tenant ID for Azure resources. The values allowed for 'tenant-id' are: 'common', 'organizations', 'consumers', or the tenant ID.
- `spring.cloud.azure.keyvault.secret.property-sources[0].proxy.hostname` (Third party; type `String`): The host of the proxy.
- `spring.cloud.azure.keyvault.secret.property-sources[0].proxy.non-proxy-hosts` (Third party; type `String`): A list of hosts or CIDR to not use proxy HTTP/HTTPS connections through.
- `spring.cloud.azure.keyvault.secret.property-sources[0].proxy.password` (Third party; type `String`): Password used to authenticate with the proxy.
- `spring.cloud.azure.keyvault.secret.property-sources[0].proxy.port` (Third party; type `Integer`): The port of the proxy.
- `spring.cloud.azure.keyvault.secret.property-sources[0].proxy.type` (Third party; type `String`): The type of the proxy. For instance of http, 'http', 'socks4', 'socks5'. For instance of amqp, 'http', 'socks'.
- `spring.cloud.azure.keyvault.secret.property-sources[0].proxy.username` (Third party; type `String`): Username used to authenticate with the proxy.
- `spring.cloud.azure.keyvault.secret.property-sources[0].resource.region` (Third party; type `String`): The region of an Azure resource. For instance, '"eastus"'.
- `spring.cloud.azure.keyvault.secret.property-sources[0].resource.resource-group` (Third party; type `String`): The resource group holds an Azure resource.
- `spring.cloud.azure.keyvault.secret.property-sources[0].resource.resource-id` (Third party; type `String`): ID of an Azure resource.
- `spring.cloud.azure.keyvault.secret.property-sources[0].retry.exponential.base-delay` (Third party; type `Duration`): Amount of time(Duration) to wait between retry attempts.
- `spring.cloud.azure.keyvault.secret.property-sources[0].retry.exponential.max-delay` (Third party; type `Duration`): Maximum permissible amount of time(duration) between retry attempts.
- `spring.cloud.azure.keyvault.secret.property-sources[0].retry.exponential.max-retries` (Third party; type `Integer`): The maximum number of attempts.
- `spring.cloud.azure.keyvault.secret.property-sources[0].retry.fixed.delay` (Third party; type `Duration`): Amount of time(Duration) to wait between retry attempts.
- `spring.cloud.azure.keyvault.secret.property-sources[0].retry.fixed.max-retries` (Third party; type `Integer`): The maximum number of attempts.
- `spring.cloud.azure.keyvault.secret.property-sources[0].retry.mode` (Third party; type `RetryOptionsProvider.RetryMode`): The retry backoff mode when retrying. Supported types are: FIXED, EXPONENTIAL.
- `spring.cloud.azure.keyvault.secret.property-sources[0].secret-keys` (Third party; type `List<String>`): The configured secret keys will be loaded from Azure Key Vaults secret, if configured nothing, then load all the secrets. Only support exact value for secret names, For example, if you configured secret key name `SecretKey1` in Key Vaults secret, you should configure 'SecretKey1' here.
- `spring.cloud.azure.keyvault.secret.property-sources[0].service-version` (Third party; type `SecretServiceVersion`): Secret service version used when making API requests.
- `spring.cloud.azure.keyvault.secret.proxy.hostname` (Third party; type `String`): The host of the proxy.
- `spring.cloud.azure.keyvault.secret.proxy.non-proxy-hosts` (Third party; type `String`): A list of hosts or CIDR to not use proxy HTTP/HTTPS connections through.
- `spring.cloud.azure.keyvault.secret.proxy.password` (Third party; type `String`): Password used to authenticate with the proxy.
- `spring.cloud.azure.keyvault.secret.proxy.port` (Third party; type `Integer`): The port of the proxy.
- `spring.cloud.azure.keyvault.secret.proxy.type` (Third party; type `String`): The type of the proxy. For instance of http, 'http', 'socks4', 'socks5'. For instance of amqp, 'http', 'socks'.
- `spring.cloud.azure.keyvault.secret.proxy.username` (Third party; type `String`): Username used to authenticate with the proxy.
- `spring.cloud.azure.keyvault.secret.resource.region` (Third party; type `String`): The region of an Azure resource. For instance, '"eastus"'.
- `spring.cloud.azure.keyvault.secret.resource.resource-group` (Third party; type `String`): The resource group holds an Azure resource.
- `spring.cloud.azure.keyvault.secret.resource.resource-id` (Third party; type `String`): ID of an Azure resource.
- `spring.cloud.azure.keyvault.secret.retry.exponential.base-delay` (Third party; type `Duration`): Amount of time(Duration) to wait between retry attempts.
- `spring.cloud.azure.keyvault.secret.retry.exponential.max-delay` (Third party; type `Duration`): Maximum permissible amount of time(duration) between retry attempts.
- `spring.cloud.azure.keyvault.secret.retry.exponential.max-retries` (Third party; type `Integer`): The maximum number of attempts.
- `spring.cloud.azure.keyvault.secret.retry.fixed.delay` (Third party; type `Duration`): Amount of time(Duration) to wait between retry attempts.
- `spring.cloud.azure.keyvault.secret.retry.fixed.max-retries` (Third party; type `Integer`): The maximum number of attempts.
- `spring.cloud.azure.keyvault.secret.retry.mode` (Third party; type `RetryOptionsProvider.RetryMode`): The retry backoff mode when retrying. Supported types are: FIXED, EXPONENTIAL.
- `spring.cloud.azure.keyvault.secret.service-version` (Third party; type `SecretServiceVersion`): Secret service version used when making API requests.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.
