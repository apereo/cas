---
layout: default
title: CAS - Audit Configuration
category: Logs & Audits
---
{% include variables.html %}

# Database Audits

If you intend to use a database for auditing functionality, enable the following module in your configuration:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-audit-jdbc" %}

To learn how to configure database drivers, please [review this guide](../installation/JDBC-Drivers.html).

{% include_cached casproperties.html properties="cas.audit.jdbc" %}
         
## Database Schema

The table schema roughly should match the following structure:

```sql
CREATE TABLE COM_AUDIT_TRAIL
(
    AUD_USER      VARCHAR2(100)  NOT NULL,
    AUD_CLIENT_IP VARCHAR(15)    NOT NULL,
    AUD_SERVER_IP VARCHAR(15)    NOT NULL,
    AUD_RESOURCE  VARCHAR2(1024) NOT NULL,
    AUD_TENANT    VARCHAR(25)    NOT NULL,
    AUD_ACTION    VARCHAR2(100)  NOT NULL,
    APPLIC_CD     VARCHAR2(5)    NOT NULL,
    AUD_DATE      TIMESTAMP      NOT NULL,
    AUD_GEOLOCATION   VARCHAR2(100)   NOT NULL,
    AUD_USERAGENT     VARCHAR2(100)   NOT NULL,
    AUD_LOCALE        VARCHAR2(10)    NOT NULL,
    AUD_HEADERS       TEXT   NOT NULL,
    AUD_EXTRA_INFO    TEXT   NOT NULL
)
```
