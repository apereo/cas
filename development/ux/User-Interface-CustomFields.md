# User Interface - Custom Fields

Source: https://apereo.github.io/cas/development/ux/User-Interface-CustomFields.html

CAS allows on the ability to dynamically extend the login form by including additional fields, to be populated by the user. Such fields are taught to CAS using settings and are then bound to the authentication flow and made available to all authentication handlers that wish to impose additional processes and rules using said fields.

The following settings and properties are available from the CAS configuration catalog:

- `cas.view.custom-login-form-fields.[key].converter` (Optional; type `String`): The id of the custom converter to use to convert bound property values.
- `cas.view.custom-login-form-fields.[key].message-bundle-key` (Optional; type `String`): The key for this field found in the message bundle used to present a label/text in CAS views.
- `cas.view.custom-login-form-fields.[key].required` (Optional; type `boolean`): Whether this field is required to have a value.
