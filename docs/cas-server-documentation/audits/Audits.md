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

Please [see this guide](Audits-File.html) for more info.

## Database Audits

Please [see this guide](Audits-Database.html) for more info.

## MongoDb Audits

Please [see this guide](Audits-MongoDb.html) for more info.

## Redis Audits

Please [see this guide](Audits-Redis.html) for more info.

## CouchDb Audits

Please [see this guide](Audits-CouchDb.html) for more info.

## Couchbase Audits

Please [see this guide](Audits-Couchbase.html) for more info.

## DynamoDb Audits

Please [see this guide](Audits-DynamoDb.html) for more info.

## REST Audits

Please [see this guide](Audits-REST.html) for more info.

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
