# User Interface - CAS v1 View

Source: https://apereo.github.io/cas/development/ux/User-Interface-Views-CASv1.html

You can control the response output when a client application interacts with CAS using the CAS v1 protocol.

The following settings and properties are available from the CAS configuration catalog:

- `cas.view.cas1.attribute-renderer-type` (Optional; type `Cas10ViewProperties.ValidationAttributesRendererTypes`; default `DEFAULT`): Indicates how attributes in the final validation response should be formatted. Available values are as follows: - `DEFAULT`: Render attributes using CAS protocol suggestions. - `VALUES_PER_LINE`: Inline attribute value(s) on each line.
