---
layout: default
title: Dynamic Themes - User Interface Customization - CAS
category: User Interface
---

{% include variables.html %}

# Static Themes

CAS is configured to decorate views based on the `theme` property of a given 
registered service in the Service Registry. The theme that is activated via 
this method will still preserve the default views for CAS but will apply 
decorations such as CSS and JavaScript to the views. The physical structure 
of views cannot be modified via this method.

{% include_cached casproperties.html properties="cas.theme." %}
                       
To create a theme, please follow the below instructions:

- Add a `[theme_name].properties` placed to the root of `src/main/resources` folder. 
Contents of this file may contain the following settings

{% include_cached themeproperties.html %}

- Create the directory `src/main/resources/static/themes/[theme_name]`. Put the 
  theme-specific `cas.css` and `cas.js` inside the appropriate directories for `css` and `js`.
- Specify `[theme_name]` for the service definition under the `theme` property.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://www.example.org",
  "name" : "MyTheme",
  "theme" : "[theme_name]",
  "id" : 1000
}
```

Values can use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.
