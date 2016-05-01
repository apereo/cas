---
layout: default
title: CAS - User Interface Customization
---

# Dynamic Themes
With the introduction of [Service Management application](Service-Management.html), deployers are now able to switch the themes based on different services. For example, you may want to have different login screens (different styles) for staff applications and student applications. Or, you want to show two layouts for day time and night time. This document could help you go through the basic settings to achieve this.

Note that support for themes comes with the following components:

| Component                      | Description
|--------------------------------+--------------------------------------------------------------------------------+
| `ServiceThemeResolver`  | can be configured to decorate CAS views based on the `theme` property of a given registered service in the Service Registry. The theme that is activated via this method will still preserve the default views for CAS but will simply apply decorations such as CSS and Javascript to the views. The physical structure of views cannot be modified via this method.
| `RegisteredServiceThemeBasedViewResolver` | If there is a need to present an entirely new set of views for a given service, such that
the structure and layout of the page needs an overhaul with additional icons, images, text, etc then this component needs to be
configured. This component will have the ability to resolve a new set of views that may entirely be different from the default views. The `theme` property of a given registered service in the Service Registry will still need to be configured to note the set of views that are to be loaded.


## `ServiceThemeResolver`
Configuration of service-specific themes is backed by the Spring framework and provided by the following component:

Furthermore, deployers may be able to use the functionality provided by the `ThemeChangeInterceptor` of Spring framework to provide theme configuration per each request.

### Configuration
- Add another theme properties file, which must be placed to the root of `src/main/resources` folder, name it as `theme_name.properties`. Contents of this file should match the `cas-theme-default.properties` file.
- Add the location of related styling files, such as CSS and Javascript in the file above.
- Specify the name of your theme for the service definition under the `theme` property.

## `RegisteredServiceThemeBasedViewResolver`
`RegisteredServiceThemeBasedViewResolver` is an alternate Spring View Resolver that utilizes a service's
associated theme to selectively choose which set of UI views will be used to generate the standard views (`casLoginView.html`, etc). This is specially useful in cases where the set of pages for a theme that are targeted
for a different type of audience are entirely different structurally that simply
using the `ServiceThemeResolver` is not practical to augment the default views. In such cases, new view pages may be required.

Views associated with a particular theme by default are expected to be found at: `src/main/resources/templates/<theme-id>`

### Configuration
- Clone the default set of view pages into a new directory based on the theme id (i.e. `src/main/resources/templates/<theme-id>`).
- Specify the name of your theme for the service definition under the `theme` property.
