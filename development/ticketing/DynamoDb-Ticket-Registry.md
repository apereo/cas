# DynamoDb Ticket Registry

Source: https://apereo.github.io/cas/development/ticketing/DynamoDb-Ticket-Registry.html

DynamoDb ticket registry integration is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-dynamodb-ticket-registry"
}
```

This registry stores tickets in [DynamoDb](https://aws.amazon.com/dynamodb/) instances. Each ticket type is linked to a distinct table.

## Configuration

You will need to provide CAS with your [AWS credentials](https://aws.amazon.com/console/). Also, to gain a better understanding of DynamoDb’s core components and concepts, please [start with this guide](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Introduction.html) first.

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.dynamo-db.credential-access-key` (Required; type `String`): Use access-key provided by AWS to authenticate.
- `cas.ticket.registry.dynamo-db.credential-secret-key` (Required; type `String`): Use secret key provided by AWS to authenticate.
- `cas.ticket.registry.dynamo-db.crypto.encryption.key` (Required; type `String`): The encryption key. The encryption key by default and unless specified otherwise must be randomly-generated string whose length is defined by the encryption key size setting.
- `cas.ticket.registry.dynamo-db.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.ticket.registry.dynamo-db.dax.url` (Required; type `String`): Cluster url. For example, `dax://my-cluster.l6fzcv.dax-clusters.us-east-1.amazonaws.com`.
- `cas.ticket.registry.dynamo-db.endpoint` (Required; type `String`): AWS custom endpoint.
- `cas.ticket.registry.dynamo-db.region` (Required; type `String`): AWS region used.
- `cas.ticket.registry.dynamo-db.billing-mode` (Optional; type `AbstractDynamoDbProperties.BillingMode`; default `PROVISIONED`): Billing mode specifies how you are charged for read and write throughput and how you manage capacity. Available values are as follows: - `PROVISIONED`: Provisioned mode means that you specify the number of reads and writes per second that you expect your application to use. Provisioned mode is a good option if any of the following are true: - You have predictable application traffic. - You run applications whose traffic is consistent or ramps gradually. - You can forecast capacity requirements to control costs. You can use auto scaling to automatically adjust capacity based on the specified utilization rate to ensure application performance while reducing costs. `PAY_PER_REQUEST`: Pay-per-request or on-demand billing means that you're charged for only the read/write requests that you use. On-demand mode is a good option if any of the following are true: You create new tables with unknown workloads. You have unpredictable application traffic. You prefer the ease of paying for only what you use. Tables using on-demand mode support all DynamoDB features (such as encryption at rest, point-in-time recovery, global tables, and so on) with the exception of auto scaling, which is not applicable with this mode.
- `cas.ticket.registry.dynamo-db.client-execution-timeout` (Optional; type `String`; default `PT10S`): Client execution timeout.
- `cas.ticket.registry.dynamo-db.connection-timeout` (Optional; type `String`; default `PT5S`): Connection timeout.
- `cas.ticket.registry.dynamo-db.crypto.alg` (Optional; type `String`; default `AES`): The signing/encryption algorithm to use.
- `cas.ticket.registry.dynamo-db.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.ticket.registry.dynamo-db.crypto.encryption.key-size` (Optional; type `Integer`; default `16`): Encryption key size.
- `cas.ticket.registry.dynamo-db.crypto.signing-enabled` (Optional; type `Boolean`; default `true`): Whether signing encryption operations are enabled.
- `cas.ticket.registry.dynamo-db.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.ticket.registry.dynamo-db.dax.connect-timeout` (Optional; type `String`; default `PT5S`): Connection timeout, calculated in milliseconds.
- `cas.ticket.registry.dynamo-db.dax.connection-ttl` (Optional; type `String`; default `PT0S`): How long should connections be kept alive, calculated in milliseconds.
- `cas.ticket.registry.dynamo-db.dax.idle-timeout` (Optional; type `String`; default `PT15S`): Connection idle timeout, calculated in milliseconds.
- `cas.ticket.registry.dynamo-db.dax.max-concurrency` (Optional; type `Integer`; default `1000`): Determines the maximum number of concurrent requests that can be made to the DAX cluster.
- `cas.ticket.registry.dynamo-db.dax.read-retries` (Optional; type `Integer`; default `2`): Number of read retry attempts.
- `cas.ticket.registry.dynamo-db.dax.request-timeout` (Optional; type `String`; default `PT5S`): Request execution timeout, calculated in milliseconds.
- `cas.ticket.registry.dynamo-db.dax.write-retries` (Optional; type `Integer`; default `2`): Number of write retry attempts.
- `cas.ticket.registry.dynamo-db.drop-tables-on-startup` (Optional; type `Boolean`; default `false`): Flag that indicates whether to drop tables on start up.
- `cas.ticket.registry.dynamo-db.local-address` (Optional; type `String`): Local address.
- `cas.ticket.registry.dynamo-db.local-instance` (Optional; type `Boolean`; default `false`): Indicates that the database instance is local to the deployment that does not require or use any credentials or other configuration other than host and region. This is mostly used during development and testing.
- `cas.ticket.registry.dynamo-db.max-connections` (Optional; type `Integer`; default `10`): Maximum connections setting.
- `cas.ticket.registry.dynamo-db.prevent-table-creation-on-startup` (Optional; type `Boolean`; default `false`): Flag that indicates whether to prevent CAS from creating tables.
- `cas.ticket.registry.dynamo-db.profile-name` (Optional; type `String`): Profile name to use.
- `cas.ticket.registry.dynamo-db.profile-path` (Optional; type `String`): Profile path.
- `cas.ticket.registry.dynamo-db.proxy-granting-tickets-table-name` (Optional; type `String`; default `proxyGrantingTicketsTable`): The table name used and created by CAS to hold proxy ticket granting tickets in DynamoDb.
- `cas.ticket.registry.dynamo-db.proxy-host` (Optional; type `String`): Optionally specifies the proxy host to connect through.
- `cas.ticket.registry.dynamo-db.proxy-password` (Optional; type `String`): Optionally specifies the proxy password to connect through.
- `cas.ticket.registry.dynamo-db.proxy-tickets-table-name` (Optional; type `String`; default `proxyTicketsTable`): The table name used and created by CAS to hold proxy tickets in DynamoDb.
- `cas.ticket.registry.dynamo-db.proxy-username` (Optional; type `String`): Optionally specifies the proxy username to connect through.
- `cas.ticket.registry.dynamo-db.read-capacity` (Optional; type `Long`; default `10`): Read capacity.
- `cas.ticket.registry.dynamo-db.retry-mode` (Optional; type `String`; default `STANDARD`): Outline the requested retry mode. Accepted values are `STANDARD, LEGACY`.
- `cas.ticket.registry.dynamo-db.service-tickets-table-name` (Optional; type `String`; default `serviceTicketsTable`): The table name used and created by CAS to hold service tickets in DynamoDb.
- `cas.ticket.registry.dynamo-db.socket-timeout` (Optional; type `String`; default `PT5S`): Socket timeout.
- `cas.ticket.registry.dynamo-db.ticket-granting-tickets-table-name` (Optional; type `String`; default `ticketGrantingTicketsTable`): The table name used and created by CAS to hold ticket granting tickets in DynamoDb.
- `cas.ticket.registry.dynamo-db.time-offset` (Optional; type `Integer`; default `0`): Time offset.
- `cas.ticket.registry.dynamo-db.transient-session-tickets-table-name` (Optional; type `String`; default `transientSessionTicketsTable`): The table name used and created by CAS to hold transient session tickets in DynamoDb.
- `cas.ticket.registry.dynamo-db.use-reaper` (Optional; type `Boolean`; default `false`): Flag that indicates whether to use reaper.
- `cas.ticket.registry.dynamo-db.write-capacity` (Optional; type `Long`; default `10`): Write capacity.

**Signing & encryption**

This CAS feature is able to accept signing and encryption crypto keys. In most scenarios if keys are not provided, CAS will auto-generate them. The following instructions apply if you wish to manually and beforehand create the signing and encryption keys.

Note that if you are asked to create a [JWK](https://tools.ietf.org/html/rfc7517) of a certain size for the key, you are to use the following set of commands to generate the token:

```bash
wget https://raw.githubusercontent.com/apereo/cas/master/etc/jwk-gen.jar
java -jar jwk-gen.jar -t oct -s [size]
```

The outcome would be similar to:

```json
{
  "kty": "oct",
  "kid": "...",
  "k": "..."
}
```

The generated value for `k` needs to be assigned to the relevant CAS settings. Note that keys generated via the above algorithm are processed by CAS using the Advanced Encryption Standard (`AES`) algorithm which is a specification for the encryption of electronic data established by the U.S. National Institute of Standards and Technology.

---

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="com.amazonaws" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
