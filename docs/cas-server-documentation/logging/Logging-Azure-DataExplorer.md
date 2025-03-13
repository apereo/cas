---
layout: default
title: CAS - Azure Data Explorer Kusto Logging Configuration
category: Logs & Audits
---

{% include variables.html %}

# Azure Data Explorer - Kusto

This extension streams your log data to your table in Azure Data Explorer (also known as Kusto), where 
you can analyze and visualize your logs in real time.

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-azure-monitor" %}

<div class="alert alert-info mt-3">:information_source: <strong>Remember</strong><p>
If you are interesting in Azure Monitor as an application performance monitoring tool,
you may want to take a look at <a href="../monitoring/Configuring-Monitoring-AzureInsights.html">this guide</a>.
</p></div>

This integration uses a custom strategy that's used in the `RollingFileAppender`. Logs are written into 
the rolling file to prevent any data loss arising out of network failure while connecting to the 
Kusto cluster. The data is stored in a rolling file and then flushed to the Kusto cluster.

```xml
<RollingFile name="AzureData" fileName="cas-${date:yyyyMMdd}.log"
             filePattern="logs/$${date:yyyy-MM}/app-%d{MM-dd-yyyy}-%i.log.gz">
    <KustoStrategy
        clusterIngestUrl="${env:LOG4J2_ADX_INGEST_CLUSTER_URL}"
        appId="${env:LOG4J2_ADX_APP_ID}"
        appKey="${env:LOG4J2_ADX_APP_KEY}"
        appTenant="${env:LOG4J2_ADX_TENANT_ID}"
        dbName="${env:LOG4J2_ADX_DB_NAME}"
        tableName="log4jTest"
        logTableMapping="log4jCsvTestMapping"
        mappingType="csv"
        flushImmediately="true"
    />
    <CsvLogEventLayout delimiter="," quoteMode="ALL"/>
    ...
</RollingFile>
```
  
The following fields are supported:

| Property           | Description                                                                                                |
|--------------------|------------------------------------------------------------------------------------------------------------|
| `clusterIngestUrl` | The ingest URI for your cluster in the format `https://ingest-<cluster>.<region>.kusto.windows.net`.       |
| `dbName`           | The case-sensitive name of the target database.                                                            |
| `tableName`        | The case-sensitive name of an existing target table.                                                       |
| `appId`            | The application client ID required for authentication, stored from Microsoft Entra App registration.       |
| `appKey`           | The application key required for authentication, stored from Microsoft Entra App registration.             |
| `appTenant`        | The ID of the tenant in which the application is registered, stored from Microsoft Entra App registration. |
| `logTableMapping`  | The name of the mapping.                                                                                   |
| `mappingType`      | The type of mapping to use. The default is `csv`.                                                          |
| `flushImmediately` | If set to `true`, the sink flushes the buffer after each log event. The default is `false`.                |
