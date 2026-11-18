# X.509 Authentication - Principal Resolution

Source: https://apereo.github.io/cas/development/authentication/X509-Authentication-Principal-Resolution.html

X.509 authentication is about rules and conditions that would allow CAS to extract a subject identifier from the client certificate presented during the SSL/TLS handshake process and turning into a CAS principal. This is a critical step in the authentication process to ensure that the principal identifier extracted from the certificate is meaningful and can be used to locate and identify the user for attributes later on.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.x509.principal-type` (Required; type `X509Properties.PrincipalTypes`; default `SUBJECT_DN`): Indicates the type of principal resolution for X509. Available values are as follows: - `CN_EDIPI`: Create principal by common name and EDIPI. - `RFC822_EMAIL`: Create principal from the RFC822 type name (aka email address) in the subject alternative name field. The subject alternative name field contains a list of various types of names, one type is RFC822 e-mail address. This will return the first e-mail address that is found (if there are more than one). - `SERIAL_NO`: Create principal by serial no. Resolve the principal by the serial number with a configurable **radix**, ranging from 2 to 36. If `radix` is `16`, then the serial number could be filled with leading zeros to even the number of digits. - `SERIAL_NO_DN`: Create principal by serial no and DN. - `SUBJECT`: Create principal by subject. Resolve the principal by extracting one or more attribute values from the certificate subject DN and combining them with intervening delimiters. - `SUBJECT_ALT_NAME`: Create principal by subject alternative name. Resolve the principal by the subject alternative name extension. (type: otherName). - `SUBJECT_DN`: Create principal by subject DN.
- `cas.authn.x509.principal.principal-transformation.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.x509.principal.active-attribute-repository-ids` (Optional; type `String`; default `*`): Activated attribute repository identifiers that should be used for fetching attributes if attribute resolution is enabled. The list here may include identifiers separated by comma.
- `cas.authn.x509.principal.attribute-repository-selection` (Optional; type `Map<String,String>`): Control the behavior of the attribute repository selection by authentication method or handler. The map here is keyed by the authentication handler name, and the value is the attribute repository identifiers separated by comma. When the authentication handler is executed, the attribute repositories assigned to this handler will be selected to fetch attributes. Note that the resolution engine will always favor attribute repositories assigned to the service definition, if any and as part of its authentication policy, over this global setting.
- `cas.authn.x509.principal.attribute-resolution-enabled` (Optional; type `TriStateBoolean`; default `undefined`): Whether attribute repositories should be contacted to fetch person attributes. Defaults to true if not set. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.x509.principal.principal-attribute` (Optional; type `String`): Attribute name to use to indicate the identifier of the principal constructed. If the attribute is blank or has no values, the default principal id will be used determined by the underlying authentication engine. The principal id attribute usually is removed from the collection of attributes collected, though this behavior depends on the schematics of the underlying authentication strategy.
- `cas.authn.x509.principal.principal-resolution-conflict-strategy` (Optional; type `String`; default `last`): In the event that the principal resolution engine resolves more than one principal, (especially if such principals in the chain have different identifiers), this setting determines strategy by which the principal id would be chosen from the chain. Accepted values are: `last`, `first`.
- `cas.authn.x509.principal.principal-resolution-failure-fatal` (Optional; type `TriStateBoolean`; default `undefined`): When true, throws an error back indicating that principal resolution has failed and no principal can be found based on the authentication requirements. Otherwise, logs the condition as an error without raising a catastrophic error. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.x509.principal.principal-transformation.blocking-pattern` (Optional; type `String`): A regular expression that will be used against the username to match for blocking/forbidden values. If a match is found, an exception will be thrown and principal transformation will fail.
- `cas.authn.x509.principal.principal-transformation.case-conversion` (Optional; type `PrincipalTransformationProperties.CaseConversion`; default `NONE`): Indicate whether the principal identifier should be transformed into upper-case, lower-case, etc. Available values are as follows: - `NONE`: No conversion. - `LOWERCASE`: Lowercase conversion. - `UPPERCASE`: Uppercase conversion.
- `cas.authn.x509.principal.principal-transformation.pattern` (Optional; type `String`): A regular expression that will be used against the provided username for username extractions. On a successful match, the first matched group in the pattern will be used as the extracted username.
- `cas.authn.x509.principal.principal-transformation.prefix` (Optional; type `String`): Prefix to add to the principal id prior to authentication.
- `cas.authn.x509.principal.principal-transformation.suffix` (Optional; type `String`): Suffix to add to the principal id prior to authentication.
- `cas.authn.x509.principal.return-null` (Optional; type `TriStateBoolean`; default `undefined`): Return a null principal object if no attributes can be found for the principal. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.x509.principal.use-existing-principal-id` (Optional; type `TriStateBoolean`; default `undefined`): Uses an existing principal id that may have already been established in order to run person directory queries. This is generally useful in situations where authentication is delegated to an external identity provider and a principal is first established to then query an attribute source. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

**Principal transformation**

