---
layout: default
title: CAS - User Interface Customization
---

# Views

CAS uses [Thymeleaf](http://www.thymeleaf.org/) to build and render views. Thymeleaf's main goal is to bring elegant natural templates to your development workflow — HTML pages that can be correctly displayed in browsers and also work as static prototypes, allowing for stronger collaboration in development teams.

## Configuration

CAS views are found at `src/main/resources/templates`, which translates to `classpath:/templates/` when deployed. While this is the default setting, you are also allowed options to move the directory to a location outside the main CAS web application, or if needed, deploy CAS with an entirely different set of views in one tier while still preserving the default look and feel for another deployment tier.

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#views).

## Warning Before Accessing Application

CAS has the ability to warn the user before being redirected to the service. This allows users to be made aware whenever an application uses CAS to log them in.
(If they don't elect the warning, they may not see any CAS screen when accessing an application that successfully relies upon an existing CAS single sign-on session.)
Some CAS adopters remove the 'warn' checkbox in the CAS login view and don't offer this interstitial advisement that single sign-on is happening.

```html
...
<input id="warn"
       name="warn"
       value="true"
       tabindex="3"
       th:accesskey="#{screen.welcome.label.warn.accesskey}"
       type="checkbox" />
<label for="warn" th:utext="#{screen.welcome.label.warn}"/>
...
```

## "I am at a public workstation" authentication

CAS has the ability to allow the user to opt-out of SSO, by indicating on the login page that the authentication
is happening at a public workstation. By electing to do so, CAS will not honor the subsequent SSO session
and will not generate the TGC that is designed to do so.

```html
...
<input id="publicWorkstation"
       name="publicWorkstation"
       value="false" tabindex="4"
       type="checkbox" />
<label for="publicWorkstation" th:utext="#{screen.welcome.label.publicstation}"/>
...
```

## Default Service

In the event that no `service` is submitted to CAS, you may specify a default
service url to which CAS will redirect. Note that this default service, much like
all other services, MUST be authorized and registered with CAS.

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#views).
