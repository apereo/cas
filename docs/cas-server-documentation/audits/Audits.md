---
layout: default
title: CAS - Audit Configuration
category: Logs & Audits
---
{% include variables.html %}

# Audits

CAS uses the [Inspektr framework](https://github.com/apereo/inspektr) for auditing purposes
and statistics. The Inspektr project allows for non-intrusive auditing and logging of the
coarse-grained execution paths e.g. Spring-managed beans method executions by using annotations
and Spring-managed `@Aspect`-style aspects.

CAS server auto-configures all the relevant Inspektr components. All the available configuration
options that are injected to Inspektr classes are available to deployers via relevant CAS properties. 
Note that the audit record management functionality of CAS supports handling multiple audit 
record destinations at the same time. In other words, you may choose to route audit records 
to both a database and a REST endpoint as well as any number of logger-based destinations all at the same time.

{% include casproperties.html properties="cas.audit.engine." %}

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include actuators.html endpoints="auditLog,auditevents" casModule="cas-server-support-reports" %}
     
## Storage

Audits can be managed via the following strategies.

| Storage          | Description                                         
|-----------------------------------------------------------
| File System      | [See this guide](Audits-File.html).
| JPA              | [See this guide](Audits-Database.html).
| MongoDb          | [See this guide](Audits-MongoDb.html).
| Redis            | [See this guide](Audits-Redis.html).
| CouchDb          | [See this guide](Audits-CouchDb.html).
| Couchbase        | [See this guide](Audits-Couchbase.html).
| DynamoDb         | [See this guide](Audits-DynamoDb.html).
| REST             | [See this guide](Audits-REST.html).

## Audit Events

The following events are tracked and recorded in the audit log:

| Event                                 | Action
|---------------------------------------|--------------------------------------
| `TICKET_GRANTING_TICKET`              | `CREATED`, `NOT_CREATED`, `DESTROYED`
| `PROXY_GRANTING_TICKET`               | `CREATED`, `NOT_CREATED`, `DESTROYED`
| `SERVICE_TICKET`                      | `CREATED`, `NOT_CREATED`
| `PROXY_TICKET`                        | `CREATED`, `NOT_CREATED`
| `AUTHENTICATION`                      | `SUCCESS`, `FAILED`
| `AUTHENTICATION_EVENT`                | `TRIGGERED`
| `AUP_VERIFY`                          | `TRIGGERED`
| `AUP_SUBMIT`                          | `TRIGGERED`
| `SAVE_SERVICE`                        | `SUCCESS`, `FAILURE`
| `SAVE_CONSENT`                        | `SUCCESS`, `FAILURE`
| `CHANGE_PASSWORD`                     | `SUCCESS`, `FAILURE`
| `PROTOCOL_SPECIFICATION_VALIDATE`     | `SUCCESS`, `FAILURE`
| `DELETE_SERVICE`                      | `SUCCESS`, `FAILURE`
| `SAML2_RESPONSE`                      | `CREATED`, `FAILED`
| `SAML2_REQUEST`                       | `CREATED`, `FAILED`
| `OAUTH2_USER_PROFILE`                 | `CREATED`, `FAILED`
| `OAUTH2_ACCESS_TOKEN_REQUEST`         | `CREATED`, `FAILED`
| `OAUTH2_ACCESS_TOKEN_RESPONSE`        | `CREATED`, `FAILED`
| `OAUTH2_CODE_RESPONSE`                | `CREATED`, `FAILED`
| `REST_API_TICKET_GRANTING_TICKET`     | `CREATED`, `FAILED`
| `REST_API_SERVICE_TICKET`             | `CREATED`, `FAILED`
| `SERVICE_ACCESS_ENFORCEMENT`          | `TRIGGERED`
| `DELEGATED_CLIENT`                    | `SUCCESS`, `FAILURE`
| `SURROGATE_AUTHENTICATION_ELIGIBILITY_VERIFICATION`          | `TRIGGERED`
| `SURROGATE_AUTHENTICATION_ELIGIBILITY_SELECTION`             | `TRIGGERED`
| `EVALUATE_RISKY_AUTHENTICATION`       | N/A
| `MITIGATE_RISKY_AUTHENTICATION`       | N/A
| `MULTIFACTOR_AUTHENTICATION_BYPASS`   | N/A
| `REQUEST_CHANGE_PASSWORD`             | N/A
