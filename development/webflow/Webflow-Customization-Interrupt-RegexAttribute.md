# Regex Attribute Authentication Interrupt

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-Interrupt-RegexAttribute.html

This strategy allows one to define regular expression patterns in CAS settings that would be matched against attributes names and values. If a successful match is produced while CAS examines the collection of both authentication and principal attributes, the authentication flow would be interrupted.

The following settings and properties are available from the CAS configuration catalog:

- `cas.interrupt.regex.attribute-name` (Required; type `String`): A regex pattern on the attribute name that if matches will successfully complete the first condition for the interrupt notifications trigger.
- `cas.interrupt.regex.attribute-value` (Required; type `String`): A regex pattern on the attribute value that if matches will successfully complete the first condition for the interrupt notifications trigger.
