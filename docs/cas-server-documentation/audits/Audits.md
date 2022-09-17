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

{% include_cached casproperties.html properties="cas.audit.engine." %}

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="auditLog,auditevents" casModule="cas-server-support-reports" %}

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

<table class="cas-datatable paginated-table" id="table-theme-properties">
    <thead>
        <tr>
          <th>Name</th>
        </tr>
    </thead>
    <tbody>
        {% for cfg in site.data[siteDataVersion]["audits"] %}
            {% assign configBlock = cfg[1] %}
            {% for config in configBlock %}
            <tr>
                <td>
                    <code>{{ config.name }}</code>
                </td>
            </tr>
            {% endfor %}
        {% endfor %}
    </tbody>
</table>
