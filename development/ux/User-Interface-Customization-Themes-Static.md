# Static Themes

Source: https://apereo.github.io/cas/development/ux/User-Interface-Customization-Themes-Static.html

CAS is configured to decorate views based on the `theme` property of a given registered service in the Service Registry. The theme that is activated via this method will still preserve the default views for CAS but will apply decorations such as CSS and JavaScript to the views. The physical structure of views cannot be modified via this method.

The following settings and properties are available from the CAS configuration catalog:

- `cas.theme.default-theme-name` (Optional; type `String`; default `cas-theme-default`): The default theme name of this CAS deployment. The default theme file `cas-theme-default.properties` can be modified and extended by the theme file `cas-theme-custom.properties`.
- `cas.theme.description` (Optional): The description for this theme to describe its purpose
- `cas.theme.name` (Optional): Default theme name
- `cas.theme.param-name` (Optional; type `String`; default `theme`): The parameter name used to switch themes.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

To create a theme, please follow the below instructions:

- Add a `[theme_name].properties` placed to the root of `src/main/resources` folder. Contents of this file may contain the following settings

| Name | Value | Description |
| --- | --- | --- |
| `cas.theme.name` | `CAS` | Default theme name |
| `cas.standard.css.file` | `/css/cas.css,/css/custom.css` | Path to theme CSS file; `/themes/[theme_name]/css/cas.css`. Multiple files may be comma-separated. |
| `cas.standard.js.file` | `/js/cas.js,/js/material.js,/js/custom.js` | Path to theme JavaScript file; `/themes/[theme_name]/js/js/css` Multiple files may be comma-separated. |
| `cas.logo.enabled` | `true` | Whether to show the CAS logo |
| `cas.logo.file` |  | Path to theme logo to display via the common layout. Example: `/images/logo.png` |
| `cas.drawer-menu.enabled` | `true` | Decide whether drawer menu should be displayed. |
| `cas.pm-links.enabled` | `true` | Whether password management/reset links should be displayed. |
| `cas.pm-links.forgot-password.url` |  | When password management is turned off, the address of an external page where users can recover their password. A link to it is shown with the other password management links; nothing is shown when blank. |
| `cas.login-form.enabled` | `true` | When the CAS login form should be displayed. |
| `cas.login-form.legend.enabled` | `true` | Display a legend on the login form to indicate the purpose of the form and fields. |
| `cas.login-form.password-reveal.enabled` | `true` | Whether the password field on the login form offers a button to show and hide the password as it is typed. |
| `cas.login-form.caps-lock-warning.enabled` | `true` | Whether the login form warns that Caps Lock is on while the password is typed. |
| `cas.login-form.terms-of-use.enabled` | `true` | Whether the terms of use should be displayed on the login form, and whether the user should be required to accept the terms of use. |
| `cas.login-form.device-fingerprint.enabled` | `true` | Whether device fingerprinting features should be enabled, allowing CAS to catch and record device fingerprints for each authentication attempt. |
| `cas.login-form.remember-username.enabled` | `false` | Whether CAS should remember the username entered on the login form for future use. Only relevant when public workstation features are disabled. |
| `cas.notifications-menu.enabled` | `true` | Enable and display the notifications menu. |
| `cas.theme-builder.enabled` | `true` | Enable and display the theme builder next to the notifications menu. The theme builder lets you adjust theme properties and styles on the page, preview the changes live and download them as a theme properties file and a custom.css file for your deployment. Changes stay in the browser and are never saved on the server; turn this off for production deployments. |
| `cas.favicon.file` |  | Path to the theme favicon Example: `/themes/example/images/favicon.ico` |
| `cas.theme.description` | `Central` | The description for this theme to describe its purpose |
| `cas.main-content.position` | `topcenter` | Where the main content of each page sits on the screen, seen as a 3x3 grid: topleft, topcenter, topright, middleleft, middlecenter, middleright, bottomleft, bottomcenter or bottomright. |
| `cas.hero-banner.file` |  | Path to a “hero” styled image/logo that would be displayed on the login form. |
| `cas.js.core.enabled` | `true` | Whether core/default JavaScript libraries should be included in the theme. |
| `cas.css.core.enabled` | `true` | Whether the theme should attempt to include CAS-provided CSS core libraries |
| `cas.locale-selection.enabled` | `true` | Whether the theme should display a language selection menu |
| `cas.locale-selection.languages` | `en,de,fr,it,es,ar,fa,ja,nl,pl,ru,sk,tr,pt_BR,zh_CN,zh_TW` | When language selection menu is enabled, control the allowed languages that can be displayed and selected in the menu |
| `cas.successful-login.display-attributes` | `true` | Whether principal/authentication attributes and other application data should be displayed on the screen after a successful “generic” authentication event. |
| `cas.public-workstation.enabled` | `false` | Whether user should be given the option to indicate if the current device is a public workstation |
| `cas.warn-on-redirect.enabled` | `false` | Whether use should be given the option to receive a warning prior to redirects to external sites and domains |
| `cas.standard.fragments.head` |  | Name of fragments that would be found inside `fragments/custom.html`. Such fragments would be included in the global layout inside the head tag of each affected page |
| `cas.browser-storage.show-progress` | `true` | While reading or writing data to browser storage, control whether CAS should show its user interface, progress bar, etc. |
| `cas.footer.show` | `true` | Whether the footer should be displayed |
| `cas.footer.show-version` | `true` | When footer is displayed, control whether the CAS version should be shown and listed. |
| `cas.footer.show-host` | `true` | When footer is displayed, control whether the CAS server host should be shown and listed. |
| `cas.footer.show-client-ip` | `true` | When footer is displayed, control whether the client IP address should be shown and listed. |
| `cas.service-ui.show` | `true` | Control whether CAS should display information about the current application requesting authentication. |
| `cas.service-ui.show-details` | `true` | Control whether CAS should display additional details about the application requesting authentication such information urls, privacy URLs, description, etc. |
| `cas.login-side-panel.enabled` | `false` | When enabled, CAS will display a side panel to the left of the login form that is primarily used to show customizations or additional information about the service. |
| `cas.login-side-panel.position` | `start` | Where the login side panel sits next to the login form, when the side panel is enabled: start or end. |
| `cas.login-providers.position` | `after` | Where external identity providers are listed relative to the login form: after or before. |
| `cas.login-providers.layout` | `stacked` | How external identity provider buttons are arranged: stacked (one per row), grid (two columns) or inline (buttons side by side, wrapping as needed). |
| `cas.login-providers.select-threshold` | `10` | Once more external identity providers than this are available, they are offered in a dropdown instead of as buttons. |
| `cas.login-providers.sort` | `false` | Whether external identity providers are sorted by name. When false, they are listed in the order CAS configures them. |
| `cas.error.show-message` | `true` | Whether error pages show the message of the error. |
| `cas.error.show-details` | `false` | Whether error pages offer a button to view error details, such as a stack trace, when the server includes them. |
| `cas.webauthn.authentication.show-device-details` | `true` | Whether WebAuthn shows the details of the authenticated device (username, nickname, registration date and device) once authentication succeeds. |
| `cas.webauthn.registration.recommended` | `passkey` | The registration option that WebAuthn presents as recommended: passkey, security-key or none. |
| `cas.gauth.registration.show-secret-key` | `true` | Whether Google Authenticator registration shows the secret key that can be typed into the app instead of scanning the QR code. |
| `cas.gauth.registration.scratch-codes` | `chips` | How Google Authenticator registration shows scratch codes: chips or list. |
| `cas.gauth.registration.show-print` | `true` | Whether Google Authenticator registration offers a print button. |
| `cas.qr-code.size` | `250` | Size of generated QR codes in pixels, used by Google Authenticator, WebAuthn and QR code login. |
| `cas.qr-code.foreground` | `#000000` | Color of the modules of generated QR codes, as #rrggbb. It must be darker than the background color. |
| `cas.qr-code.background` | `#FFFFFF` | Background color of generated QR codes, as #rrggbb. |
| `cas.qr-code.margin` | `2` | Width of the quiet zone around generated QR codes, in modules. |
| `cas.mfa.selection.layout` | `list` | How the multifactor authentication provider selection lists providers: list, grid or cards. |
| `cas.mfa.device-name.prefill` | `random` | How device and account names are suggested when registering a multifactor authentication device (trusted devices, Google Authenticator, YubiKey, WebAuthn): random, empty or username. |
| `cas.consent.show-attribute-values` | `true` | Whether attribute consent shows the values of the attributes that are released, or only their names. |
| `cas.consent.layout` | `table` | How attribute consent lists the released attributes: table or list. |

- Create the directory `src/main/resources/static/themes/[theme_name]`. Put the theme-specific `cas.css` and `cas.js` inside the appropriate directories for `css` and `js`.
- Specify `[theme_name]` for the service definition under the `theme` property.

A theme only needs the properties it changes. Every other property comes from `cas-theme-custom.properties`, the file that holds your deployment-wide changes, and then from `cas-theme-default.properties`. Theme files found under the template prefixes apply to every browser language.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://www.example.org",
  "name" : "MyTheme",
  "theme" : "[theme_name]",
  "id" : 1000
}
```

Values can use the [Spring Expression Language](https://apereo.github.io/cas/development/configuration/Configuration-Spring-Expressions.html) syntax.
