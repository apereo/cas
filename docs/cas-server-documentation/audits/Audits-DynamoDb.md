---
layout: default
title: CAS - DynamoDb Audits
category: Logs & Audits
---
{% include variables.html %}

# DynamoDb Audits

Store the CAS audit trail in an Amazon DynamoDB table, so audit records from every node end up in one durable place.

If you intend to use a DynamoDb database for auditing functionality, enable the following module in your configuration:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-audit-dynamodb" %}
         
{% include_cached casproperties.html properties="cas.audit.dynamo-db" %}

Next to the records table, CAS keeps a second table, named after the first with `Summary` appended, that counts
records per hour, per action and per target as they are written. Hourly summaries, such as the sign-in charts of the
Palantir dashboard, read these counts instead of scanning the records, and the records table holds only records.


