---
layout: default
title: Overview - User Interface Customization - CAS
category: User Interface
---

{% include variables.html %}

# Overview

Branding the CAS User Interface (UI) involves editing the CSS stylesheet and also a small collection of relatively simple HTML include files, also known as views. Optionally, you may also wish to modify the text displayed and/or add additional JavaScript effects on these views.

# Browser Support

CAS user interface should properly and comfortably lend itself to all major browser vendors:

* Google Chrome
* Mozilla Firefox
* Apple Safari
* Microsoft Edge

Note that certain older version of IE, particularly IE 9 and below may impose additional difficulty 
in getting the right UI configuration in place. The libraries used for the user interface (Bootstrap and Material.io) 
do not support Internet Explorer 9 or below.

<div class="alert alert-info">:information_source: <strong>Supported Browsers</strong><p>The supported 
browsers listed here are in reference to the default CAS user interface. Customizations can be 
implemented to support other browsers using the overlay, themes, etc.</p></div>

**DISCLAIMER**

The CAS project does not *officially* support any kind of browser and tries to remain browser-agnostic as much as possible. 
By *support*, we mean that there is almost no special logic, or code
or configuration that ensures CAS runs on a particular browser. Furthermore, there is limited form of browser testing to ensure
CAS renders correctly and runs correctly for browsers. Please [see this](../../developer/Test-Process.html) for more details.

The CAS project's main focus is on *modern* browsers; that is recent versions Firefox, Chrome, Edge, Safari, etc. 
Old or EOL browser versions (such as Internet Explorer) that either misbehave or do not support the relevant web 
standards are likely to run into issues and you will need to be prepared
to own, maintain and manage those issues yourself. Even if the browser is super modern and recent, note that complicated weird convoluted 
fixes that look more like hacks or make a lot of internal assumptions about a particular browser are rejected as contributions and 
you will be advised to own the fix going forward and contact the relevant browser community/vendor to fix the problem
at the source where it belongs.

## Internet Explorer

To instruct CAS to render UI in compatibility mode, the following is added automatically to relevant UI components:

```html
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
```

# CSS & JavaScript

See [this guide](User-Interface-Customization-CSSJS.html) for more info.

# Views

See [this guide](User-Interface-Customization-Views.html) for more info.

# Localization

See [this guide](User-Interface-Customization-Localization.html) for more info.

# Themes

See [this guide](User-Interface-Customization-Themes.html) for more info.
