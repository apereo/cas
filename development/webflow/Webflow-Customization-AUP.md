# Acceptable Usage Policy

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP.html

Also known as *Terms of Use* or *EULA*, CAS presents the ability to allow the user to accept the usage policy before moving on to the application. Production-level deployments of this feature would require modifications to the flow such that the retrieval and/or acceptance of the policy would be handled via an external storage mechanism such as LDAP or JDBC.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-aup-webflow"
}
```

Customize the policy by modifying the `casAcceptableUsagePolicyView.html`. See [this guide](https://apereo.github.io/cas/development/ux/User-Interface-Customization.html) to learn more about user interface customizations. Note that the view here should have full access to the resolved principal and attributes, if you wish to dynamically alter the page to present different text, etc.

> **Webflow Sequence**
>
> Remember that acceptable usage policy executes after a successful authentication event where CAS has already established the authentication principal, since the policy record is strongly tied to the identified user record. Implementing this feature before the authentication event would require rather heavy modifications to the CAS webflow as well as alternative means of storing and remembering decisions such as cookies or browser storage, etc.

The following settings and properties are available from the CAS configuration catalog:

- `cas.acceptable-usage-policy.core.aup-attribute-name` (Required; type `String`; default `aupAccepted`): AUP attribute to choose in order to determine whether policy has been accepted or not. The attribute is expected to contain a boolean value where `true` indicates policy has been accepted and `false` indicates otherwise. The attribute is fetched for the principal from configured sources and compared for the right match to determine policy status. If the attribute is not found, the policy status is considered as denied.
- `cas.acceptable-usage-policy.core.enabled` (Required; type `Boolean`; default `true`): Allows AUP to be turned off on startup.
- `cas.acceptable-usage-policy.core.aup-omit-if-attribute-missing` (Optional; type `Boolean`; default `false`): By default, the policy status is considered as denied when the attribute `#aupAttributeName` is not found. If set to `true`, the policy status in those cases is considered as accepted.
- `cas.acceptable-usage-policy.core.aup-policy-terms-attribute-name` (Optional; type `String`): AUP attribute to choose whose single value dictates how CAS should fetch the policy terms from the relevant message bundles.

## Per Service

Acceptable usage policy can be disabled and skipped on a per-service basis:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "https://app.example.org",
  "name": "Example",
  "id": 1,
  "acceptableUsagePolicy": {
    "@class": "org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy",
    "enabled": true,
    "messageCode": "example.code",
    "text": "example text"
  }
}
```

The policy assigned to each service includes the following features:

| Field | Description |
| --- | --- |
| `enabled` | Control whether policy is active/inactive for this service. Default is `true`. |
| `messageCode` | The policy language code that is linked to the CAS language bundles which carries the actual policy text. |
| `text` | The policy text that should be displayed for this application. |

## Storage Mechanism

Usage policy user decisions are stored and remembered via the following ways.

In almost all storage strategies, CAS allows the deployer to detect the current user’s policy choice via a CAS single-valued `boolean` attribute. The attribute must be resolved using the [CAS attribute resolution strategy](https://apereo.github.io/cas/development/integration/Attribute-Resolution.html). If the attribute contains a value of `false`, CAS will attempt to ask for policy acceptance. Upon accepting the policy, the result will be stored back into storage.

| Storage | Description |
| --- | --- |
| Default | [See this guide](https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP-Default.html). |
| Groovy | [See this guide](https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP-Groovy.html). |
| LDAP | [See this guide](https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP-LDAP.html). |
| MongoDb | [See this guide](https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP-MongoDb.html). |
| Redis | [See this guide](https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP-Redis.html). |
| JDBC | [See this guide](https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP-JDBC.html). |
| REST | [See this guide](https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP-REST.html). |
| Custom | [See this guide](https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP-Custom.html). |

### Multitenancy

Configuration settings for a number of storage mechanisms can be specified in a multitenant environment. Please [review this guide](https://apereo.github.io/cas/development/multitenancy/Multitenancy-Tenant-Properties.html) for more information and then cross check with the specific storage mechanism guides listed above.

## Policy Terms

Storage options outlined above are also available to fetch the acceptable usage policy and pass it along to the appropriate views for display and acceptance under the attribute `aupPolicy`. The policy terms can reference to a particular message code found in CAS language bundles, or it can contain the default policy text that would be used for display verbatim.

Unless the storage option overrides and specializes this ability, th default behavior to fetch policy terms is based on a single-valued attribute defined in CAS properties that typically might indicate user status or membership. The attribute value is appended to the language code `screen.aup.policyterms` to then allow CAS to look up the specific policy text from language bundles. If no such key is available in CAS languages bundles, a default policy text found under the same language key will be displayed.

The defined attribute must of course be available for the resolved authenticated principal from the relevant sources.

For example, if the policy terms attribute is defined as `status` with the value of `developer`, the expected language code to carry the policy text would be `screen.aup.policyterms.developer=<p>Policy for developers</p>`.
