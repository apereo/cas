---
layout: default
title: Views - User Interface Customization - CAS
category: User Interface
---

{% include variables.html %}

# User Interface - Thymeleaf

CAS uses [Thymeleaf](https://www.thymeleaf.org) for its markup rendering engine. Each template is 
decorated by `layout.html` template file, which provides a layout structure for the template's content. Individual 
components optimized for re-use among multiple templates are stored in the `src/main/resources/templates/fragments` 
folder, and referenced by the templates in `src/main/resources/templates`.

Refer to the [Thymeleaf documentation](https://www.thymeleaf.org/) for more information on its use and syntax.

{% include_cached casproperties.html properties="spring.thymeleaf" %}
