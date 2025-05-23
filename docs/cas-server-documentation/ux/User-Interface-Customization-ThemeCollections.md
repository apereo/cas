---
layout: default
title: Themes - User Interface Customization - CAS
category: User Interface
---

{% include variables.html %}

# Theme Collections - User Interface Customization

CAS provides a module that presents a number of ready-made themes. The intention for each themes
to account for common and provide for common use cases when it comes to user interface modifications
and samples, and attempt to automate much of the configuration.

Support is enabled by including the following module in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-themes-collection" %}

The following themes are provided by this module and can be assigned to service definitions:

| Theme     | Description                                                                                     |
|-----------|-------------------------------------------------------------------------------------------------|
| `example` | A reference example theme that combines customized CSS, JavaScript and views                    |
| `twbs`    | A basic theme utilizing [Bootstrap](http://getbootstrap.com "Bootstrap") for CSS and JavaScript |

The collection of themes above can also serve as reference examples of how to define a theme with
custom CSS, JavaScript and associated views and fragments.
