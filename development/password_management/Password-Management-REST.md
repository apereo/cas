# Password Management - REST

Source: https://apereo.github.io/cas/development/password_management/Password-Management-REST.html

Tasks such as locating users’ email and security questions as well as management and updating of the password are delegated to user-defined rest endpoints.

REST support is enabled by including the following dependencies in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-pm-rest"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pm.rest.endpoint-password` (Required; type `String`): Password for Basic-Auth at the password management endpoints.
- `cas.authn.pm.rest.endpoint-url-account-unlock` (Required; type `String`): Endpoint URL to use when unlocking account.
- `cas.authn.pm.rest.endpoint-url-change` (Required; type `String`): Endpoint URL to use when updating passwords..
- `cas.authn.pm.rest.endpoint-url-email` (Required; type `String`): Endpoint URL to use when locating email addresses.
- `cas.authn.pm.rest.endpoint-url-phone` (Required; type `String`): Endpoint URL to use when locating phone numbers.
- `cas.authn.pm.rest.endpoint-url-security-questions` (Required; type `String`): Endpoint URL to use when locating security questions.
- `cas.authn.pm.rest.endpoint-url-user` (Required; type `String`): Endpoint URL to use when locating user names.
- `cas.authn.pm.rest.endpoint-username` (Required; type `String`): Username for Basic-Auth at the password management endpoints.
- `cas.authn.pm.rest.field-name-password` (Required; type `String`; default `password`): Field name for password field when password change requests are submitted.
- `cas.authn.pm.rest.field-name-password-old` (Required; type `String`; default `oldPassword`): Field name for oldPassword field when password change requests are submitted.
- `cas.authn.pm.rest.field-name-user` (Required; type `String`; default `username`): Field name for username field when password change requests are submitted.
- `cas.authn.pm.rest.headers` (Optional; type `Map<String,String>`): Additional headers to be included in REST API calls for password management. The map keys are header names and the corresponding values are header values.

| Endpoint | Method | Query Parameter(s) | Request Body | Expected Response |
| --- | --- | --- | --- | --- |
| Get Email Address | `GET` | `username` | None | `200`. Email address in the body. |
| Get Phone Number | `GET` | `username` | None | `200`. Phone number in the body. |
| Get Security Questions | `GET` | `username` | None | `200`. Security questions map in the body. |
| Update Security Questions | `POST` | `username` | Security questions map in the body. | `200`. `true/false` in the body. |
| Update Password | `POST` | None | `username`, `password`, `oldPassword` | `200`. `true/false` in the body. |
| Unlock Account | `POST` | `username` | None | `200`. `true/false` in the body. |

By default, all requests are submitted using the `Accept` header `application/json`, `Content-Type` header set to `application/json` and `Accept-Charset` header set to `UTF-8`.
