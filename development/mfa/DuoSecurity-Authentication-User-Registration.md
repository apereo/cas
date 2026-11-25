# Duo Security User Registration

Source: https://apereo.github.io/cas/development/mfa/DuoSecurity-Authentication-User-Registration.html

If users are unregistered with Duo Security or allowed through via a direct bypass, CAS will query Duo Security for the user account apriori to learn whether user is registered or configured for direct bypass. If the account is configured for direct bypass or the user account is not registered yet the new-user enrollment policy allows the user to skip registration, CAS will bypass Duo Security altogether and shall not challenge the user and will also **NOT** report back a multifactor-enabled authentication context back to the application.

> **YMMV**
>
> In recent conversations with Duo Security, it turns out that the API behavior has changed (for security reasons) where it may no longer accurately report back account status. This means even if the above conditions hold true, CAS may continue to route the user to Duo Security having received an eligibility status from the API. Duo Security is reportedly working on a fix to restore the API behavior in a more secure way. In the meanwhile, YMMV.

If you would rather not rely on Duo Security’s built-in registration flow and have your own registration application that allows users to onboard and enroll with Duo Security, you can instruct CAS to redirect to your enrollment application, if the user’s account status is determined to require enrollment. This typically means that you must turn on user-account-status checking in CAS so that it can verify the user’s account status directly with Duo Security. You must also make sure your integration type, as selected in Duo Security’s admin dashboard, is chosen to be the correct type that would allow CAS to execute such requests and of course, the user in question must not have been onboard, enrolled or created previously anywhere in Duo Security.

The redirect URL to your enrollment application may include a special `principal` parameter that contains the user’s identity as JWT. Cipher operations and settings must be enabled in CAS settings for Duo Security’s registration before this parameter can be built and added to the final URL.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.duo[0].registration.crypto.encryption.key` (Required; type `String`): The encryption key is a string whose length is defined by the encryption key size setting.
- `cas.authn.mfa.duo[0].registration.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.authn.mfa.duo[0].registration.crypto.alg` (Optional; type `String`; default `A256CBC-HS512`): The signing/encryption algorithm to use.
- `cas.authn.mfa.duo[0].registration.crypto.enabled` (Optional; type `boolean`; default `true`): Whether crypto operations are enabled.
- `cas.authn.mfa.duo[0].registration.crypto.encryption-enabled` (Optional; type `boolean`; default `true`): Whether crypto encryption operations are enabled.
- `cas.authn.mfa.duo[0].registration.crypto.encryption.key-size` (Optional; type `int`; default `512`): The encryption key size.
- `cas.authn.mfa.duo[0].registration.crypto.signing-enabled` (Optional; type `boolean`; default `true`): Whether crypto signing operations are enabled.
- `cas.authn.mfa.duo[0].registration.crypto.signing.key-size` (Optional; type `int`; default `512`): The signing key size.
- `cas.authn.mfa.duo[0].registration.crypto.strategy-type` (Optional; type `String`; default `ENCRYPT_AND_SIGN`): Control the cipher sequence of operations. The accepted values are: - `ENCRYPT_AND_SIGN`: Encrypt the value first, and then sign. - `SIGN_AND_ENCRYPT`: Sign the value first, and then encrypt.
- `cas.authn.mfa.duo[0].registration.registration-url` (Optional; type `String`): Link to a registration app, typically developed in-house in order to allow new users to sign-up for duo functionality. If the user account status requires enrollment and this link is specified, CAS will redirect the authentication flow to this registration app. Otherwise, the default duo mechanism for new-user registrations shall take over. Upon redirecting to the registration app, CAS would also build a `principal` parameter into the registration URL, typically in form of a JSON web token that conveys the user's identity. This JWT can be signed and/or encrypted using settings defined via the `#getCrypto()` configuration block here.

**Signing & encryption**

This CAS feature is able to accept signing and encryption crypto keys. In most scenarios if keys are not provided, CAS will auto-generate them. The following instructions apply if you wish to manually and beforehand create the signing and encryption keys.

Note that if you are asked to create a [JWK](https://tools.ietf.org/html/rfc7517) of a certain size for the key, you are to use the following set of commands to generate the token:

```bash
wget https://raw.githubusercontent.com/apereo/cas/master/etc/jwk-gen.jar
java -jar jwk-gen.jar -t oct -s [size]
```

The outcome would be similar to:

```json
{
  "kty": "oct",
  "kid": "...",
  "k": "..."
}
```

The generated value for `k` needs to be assigned to the relevant CAS settings. Note that keys generated via the above algorithm are processed by CAS using the Advanced Encryption Standard (`AES`) algorithm which is a specification for the encryption of electronic data established by the U.S. National Institute of Standards and Technology.

---