Authentication handlers that generally deal with username-password credentials can be configured to transform the user id prior to executing the authentication sequence. Each authentication strategy in CAS provides settings to properly transform the principal. Refer to the relevant settings for the authentication strategy at hand to learn more.

Authentication handlers as part of principal transformation may also be provided a path to a Groovy script to transform the provided username. The outline of the script may take on the following form:

```groovy
String run(final Object... args) {
    def (providedUsername,logger) = args
    return providedUsername.concat("SomethingElse")
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

## Principal Resolution Types

The following principal resolution types are available.

**SUBJECT_DN**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.x509.subject-dn.format` (Optional; type `SubjectDnPrincipalResolverProperties.SubjectDnFormat`; default `DEFAULT`): Format of subject DN to use. Available values are as follows: - `DEFAULT`: Denigrated result of calling certificate.getSubjectDN() method. Javadocs designate this method as "denigrated" for not being portable and/or not being well defined. It is what has been used by CAS for a long time so it remains the default. - `RFC1779`: RFC 1779 String format of Distinguished Names. Calls `X500Principal.getName("RFC1779")` which emits a subject DN with the attribute keywords defined in RFC 1779 (CN, L, ST, O, OU, C, STREET). Any other attribute type is emitted as an OID. - `RFC2253`: RFC 2253 String format of Distinguished Names. Calls `X500Principal.getName("RFC2253")` which emits a subject DN with the attribute keywords defined in RFC 2253 (CN, L, ST, O, OU, C, STREET, DC, UID). Any other attribute type is emitted as an OID. - `CANONICAL`: Canonical String format of Distinguished Names. Calls X500Principal.getName("CANONICAL" which emits a subject DN that starts with RFC 2253 and applies additional canonicalization described in the javadoc.

**CN_EDIPI**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.x509.cn-edipi.alternate-principal-attribute` (Optional; type `String`): Attribute name that will be used by X509 principal resolvers if the main attribute in the certificate is not present. This only applies to principal resolvers that are looking for attributes in the certificate that are not common to all certificates. (e.g. `SUBJECT_ALT_NAME`, `CN_EDIPI`) This assumes you would rather get something like the `subjectDn` rather than `null` where `null` would allow falling through to another authentication mechanism. Currently supported values are: `subjectDn`, `sigAlgOid`, `subjectX500Principal`.
- `cas.authn.x509.cn-edipi.extract-edipi-as-attribute` (Optional; type `Boolean`; default `false`): Whether to extract EDIPI as an attribute, regardless of principal resolver type.

**RFC822_EMAIL**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.x509.rfc822-email.alternate-principal-attribute` (Optional; type `String`): Attribute name that will be used by X509 principal resolvers if the main attribute in the certificate is not present. This only applies to principal resolvers that are looking for attributes in the certificate that are not common to all certificates. (e.g. `SUBJECT_ALT_NAME`, `CN_EDIPI`) This assumes you would rather get something like the `subjectDn` rather than `null` where `null` would allow falling through to another authentication mechanism. Currently supported values are: `subjectDn`, `sigAlgOid`, `subjectX500Principal`.

**SERIAL_NO**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.x509.serial-no-dn.serial-number-prefix` (Optional; type `String`; default `SERIALNUMBER=`): The serial number prefix used for principal resolution when type is set to `X509Properties.PrincipalTypes#SERIAL_NO_DN`.
- `cas.authn.x509.serial-no-dn.value-delimiter` (Optional; type `String`; default `, `): Value delimiter used for principal resolution when type is set to `X509Properties.PrincipalTypes#SERIAL_NO_DN`.
- `cas.authn.x509.serial-no.principal-hex-s-n-zero-padding` (Optional; type `Boolean`; default `false`): If radix hex padding should be used when `X509Properties#getPrincipalType()` is `X509Properties.PrincipalTypes#SERIAL_NO`.
- `cas.authn.x509.serial-no.principal-s-n-radix` (Optional; type `Integer`; default `0`): Radix used when `X509Properties#getPrincipalType()` is `X509Properties.PrincipalTypes#SERIAL_NO`.

**SERIAL_NO_DN**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.x509.serial-no-dn.serial-number-prefix` (Optional; type `String`; default `SERIALNUMBER=`): The serial number prefix used for principal resolution when type is set to `X509Properties.PrincipalTypes#SERIAL_NO_DN`.
- `cas.authn.x509.serial-no-dn.value-delimiter` (Optional; type `String`; default `, `): Value delimiter used for principal resolution when type is set to `X509Properties.PrincipalTypes#SERIAL_NO_DN`.

**SUBJECT_ALT_NAME**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.x509.subject-alt-name.alternate-principal-attribute` (Optional; type `String`): Attribute name that will be used by X509 principal resolvers if the main attribute in the certificate is not present. This only applies to principal resolvers that are looking for attributes in the certificate that are not common to all certificates. (e.g. `SUBJECT_ALT_NAME`, `CN_EDIPI`) This assumes you would rather get something like the `subjectDn` rather than `null` where `null` would allow falling through to another authentication mechanism. Currently supported values are: `subjectDn`, `sigAlgOid`, `subjectX500Principal`.
