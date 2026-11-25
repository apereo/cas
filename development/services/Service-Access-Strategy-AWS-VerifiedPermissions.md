# Service Access Strategy - AWS Verified Permissions

Source: https://apereo.github.io/cas/development/services/Service-Access-Strategy-AWS-VerifiedPermissions.html

[Amazon Verified Permissions](https://aws.amazon.com/verified-permissions/) is a scalable permissions management and fine-grained authorization service for the applications that you build.

Support is enabled by including the following module in the overlay:

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

This access strategy builds an authorization request and submits it to Amazon Verified Permissions. The specifics of the authorization request are taught to CAS using the settings typically defined within the access strategy itself:

```json
{
  "@class": "org.apereo.cas.aws.authz.AmazonVerifiedPermissionsRegisteredServiceAccessStrategy",
  "credentialAccessKey": "...",
  "credentialSecretKey": "...",
  "region": "us-east-1",
  "policyStoreId": "...",
  "actionId": "read",
  "context": {
    "@class": "java.util.LinkedHashMap",
    "key": "value"
  }
}
```

The following fields are available to this access strategy:

| Field | Purpose |
| --- | --- |
| `credentialAccessKey` | [1] (Optional) The access key used to authenticate the request. |
| `credentialSecretKey` | [1] (Optional) The secret key used to authenticate the request. |
| `region` | [1] (Optional) AWS region to use for authorization API requests. |
| `policyStoreId` | [1] (Optional) Policies in this policy store to use authorizations |
| `context` | (Optional) Additional context used for granular authorization decisions. |
| `actionId` | [1] Specifies the requested action to be authorized. |

*[1] This field supports the [Spring Expression Language](https://apereo.github.io/cas/development/configuration/Configuration-Spring-Expressions.html) syntax.*
