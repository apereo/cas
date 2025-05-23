---
layout: default
title: CAS - Service Management
category: Services
---

{% include variables.html %}

# Service Management

The CAS service management facility allows CAS server administrators to declare and configure which services
(CAS clients) may make use of CAS in which ways. The core component of the service management facility is the
service registry that stores one or more registered services containing metadata that drives a number of CAS behaviors.

{% include_cached casproperties.html properties="cas.service-registry.core" %}

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="registeredServices,serviceAccess" casModule="cas-server-support-reports" %}

## Registered Services

Registered services present the following metadata:

| Field                       | Description                                                                                                                                                                                                                                                                                                                                                                                                                 |
|-----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `id`                        | Required unique identifier. This **MUST** be a valid numeric value.                                                                                                                                                                                                                                                                                                                                                         |
| `name`                      | Required name (`255` characters or less).                                                                                                                                                                                                                                                                                                                                                                                   |
| `description`               | Optional free-text description of the service. (`255` characters or less)                                                                                                                                                                                                                                                                                                                                                   |
| `informationUrl`            | Optional free-text link to the service information guide.                                                                                                                                                                                                                                                                                                                                                                   |
| `privacyUrl`                | Optional free-text link to the service privacy policy.                                                                                                                                                                                                                                                                                                                                                                      |
| `redirectUrl`               | Optional URL to use when returning an authentication response back to applications.                                                                                                                                                                                                                                                                                                                                         |
| `logo`                      | Optional path to an image file that is the logo for this service. The image will be displayed on the login page along with the service description and name. The value may be a relative path to the `images` directory of the CAS web application or it may be a full URL.                                                                                                                                                 |
| `serviceId`                 | Required [regular expression](http://docs.oracle.com/javase/tutorial/essential/regex/) describing a logical service. A logical service defines one or more URLs where a service or services are located. The definition of the url pattern must be **done carefully** because it can open security breaches.                                                                                                                |
| `locale`                    | Optional locale name that may be used to customize the CAS UI when the service requests a ticket. Values can use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax. See [this guide](../ux/User-Interface-Customization-Localization.html) for more details.                                                                                                                  |
| `theme`                     | Optional theme name that may be used to customize the CAS UI when the service requests a ticket. Values can use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax. See [this guide](../ux/User-Interface-Customization.html) for more details.                                                                                                                                |
| `proxyPolicy`               | Determines whether the service is able to proxy authentication. See [this guide](Configuring-Service-Proxy-Policy.html) for more info.                                                                                                                                                                                                                                                                                      |
| `evaluationOrder`           | Determines relative order of evaluation of registered services. This flag is particularly important in cases where two service URL expressions cover the same services; evaluation order determines which registration is evaluated first and acts as an internal sorting factor.                                                                                                                                           |
| `authenticationPolicy`      | The authentication policy to act as a complement or override for the global authentication engine. See [this guide](Configuring-Service-AuthN-Policy.html) for more details.                                                                                                                                                                                                                                                |
| `attributeReleasePolicy`    | The policy that describes the set of attributes allowed to be released to the application, as well as any other filtering logic needed to weed some out. See [this guide](../integration/Attribute-Release.html) for more details on attribute release and filters.                                                                                                                                                         |
| `logoutType`                | Defines how this service should be treated once the logout protocol is initiated. Acceptable values are `LogoutType.BACK_CHANNEL`, `LogoutType.FRONT_CHANNEL` or `LogoutType.NONE`. See [this guide](../installation/Logout-Single-Signout.html) for more details on logout.                                                                                                                                                |
| `responseType`              | Defines how CAS should respond to requests for this service. See [this guide](Configuring-Service-Response-Type.html) for more details.                                                                                                                                                                                                                                                                                     |
| `usernameAttributeProvider` | The provider configuration which dictates what value as the "username" should be sent back to the application. See [this guide](../integration/Attribute-Release.html) for more details on attribute release and filters.                                                                                                                                                                                                   |
| `accessStrategy`            | The [strategy configuration](Configuring-Service-Access-Strategy.html) that outlines and access rules for this service. It describes whether the service is allowed, authorized to participate in SSO, or can be granted access from the CAS perspective based on a particular attribute-defined role, aka RBAC. See [this guide](../integration/Attribute-Release.html) for more details on attribute release and filters. |
| `publicKey`                 | The public key associated with this service that is used to authorize the request by encrypting certain elements and attributes in the CAS validation protocol response, such as [the PGT](../authentication/Configuring-Proxy-Authentication.html) or [the credential](../integration/ClearPass.html). See [this guide](../integration/Attribute-Release.html) for more details on attribute release and filters.          |
| `logoutUrl`                 | URL endpoint for this service to receive logout requests. See [this guide](../installation/Logout-Single-Signout.html) for more details                                                                                                                                                                                                                                                                                     |
| `properties`                | Extra metadata associated with this service in form of key/value pairs. This is used to inject custom fields into the service definition, to be used later by extension modules to define additional behavior on a per-service basis. [See this guide](Configuring-Service-Custom-Properties.html) for more info please.                                                                                                    |
| `multifactorPolicy`         | The policy that describes the configuration required for this service authentication, typically for [multifactor authentication](../mfa/Configuring-Multifactor-Authentication.html).                                                                                                                                                                                                                                       |
| `contacts`                  | Specify the collection of contacts associated with service that own the application. See [this guide](Configuring-Service-Contacts.html) for more info.                                                                                                                                                                                                                                                                     |
| `matchingStrategy`          | Specify the strategy used to match the service definition against an authentication request. See [this guide](Configuring-Service-Matching-Strategy.html) for more info.                                                                                                                                                                                                                                                    |
| `supportedProtocols`        | Specify supported and allowed protocols for this service. See [this guide](Configuring-Service-Supported-Protocols.html) for more info.                                                                                                                                                                                                                                                                                     |
| `templateName`              | Name of the [template service definition](Configuring-Service-Template-Definitions.html) to use as a blueprint, when constructing this service definition.                                                                                                                                                                                                                                                                  |

<div class="alert alert-info">:information_source: <strong>Service Types</strong><p>Note that while the above properties apply to all <strong>generic</strong> service definitions, 
there are additional service types in CAS that may be activated and required depending on the protocol used and the nature of the client application. Always 
check the dedicated guide for the capability you have in mind (i.e. OAuth, SAML, etc).</p></div>

## Storage

The following options may be used to store services in CAS.

| Storage       | Description                                          | Usage                                                                                                    |
|---------------|------------------------------------------------------|----------------------------------------------------------------------------------------------------------|
| Memory        | [See this guide](InMemory-Service-Management.html).  | Store service definitions XML stored in memory. Changes require CAS repackaging and server restarts      |
| JSON          | [See this guide](JSON-Service-Management.html).      | Store service definitions in flat JSON files. HA deployments require replication of service definitions. |
| YAML          | [See this guide](YAML-Service-Management.html).      | Same as `JSON`.                                                                                          |
| GIT           | [See this guide](Git-Service-Management.html).       | Store service definitions in Git repository. Candidate for HA deployments.                               |
| MongoDb       | [See this guide](MongoDb-Service-Management.html).   | Store service definitions in MongoDb. Candidate for HA deployments.                                      |
| Redis         | [See this guide](Redis-Service-Management.html).     | Store service definitions in Redis. Candidate for HA deployments.                                        |
| LDAP          | [See this guide](LDAP-Service-Management.html).      | Store service definitions in a directory server. Candidate for HA deployments.                           |
| JPA           | [See this guide](JPA-Service-Management.html).       | Store service definitions in a relational database (Oracle, MySQL, etc). Candidate for HA deployments.   |
| DynamoDb      | [See this guide](DynamoDb-Service-Management.html).  | Store service definitions in DynamoDb. Candidate for HA deployments.                                     |
| Amazon S3     | [See this guide](AmazonS3-Service-Management.html).  | Store service definitions in Amazon S3 buckets. Candidate for HA deployments.                            |
| CosmosDb      | [See this guide](CosmosDb-Service-Management.html).  | Store service definitions in an Azure CosmosDb. Candidate for HA deployments.                            |
| Cassandra     | [See this guide](Cassandra-Service-Management.html). | Store service definitions in an Apache Cassandra. Candidate for HA deployments.                          |
| REST          | [See this guide](REST-Service-Management.html).      | Design your own service registry implementation as a REST API. Candidate for HA deployments.             |
| Custom        | [See this guide](Custom-Service-Management.html).    | Design your own service registry using CAS APIs as an extension. Candidate for HA deployments.           |

### How Do I Choose?

There are is a wide range of service registries on the menu. The selection criteria is outlined below:

- Choose a technology that you are most familiar with and have the skills and patience to troubleshoot, tune and scale for the win. 
- Choose a technology that does not force your CAS configuration to be tied to any individual servers/nodes in the cluster, as this will present auto-scaling issues and manual effort.
- Choose a technology that works well with your network and firewall configuration and is performant and reliable enough based on your network topology.
- Choose a technology that shows promising results under *your expected load*, having run [performance and stress tests](../high_availability/High-Availability-Performance-Testing.html).
- Choose a technology that does not depend on outside processes, systems and manual work as much as possible, is self-reliant and self contained.
