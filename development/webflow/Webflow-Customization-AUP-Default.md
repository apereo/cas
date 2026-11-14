# Default Acceptable Usage Policy

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP-Default.html

By default the task of remembering the user’s choice is kept in memory by default and will be lost upon container restarts and/or in clustered deployments. This option is only useful during development, testing and demos and is not at all suitable for production.

The following settings and properties are available from the CAS configuration catalog:

- `cas.acceptable-usage-policy.in-memory.scope` (Optional; type `InMemoryAcceptableUsagePolicyProperties.Scope`; default `GLOBAL`): Scope of map where the aup selection is stored. Available values are as follows: - `GLOBAL`: Store in global in-memory map (for life of server). - `AUTHENTICATION`: Store aup acceptance such that user is prompted when they authenticate via credentials.

The scope of the default storage mechanism can be adjusted from the default of GLOBAL (described above) to AUTHENTICATION which will result in the user having to agree to the policy during each authentication event. The user will not have to agree to the policy when CAS grants access based on an existing ticket granting ticket cookie.
