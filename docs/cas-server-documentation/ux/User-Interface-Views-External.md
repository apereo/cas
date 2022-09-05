---
layout: default
title: Views - User Interface Customization - CAS
category: User Interface
---

{% include variables.html %}

# User Interface - External Views

Views also may be externalized outside the web application conditionally and individually, provided the external path 
via CAS settings. If a view template file is not found at the externalized path, the 
default one that ships with CAS will be used as the fallback.

Views may also be found using an external URL in CAS settings that is responsible to produce the full view body in 
the response. This URL endpoint will receive the available request headers as well as the following headers in its request:

| Header                 |
|------------------------|
| `owner`                |
| `template`             |
| `resource`             |
| `theme`, if available  |
| `locale`, if available |

Upon a successful `200` status result, the response body is expected to contain the view that will be rendered by CAS.

{% include_cached casproperties.html properties="cas.view.rest" %}
