---
layout: default
title: Views - User Interface Customization - CAS
category: User Interface
---

{% include variables.html %}

# Views

The views are found inside the CAS web application in the `WEB-INF\lib\cas-server-support-thymeleaf-<cas.version>.jar` 
in the templates folder. Add any views that require customization to the `src/main/resources/templates` folder in 
the CAS overlay project. Any files found in that module can be overridden by putting them in the same 
location under `src/main/resources` in the CAS overlay project. The Gradle build script for the overlay 
has tasks that help get resources from the CAS web application to the correct location in the CAS overlay. 

## Templates

{% include_cached userinterface-templates.html  %}

## Configuration

{% include_cached casproperties.html properties="cas.view.template-" %}

## Warning Before Accessing Application

CAS has the ability to warn the user before being redirected to the service. This allows users 
to be made aware whenever an application uses CAS to log them in. (If they don't elect the 
warning, they may not see any CAS screen when accessing an application that successfully 
relies upon an existing CAS single sign-on session.) Some CAS adopters remove the 'warn' 
checkbox in the CAS login view and don't offer this interstitial advisement that single sign-on is happening.

```html
...
<input id="warn"
   name="warn"
   value="true"
   th:accesskey="#{screen.welcome.label.warn.accesskey}"
   type="checkbox" />
<label for="warn" th:utext="#{screen.welcome.label.warn}"/>
...
```      
