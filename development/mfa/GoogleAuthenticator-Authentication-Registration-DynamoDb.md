# DynamoDb Google Authenticator Registration

Source: https://apereo.github.io/cas/development/mfa/GoogleAuthenticator-Authentication-Registration-DynamoDb.html

Registration records and tokens may be kept inside a DynamoDb instance via the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-gauth-dynamodb"
}
```

Registration records are keyed by their identifier, and looked up by user through a global secondary index named `useridIndex` on the `userid` attribute (projecting all attributes). CAS creates the index along with the table, and adds it to an existing table at startup.

> **Usage Warning**
>
> When table creation on startup is turned off, the index must be created ahead of time. Lookups by user fail while the index is missing or still being built on an existing table.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.gauth.dynamo-db.credential-access-key` (Required; type `String`): Use access-key provided by AWS to authenticate.
- `cas.authn.mfa.gauth.dynamo-db.credential-secret-key` (Required; type `String`): Use secret key provided by AWS to authenticate.
- `cas.authn.mfa.gauth.dynamo-db.dax.url` (Required; type `String`): Cluster url. For example, `dax://my-cluster.l6fzcv.dax-clusters.us-east-1.amazonaws.com`.
- `cas.authn.mfa.gauth.dynamo-db.endpoint` (Required; type `String`): AWS custom endpoint.
- `cas.authn.mfa.gauth.dynamo-db.region` (Required; type `String`): AWS region used.
- `cas.authn.mfa.gauth.dynamo-db.billing-mode` (Optional; type `AbstractDynamoDbProperties.BillingMode`; default `PROVISIONED`): Billing mode specifies how you are charged for read and write throughput and how you manage capacity. Available values are as follows: - `PROVISIONED`: Provisioned mode means that you specify the number of reads and writes per second that you expect your application to use. Provisioned mode is a good option if any of the following are true: - You have predictable application traffic. - You run applications whose traffic is consistent or ramps gradually. - You can forecast capacity requirements to control costs. You can use auto scaling to automatically adjust capacity based on the specified utilization rate to ensure application performance while reducing costs. `PAY_PER_REQUEST`: Pay-per-request or on-demand billing means that you're charged for only the read/write requests that you use. On-demand mode is a good option if any of the following are true: You create new tables with unknown workloads. You have unpredictable application traffic. You prefer the ease of paying for only what you use. Tables using on-demand mode support all DynamoDB features (such as encryption at rest, point-in-time recovery, global tables, and so on) with the exception of auto scaling, which is not applicable with this mode.
- `cas.authn.mfa.gauth.dynamo-db.client-execution-timeout` (Optional; type `String`; default `PT10S`): Client execution timeout.
- `cas.authn.mfa.gauth.dynamo-db.connection-timeout` (Optional; type `String`; default `PT5S`): Connection timeout.
- `cas.authn.mfa.gauth.dynamo-db.dax.connect-timeout` (Optional; type `String`; default `PT5S`): Connection timeout, calculated in milliseconds.
- `cas.authn.mfa.gauth.dynamo-db.dax.connection-ttl` (Optional; type `String`; default `PT0S`): How long should connections be kept alive, calculated in milliseconds.
- `cas.authn.mfa.gauth.dynamo-db.dax.idle-timeout` (Optional; type `String`; default `PT15S`): Connection idle timeout, calculated in milliseconds.
- `cas.authn.mfa.gauth.dynamo-db.dax.max-concurrency` (Optional; type `Integer`; default `1000`): Determines the maximum number of concurrent requests that can be made to the DAX cluster.
- `cas.authn.mfa.gauth.dynamo-db.dax.read-retries` (Optional; type `Integer`; default `2`): Number of read retry attempts.
- `cas.authn.mfa.gauth.dynamo-db.dax.request-timeout` (Optional; type `String`; default `PT5S`): Request execution timeout, calculated in milliseconds.
- `cas.authn.mfa.gauth.dynamo-db.dax.write-retries` (Optional; type `Integer`; default `2`): Number of write retry attempts.
- `cas.authn.mfa.gauth.dynamo-db.drop-tables-on-startup` (Optional; type `Boolean`; default `false`): Flag that indicates whether to drop tables on start up.
- `cas.authn.mfa.gauth.dynamo-db.local-address` (Optional; type `String`): Local address.
- `cas.authn.mfa.gauth.dynamo-db.local-instance` (Optional; type `Boolean`; default `false`): Indicates that the database instance is local to the deployment that does not require or use any credentials or other configuration other than host and region. This is mostly used during development and testing.
- `cas.authn.mfa.gauth.dynamo-db.max-connections` (Optional; type `Integer`; default `10`): Maximum connections setting.
- `cas.authn.mfa.gauth.dynamo-db.prevent-table-creation-on-startup` (Optional; type `Boolean`; default `false`): Flag that indicates whether to prevent CAS from creating tables.
- `cas.authn.mfa.gauth.dynamo-db.profile-name` (Optional; type `String`): Profile name to use.
- `cas.authn.mfa.gauth.dynamo-db.profile-path` (Optional; type `String`): Profile path.
- `cas.authn.mfa.gauth.dynamo-db.proxy-host` (Optional; type `String`): Optionally specifies the proxy host to connect through.
- `cas.authn.mfa.gauth.dynamo-db.proxy-password` (Optional; type `String`): Optionally specifies the proxy password to connect through.
- `cas.authn.mfa.gauth.dynamo-db.proxy-username` (Optional; type `String`): Optionally specifies the proxy username to connect through.
- `cas.authn.mfa.gauth.dynamo-db.read-capacity` (Optional; type `Long`; default `10`): Read capacity.
- `cas.authn.mfa.gauth.dynamo-db.retry-mode` (Optional; type `String`; default `STANDARD`): Outline the requested retry mode. Accepted values are `STANDARD, LEGACY`.
- `cas.authn.mfa.gauth.dynamo-db.socket-timeout` (Optional; type `String`; default `PT5S`): Socket timeout.
- `cas.authn.mfa.gauth.dynamo-db.table-name` (Optional; type `String`; default `DynamoDbGoogleAuthenticatorRepository`): The table name used and created by CAS to hold accounts in DynamoDb.
- `cas.authn.mfa.gauth.dynamo-db.time-offset` (Optional; type `Integer`; default `0`): Time offset.
- `cas.authn.mfa.gauth.dynamo-db.token-table-name` (Optional; type `String`; default `DynamoDbGoogleAuthenticatorTokenRepository`): The table name used and created by CAS to hold tokens in DynamoDb.
- `cas.authn.mfa.gauth.dynamo-db.use-reaper` (Optional; type `Boolean`; default `false`): Flag that indicates whether to use reaper.
- `cas.authn.mfa.gauth.dynamo-db.write-capacity` (Optional; type `Long`; default `10`): Write capacity.
