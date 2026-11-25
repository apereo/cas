# User Interface - CAS v2 View

Source: https://apereo.github.io/cas/development/ux/User-Interface-Views-CASv2.html

You can control the response output when a client application interacts with CAS using the CAS v2 protocol.

The following settings and properties are available from the CAS configuration catalog:

- `cas.view.cas2.failure` (Optional; type `String`; default `protocol/2.0/casServiceValidationFailure`): The relative location of the CAS3 failure view bean.
- `cas.view.cas2.proxy.failure` (Optional; type `String`; default `protocol/2.0/casProxyFailureView`): The relative location of the CAS2 proxy failure view bean.
- `cas.view.cas2.proxy.success` (Optional; type `String`; default `protocol/2.0/casProxySuccessView`): The relative location of the CAS2 proxy success view bean.
- `cas.view.cas2.success` (Optional; type `String`; default `protocol/2.0/casServiceValidationSuccess`): The relative location of the CAS2 success view bean.
- `cas.view.cas2.v3-forward-compatible` (Optional; type `Boolean`; default `true`): Whether v2 protocol support should be forward compatible to act like v3 and match its response, mainly for attribute release.
