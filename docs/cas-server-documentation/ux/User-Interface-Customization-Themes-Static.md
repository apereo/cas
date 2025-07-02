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
Contents of this file may contain the following settings:

| Setting                                   | Description                                                                              | Value                                |
|-------------------------------------------|------------------------------------------------------------------------------------------|--------------------------------------|
| `cas.standard.css.file`                   | Path to theme CSS file; Multiple files may be comma-separated.                           | `/themes/[theme_name]/css/cas.css`   |
| `cas.standard.js.file`                    | Path to theme JavaScript file; Multiple files may be comma-separated.                    | `/themes/[theme_name]/js/js/css`     |
| `cas.standard.fragments.head`             | Names of fragments found in `custom.html` fragment, included in the layout `<head>` tag  | Blank                                |
| `cas.logo.file`                           | Path to theme logo to display via the common layout.                                     | `/images/logo.png`                   |
| `cas.drawer-menu.enabled`                 | Decide whether drawer menu should be displayed.                                          | `true`                               |
| `cas.theme.name`                          | Theme name used in various titles/captions.                                              | `Example Theme`                      |
| `cas.theme.description`                   | Theme description used in various titles/captions.                                       | `Example Theme Description`          |
| `cas.pm-links.enabled`                    | Whether password management/reset links should be displayed.                             | `true`                               |
| `cas.login-form.enabled`                  | When the CAS login form should be displayed.                                             | `true`                               |
| `cas.notifications-menu.enabled`          | Enable and display the notifications menu.                                               | `true`                               |
| `cas.favicon.file`                        | Path to theme favicon file.                                                              | `/themes/example/images/favicon.ico` |
| `cas.hero-banner.file`                    | Path to a "hero" styled image/logo on the login form.                                    | `/themes/example/images/hero.png`    |
| `cas.js.core.enabled`                     | Whether core/default JavaScript libraries should be included.                            | `true`                               |
| `cas.css.core.enabled=true`               | Whether core/default CSS libraries should be included.                                   | `true`                               |
| `cas.successful-login.display-attributes` | Whether attributes/applications should be displayed on login.                            | `true`                               |
| `cas.public-workstation.enabled`          | Whether user can indicate a public workstation option on login.                          | `false`                              |
| `cas.warn-on-redirect.enabled`            | Whether user should be warned prior to redirects to applications.                        | `false`                              |
| `cas.browser-storage.show-progress`       | Whether to display progress when reading/writing browser storage data.                   | `true`                               |
| `cas.footer.show`                         | Whether to display the CAS footer.                                                       | `true`                               |
| `cas.footer.show-version`                 | Whether to display CAS version details in the footer.                                    | `true`                               |
| `cas.footer.show-host`                    | Whether to display CAS server host in the footer.                                        | `true`                               |
| `cas.login-form.legend.enabled`           | Whether to display a legend for login form fields.                                       | `true`                               |
| `cas.login-form.terms-of-use.enabled`     | Whether to display messaging on the login form to indicate the terms of use requirement. | `true`                               |


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
