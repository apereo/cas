# DynamoDb - Attribute Consent Storage

Source: https://apereo.github.io/cas/development/integration/Attribute-Release-Consent-Storage-DynamoDb.html

Keep the attribute consent decisions users make in an Amazon DynamoDB table.

Support is enabled by including the following module in the WAR Overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-consent-dynamodb"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.consent.dynamo-db.credential-access-key` (Required; type `String`): Use access-key provided by AWS to authenticate.
- `cas.consent.dynamo-db.credential-secret-key` (Required; type `String`): Use secret key provided by AWS to authenticate.
- `cas.consent.dynamo-db.dax.url` (Required; type `String`): Cluster url. For example, `dax://my-cluster.l6fzcv.dax-clusters.us-east-1.amazonaws.com`.
- `cas.consent.dynamo-db.endpoint` (Required; type `String`): AWS custom endpoint.
- `cas.consent.dynamo-db.region` (Required; type `String`): AWS region used.
- `cas.consent.dynamo-db.billing-mode` (Optional; type `AbstractDynamoDbProperties.BillingMode`; default `PROVISIONED`): Billing mode specifies how you are charged for read and write throughput and how you manage capacity. Available values are as follows: - `PROVISIONED`: Provisioned mode means that you specify the number of reads and writes per second that you expect your application to use. Provisioned mode is a good option if any of the following are true: - You have predictable application traffic. - You run applications whose traffic is consistent or ramps gradually. - You can forecast capacity requirements to control costs. You can use auto scaling to automatically adjust capacity based on the specified utilization rate to ensure application performance while reducing costs. `PAY_PER_REQUEST`: Pay-per-request or on-demand billing means that you're charged for only the read/write requests that you use. On-demand mode is a good option if any of the following are true: You create new tables with unknown workloads. You have unpredictable application traffic. You prefer the ease of paying for only what you use. Tables using on-demand mode support all DynamoDB features (such as encryption at rest, point-in-time recovery, global tables, and so on) with the exception of auto scaling, which is not applicable with this mode.
- `cas.consent.dynamo-db.client-execution-timeout` (Optional; type `String`; default `PT10S`): Client execution timeout.
- `cas.consent.dynamo-db.connection-timeout` (Optional; type `String`; default `PT5S`): Connection timeout.
- `cas.consent.dynamo-db.dax.connect-timeout` (Optional; type `String`; default `PT5S`): Connection timeout, calculated in milliseconds.
- `cas.consent.dynamo-db.dax.connection-ttl` (Optional; type `String`; default `PT0S`): How long should connections be kept alive, calculated in milliseconds.
- `cas.consent.dynamo-db.dax.idle-timeout` (Optional; type `String`; default `PT15S`): Connection idle timeout, calculated in milliseconds.
- `cas.consent.dynamo-db.dax.max-concurrency` (Optional; type `Integer`; default `1000`): Determines the maximum number of concurrent requests that can be made to the DAX cluster.
- `cas.consent.dynamo-db.dax.read-retries` (Optional; type `Integer`; default `2`): Number of read retry attempts.
- `cas.consent.dynamo-db.dax.request-timeout` (Optional; type `String`; default `PT5S`): Request execution timeout, calculated in milliseconds.
- `cas.consent.dynamo-db.dax.write-retries` (Optional; type `Integer`; default `2`): Number of write retry attempts.
- `cas.consent.dynamo-db.drop-tables-on-startup` (Optional; type `Boolean`; default `false`): Flag that indicates whether to drop tables on start up.
- `cas.consent.dynamo-db.local-address` (Optional; type `String`): Local address.
- `cas.consent.dynamo-db.local-instance` (Optional; type `Boolean`; default `false`): Indicates that the database instance is local to the deployment that does not require or use any credentials or other configuration other than host and region. This is mostly used during development and testing.
- `cas.consent.dynamo-db.max-connections` (Optional; type `Integer`; default `10`): Maximum connections setting.
- `cas.consent.dynamo-db.prevent-table-creation-on-startup` (Optional; type `Boolean`; default `false`): Flag that indicates whether to prevent CAS from creating tables.
- `cas.consent.dynamo-db.profile-name` (Optional; type `String`): Profile name to use.
- `cas.consent.dynamo-db.profile-path` (Optional; type `String`): Profile path.
- `cas.consent.dynamo-db.proxy-host` (Optional; type `String`): Optionally specifies the proxy host to connect through.
- `cas.consent.dynamo-db.proxy-password` (Optional; type `String`): Optionally specifies the proxy password to connect through.
- `cas.consent.dynamo-db.proxy-username` (Optional; type `String`): Optionally specifies the proxy username to connect through.
- `cas.consent.dynamo-db.read-capacity` (Optional; type `Long`; default `10`): Read capacity.
- `cas.consent.dynamo-db.retry-mode` (Optional; type `String`; default `STANDARD`): Outline the requested retry mode. Accepted values are `STANDARD, LEGACY`.
- `cas.consent.dynamo-db.socket-timeout` (Optional; type `String`; default `PT5S`): Socket timeout.
- `cas.consent.dynamo-db.table-name` (Optional; type `String`; default `DynamoDbConsentRecords`): The table name used and created by CAS to hold consent records in DynamoDb.
- `cas.consent.dynamo-db.time-offset` (Optional; type `Integer`; default `0`): Time offset.
- `cas.consent.dynamo-db.use-reaper` (Optional; type `Boolean`; default `false`): Flag that indicates whether to use reaper.
- `cas.consent.dynamo-db.write-capacity` (Optional; type `Long`; default `10`): Write capacity.
