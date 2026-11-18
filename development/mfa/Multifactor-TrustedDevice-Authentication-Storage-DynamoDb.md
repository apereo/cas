# DynamoDb Device Storage - Multifactor Authentication Trusted Device/Browser

Source: https://apereo.github.io/cas/development/mfa/Multifactor-TrustedDevice-Authentication-Storage-DynamoDb.html

User decisions may also be kept inside a DynamoDb instance.

Support is provided via the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-trusted-mfa-dynamodb"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.trusted.dynamo-db.credential-access-key` (Required; type `String`): Use access-key provided by AWS to authenticate.
- `cas.authn.mfa.trusted.dynamo-db.credential-secret-key` (Required; type `String`): Use secret key provided by AWS to authenticate.
- `cas.authn.mfa.trusted.dynamo-db.dax.url` (Required; type `String`): Cluster url. For example, `dax://my-cluster.l6fzcv.dax-clusters.us-east-1.amazonaws.com`.
- `cas.authn.mfa.trusted.dynamo-db.endpoint` (Required; type `String`): AWS custom endpoint.
- `cas.authn.mfa.trusted.dynamo-db.region` (Required; type `String`): AWS region used.
- `cas.authn.mfa.trusted.dynamo-db.billing-mode` (Optional; type `AbstractDynamoDbProperties.BillingMode`; default `PROVISIONED`): Billing mode specifies how you are charged for read and write throughput and how you manage capacity. Available values are as follows: - `PROVISIONED`: Provisioned mode means that you specify the number of reads and writes per second that you expect your application to use. Provisioned mode is a good option if any of the following are true: - You have predictable application traffic. - You run applications whose traffic is consistent or ramps gradually. - You can forecast capacity requirements to control costs. You can use auto scaling to automatically adjust capacity based on the specified utilization rate to ensure application performance while reducing costs. `PAY_PER_REQUEST`: Pay-per-request or on-demand billing means that you're charged for only the read/write requests that you use. On-demand mode is a good option if any of the following are true: You create new tables with unknown workloads. You have unpredictable application traffic. You prefer the ease of paying for only what you use. Tables using on-demand mode support all DynamoDB features (such as encryption at rest, point-in-time recovery, global tables, and so on) with the exception of auto scaling, which is not applicable with this mode.
- `cas.authn.mfa.trusted.dynamo-db.client-execution-timeout` (Optional; type `String`; default `PT10S`): Client execution timeout.
- `cas.authn.mfa.trusted.dynamo-db.connection-timeout` (Optional; type `String`; default `PT5S`): Connection timeout.
- `cas.authn.mfa.trusted.dynamo-db.dax.connect-timeout` (Optional; type `String`; default `PT5S`): Connection timeout, calculated in milliseconds.
- `cas.authn.mfa.trusted.dynamo-db.dax.connection-ttl` (Optional; type `String`; default `PT0S`): How long should connections be kept alive, calculated in milliseconds.
- `cas.authn.mfa.trusted.dynamo-db.dax.idle-timeout` (Optional; type `String`; default `PT15S`): Connection idle timeout, calculated in milliseconds.
- `cas.authn.mfa.trusted.dynamo-db.dax.max-concurrency` (Optional; type `Integer`; default `1000`): Determines the maximum number of concurrent requests that can be made to the DAX cluster.
- `cas.authn.mfa.trusted.dynamo-db.dax.read-retries` (Optional; type `Integer`; default `2`): Number of read retry attempts.
- `cas.authn.mfa.trusted.dynamo-db.dax.request-timeout` (Optional; type `String`; default `PT5S`): Request execution timeout, calculated in milliseconds.
- `cas.authn.mfa.trusted.dynamo-db.dax.write-retries` (Optional; type `Integer`; default `2`): Number of write retry attempts.
- `cas.authn.mfa.trusted.dynamo-db.drop-tables-on-startup` (Optional; type `Boolean`; default `false`): Flag that indicates whether to drop tables on start up.
- `cas.authn.mfa.trusted.dynamo-db.local-address` (Optional; type `String`): Local address.
- `cas.authn.mfa.trusted.dynamo-db.local-instance` (Optional; type `Boolean`; default `false`): Indicates that the database instance is local to the deployment that does not require or use any credentials or other configuration other than host and region. This is mostly used during development and testing.
- `cas.authn.mfa.trusted.dynamo-db.max-connections` (Optional; type `Integer`; default `10`): Maximum connections setting.
- `cas.authn.mfa.trusted.dynamo-db.prevent-table-creation-on-startup` (Optional; type `Boolean`; default `false`): Flag that indicates whether to prevent CAS from creating tables.
- `cas.authn.mfa.trusted.dynamo-db.profile-name` (Optional; type `String`): Profile name to use.
- `cas.authn.mfa.trusted.dynamo-db.profile-path` (Optional; type `String`): Profile path.
- `cas.authn.mfa.trusted.dynamo-db.proxy-host` (Optional; type `String`): Optionally specifies the proxy host to connect through.
- `cas.authn.mfa.trusted.dynamo-db.proxy-password` (Optional; type `String`): Optionally specifies the proxy password to connect through.
- `cas.authn.mfa.trusted.dynamo-db.proxy-username` (Optional; type `String`): Optionally specifies the proxy username to connect through.
- `cas.authn.mfa.trusted.dynamo-db.read-capacity` (Optional; type `Long`; default `10`): Read capacity.
- `cas.authn.mfa.trusted.dynamo-db.retry-mode` (Optional; type `String`; default `STANDARD`): Outline the requested retry mode. Accepted values are `STANDARD, LEGACY`.
- `cas.authn.mfa.trusted.dynamo-db.socket-timeout` (Optional; type `String`; default `PT5S`): Socket timeout.
- `cas.authn.mfa.trusted.dynamo-db.table-name` (Optional; type `String`; default `DynamoDbCasMfaTrustRecords`): The table name used and created by CAS to hold mfa trust definitions in DynamoDb.
- `cas.authn.mfa.trusted.dynamo-db.time-offset` (Optional; type `Integer`; default `0`): Time offset.
- `cas.authn.mfa.trusted.dynamo-db.use-reaper` (Optional; type `Boolean`; default `false`): Flag that indicates whether to use reaper.
- `cas.authn.mfa.trusted.dynamo-db.write-capacity` (Optional; type `Long`; default `10`): Write capacity.
