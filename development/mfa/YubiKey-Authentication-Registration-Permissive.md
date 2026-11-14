# Permissive YubiKey Registration

Source: https://apereo.github.io/cas/development/mfa/YubiKey-Authentication-Registration-Permissive.html

Registration records may be specified statically via CAS settings in form of a map that links registered usernames with the public id of the YubiKey device.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.yubikey.allowed-devices` (Optional; type `Map<String,String>`): Collection of allowed devices allowed per user. This is done using a key-value structure where the key is the user the value is the allowed collection of yubikey device ids.
