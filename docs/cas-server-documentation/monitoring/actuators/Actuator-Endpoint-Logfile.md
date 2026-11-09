---
layout: default
title: CAS - Actuator Endpoint - Logfile
category: Monitoring & Statistics
---

{% include variables.html %}

# Actuator Endpoint - Logfile

Returns the contents of the logfile, if the name or the path of the log file has been set in the logging settings. 
Supports the use of the HTTP `Range` header to retrieve part of the log file’s content.

{% include_cached casproperties.html thirdPartyExactMatch="logging.file.name,logging.file.path" %}

{% include_cached actuators.html endpoints="logfile" %}
