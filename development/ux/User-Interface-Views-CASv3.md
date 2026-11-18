# User Interface - CAS v3 View

Source: https://apereo.github.io/cas/development/ux/User-Interface-Views-CASv3.html

You can control the response output when a client application interacts with CAS using the CAS v3 protocol.

The following settings and properties are available from the CAS configuration catalog:

- `cas.view.cas3.attribute-renderer-type` (Optional; type `Cas30ViewProperties.ValidationAttributesRendererTypes`; default `DEFAULT`): Indicates how attributes in the final validation response should be formatted. Available values are as follows: - `DEFAULT`: Render attributes using CAS protocol suggestions. - `INLINE`: Inline attribute name/value as XML attributes.
- `cas.view.cas3.failure` (Optional; type `String`; default `protocol/3.0/casServiceValidationFailure`): The relative location of the CAS3 success validation bean.
- `cas.view.cas3.success` (Optional; type `String`; default `protocol/3.0/casServiceValidationSuccess`): The relative location of the CAS3 success validation bean.
