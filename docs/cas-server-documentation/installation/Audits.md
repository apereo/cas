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

{% include {{ version }}/audit-configuration.md %}

## Administrative Endpoints

The following endpoints are provided by CAS:
 
| Endpoint                 | Description
|--------------------------|------------------------------------------------
| `auditLog`               | Provides a JSON representation of all the audit log.

You can specify an interval of log entries to return by adding a `Duration` Syntax 
to the navigated path. This interval will be subtracted from the current 
date and time when the query is executed. For instance `/actuator/auditLog/PT1H` will 
return only entries for the past hour.

The actuator endpoint can also accept a JSON object through a POST method containing criteria to filter log entries by.

The following filters that can be applied:

| Key                       | Value
|---------------------------|-----------------------------------------------
| `interval`                | `PT1H`, `PT10M`, `P1D`
| `actionPerformed`         | `TICKET_GRANTING_TICKET_CREATED`, `SERVICE_TICK.*`
| `clientIpAddress`         | `111.111.111.111`, `111.111.*` 
| `username`                | `casuser`, `cas.*`
| `resourceOperatedOn`      | `ST-1.*`, `TGT-1-.*`

Each filter other than `interval` can accept a regular expression to match against.

## File-based Audits

File-based audit logs appear in a `cas_audit.log` file defined in the [Logging](../logging/Logging.html) configuration.

{% include {{ version }}/slf4j-audit-configuration.md %}

### Sample Log Output

```bash
WHO: org.apereo.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
WHAT: supplied credentials: org.apereo.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
ACTION: AUTHENTICATION_SUCCESS
APPLICATION: CAS
WHEN: Mon Aug 26 12:35:59 IST 2013
CLIENT IP ADDRESS: 172.16.5.181
SERVER IP ADDRESS: 192.168.200.22

WHO: org.apereo.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
WHAT: TGT-9-qj2jZKQUmu1gQvXNf7tXQOJPOtROvOuvYAxybhZiVrdZ6pCUwW-cas01.example.org
ACTION: TICKET_GRANTING_TICKET_CREATED
APPLICATION: CAS
WHEN: Mon Aug 26 12:35:59 IST 2013
CLIENT IP ADDRESS: 172.16.5.181
SERVER IP ADDRESS: 192.168.200.22
```

## Database Audits

If you intend to use a database for auditing functionality, enable the following module in your configuration:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-audit-jdbc" %}

To learn how to configure database drivers, please [review this guide](JDBC-Drivers.html).

{% include {{ version }}/rdbms-configuration.md configKey="cas.audit.jdbc" %}

{% include {{ version }}/jdbc-audit-configuration.md %}

{% include {{ version }}/job-scheduling-configuration.md configKey="cas.audit.jdbc" %}

## MongoDb Audits

If you intend to use a MongoDb database for auditing functionality, enable the following module in your configuration:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-audit-mongo" %}

{% include {{ version }}/mongodb-configuration.md configKey="cas.audit" %}

{% include {{ version }}/mongodb-audit-configuration.md %}

## Redis Audits

If you intend to use a Redis database for auditing functionality, enable the following module in your configuration:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-audit-redis" %}

{% include {{ version }}/redis-configuration.md configKey="cas.audit" %}

{% include {{ version }}/redis-audit-configuration.md %}

## CouchDb Audits

If you intend to use a CouchDb database for auditing functionality, enable the following module in your configuration:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-audit-couchdb" %}

{% include {{ version }}/couchdb-integration.md configKey="cas.audit" %}

## Couchbase Audits

If you intend to use a Couchbase database for auditing functionality, enable the following module in your configuration:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-audit-couchbase" %}

{% include {{ version }}/couchbase-configuration.md configKey="cas.audit.couchbase" %}

{% include {{ version }}/couchbase-audit-configuration.md %}

## DynamoDb Audits

If you intend to use a DynamoDb database for auditing functionality, enable the following module in your configuration:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-audit-dynamodb" %}

{% include {{ version }}/dynamodb-configuration.md configKey="cas.audit" %}

{% include {{ version }}/dynamodb-audit-configuration.md %}

## REST Audits

Audit events may also be `POST`ed to an endpoint of your choosing. To activate 
this feature, enable the following module in your configuration:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-audit-rest" %}

The body of the HTTP request is a JSON representation of the audit record. 

{% include {{ version }}/rest-integration.md configKey="cas.audit.rest" %}

{% include {{ version }}/rest-audit-configuration.md %}

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
| `EVALUATE_RISKY_AUTHENTICATION`       | N/A
| `MITIGATE_RISKY_AUTHENTICATION`       | N/A
| `MULTIFACTOR_AUTHENTICATION_BYPASS`   | N/A
| `SAVE_SERVICE`                        | `SUCCESS`, `FAILURE`
| `SAVE_CONSENT`                        | `SUCCESS`, `FAILURE`
| `CHANGE_PASSWORD`                     | `SUCCESS`, `FAILURE`
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
