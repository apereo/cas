---
layout: default
title: CAS - REST Audits
category: Logs & Audits
---
{% include variables.html %}

# REST Audits

Audit events may also be `POST`ed to an endpoint of your choosing. To activate 
this feature, enable the following module in your configuration:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-audit-rest" %}

The body of the HTTP request is a JSON representation of the audit record. 

## Summaries

To count sign-ins and other audit records per hour, for example on the Palantir dashboard, CAS sends a `GET` request
to the same endpoint with these parameters:

| Parameter | Description                                                                                       |
|-----------|---------------------------------------------------------------------------------------------------|
| `summary` | Always `hourly`.                                                                                  |
| `from`    | The first hour to count, as an ISO date and time without a time zone, such as `2026-11-15T09:00`. |
| `to`      | The end of the period, not included, in the same format.                                          |
| `actions` | The audit actions to count, separated by commas.                                                  |

The endpoint answers with a JSON list of counts, leaving out hours and actions without records:

```json
[
  { "hour": "2026-11-15T09:00", "action": "AUTHENTICATION_SUCCESS", "count": 42 },
  { "hour": "2026-11-15T09:00", "action": "AUTHENTICATION_FAILED", "count": 3 }
]
```

Any other answer, such as an error or a list of audit records, means the endpoint does not count records, and CAS
reports that summaries are not available.

{% include_cached casproperties.html properties="cas.audit.rest" %}
