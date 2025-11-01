---
layout: default
title: Overview - User Interface Customization - CAS
category: User Interface
---

# Overview

Branding the CAS User Interface (UI) involves editing the CSS stylesheet and also a small collection of relatively simple HTML include files, also known as views. Optionally, you may also wish to modify the text displayed and/or add additional Javascript effects
on these views.

# Browser Support

CAS user interface should properly and comfortably lend itself to all major browser vendors:

* Google Chrome
* Mozilla Firefox
* Apple Safari
* Microsoft Edge
* Internet Explorer (v11 only)

Note that certain older version of IE, particularly IE 9 and below may impose additional difficulty in getting the right UI configuration in place. The libraries used for the user interface (Bootstrap and Material.io) do not support Internet Explorer 9 or below.

<div class="alert alert-info"><strong>Supported Browsers</strong><p>The supported browsers listed here are in reference to the default CAS user interface. Customizations can be implemented to support other browsers using the overlay, themes, etc.</p></div>

## Internet Explorer

To instruct CAS to render UI in compatibility mode, the following is added automatically to relevant UI components:

```html
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
```

# CSS & Javascript

See [this guide](User-Interface-Customization-CSSJS.html) for more info.

# Views

See [this guide](User-Interface-Customization-Views.html) for more info.

# Localization

See [this guide](User-Interface-Customization-Localization.html) for more info.

# Themes

See [this guide](User-Interface-Customization-Themes.html) for more info.
