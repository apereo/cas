---
layout: default
title: CAS - Monitoring & Statistics
category: Monitoring & Statistics
---

{% include variables.html %}

# Actuator Endpoint - Logfile

Returns the contents of the logfile (if the `logging.file.name` or the `logging.file.path` property has been set). 
Supports the use of the HTTP `Range` header to retrieve part of the log file’s content.

{% include_cached actuators.html endpoints="logfile" %}
