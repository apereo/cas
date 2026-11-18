# JSON YubiKey Registration

Source: https://apereo.github.io/cas/development/mfa/YubiKey-Authentication-Registration-JSON.html

Registration records may be tracked inside a JSON file, provided the file path is specified in CAS settings.

The JSON structure is a map of user id to yubikey public id representing any particular device:

```json
{
  "uid1": ["yubikeyPublicId1"],
  "uid2": ["yubikeyPublicId2"]
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.yubikey.json` (Optional; type `YubiKeyJsonMultifactorProperties`): Keep device registration records inside a static JSON resource.
