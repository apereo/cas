---
layout: default
title: Themes - User Interface Customization - CAS
category: User Interface
---

{% include variables.html %}

# Themed Views - User Interface Customization

CAS can also utilize a service's associated theme to selectively choose which set of UI views will be used to generate 
the standard views (`login/casLoginView.html`, etc). This is especially useful in cases where the set of pages for a theme that are targeted
for a different type of audience are entirely different structurally that using a simple theme is not practical 
to augment the default views. In such cases, new view pages may be required.

Views associated with a particular theme by default are expected to be found at: `src/main/resources/templates/<theme-id>`. Note that CAS 
views and theme-based views may both be externalized out of the web application context. When externalized, themed 
views are expected to be found at the specified path via CAS properties under a 
directory named after the theme. For instance, if the external path for CAS views is `/etc/cas/templates`, view template files for 
theme `sample` may be located `/etc/cas/templates/sample/`.

{% include_cached casproperties.html properties="cas.view.template-" %}

## Configuration

- Add a `[theme_name].properties` placed to the root of `src/main/resources` folder. Contents of this file should match the following:

```properties
cas.standard.css.file=/themes/[theme_name]/css/cas.css
cas.standard.js.file=/themes/[theme_name]/js/cas.js
```

- Clone the default set of view pages into a new directory based on the theme id (i.e. `src/main/resources/templates/<theme-id>`).
- Specify the name of your theme for the service definition under the `theme` property.
