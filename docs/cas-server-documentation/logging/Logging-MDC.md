---
layout: default
title: CAS - MDC Logging Configuration
category: Logs & Audits
---

{% include variables.html %}

# Mapped Diagnostic Context

To uniquely stamp each request, CAS puts contextual
information into the `MDC`, the abbreviation of Mapped Diagnostic Context. This effectively
translates to a number of special variables available to the logging context that
may convey additional information about the nature of the request or the authentication event.

| Variable        | Description                              |
|-----------------|------------------------------------------|
| `remoteAddress` | Remote address of the HTTP request.      |
| `remoteUser`    | Remote user of the HTTP request.         |
| `serverName`    | Server name of the HTTP request.         |
| `serverPort`    | Server port of the HTTP request.         |
| `locale`        | Locale of the HTTP request.              |
| `contentType`   | Content type of the HTTP request.        |
| `contextPath`   | Context path of the HTTP request.        |
| `localAddress`  | Local address of the HTTP request.       |
| `localPort`     | Local port of the HTTP request.          |
| `remotePort`    | Remote port of the HTTP request.         |
| `pathInfo`      | Path information of the HTTP request.    |
| `protocol`      | Protocol of the HTTP request.            |
| `authType`      | Authentication type of the HTTP request. |
| `method`        | Method of the HTTP request.              |
| `queryString`   | Query string of the HTTP request.        |
| `requestUri`    | Request URI of the HTTP request.         |
| `scheme`        | Scheme of the HTTP request.              |
| `timezone`      | Timezone of the HTTP request.            |
| `principal`     | CAS authenticated principal id.          |
| `requestId`     | Generated identifier for this request.   |

Additionally, all available request attributes, headers, and parameters are exposed as variables.
<div class="alert alert-warning">
  <strong>Pay Attention</strong><br /> These variables potentially include the password. If you set <code>includeMDC=true</code> in <a href="Logging-SysLog.html">SysLog Appender</a>, these details, including clear password, will be sent to the log server.
</div>

The above variables may be used in logging patterns:

- Use `%X` by itself to include all variables.
- Use `%X{key}` to include the specified variable.

```xml
<Console name="console" target="SYSTEM_OUT">
    <PatternLayout pattern="%X{locale} %d %p [%c] - &lt;%m&gt;%n"/>
</Console>
```
