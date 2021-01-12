---
layout: default
title: CAS - YubiKey Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# JSON YubiKey Registration

Registration records may be tracked inside a JSON file, provided the file path is specified in CAS settings. 

The JSON structure is a map of user id to yubikey public id representing any particular device:

```json
{
  "uid1": ["yubikeyPublicId1"],
  "uid2": ["yubikeyPublicId2"]
}
```

{% include casproperties.html properties="cas.authn.mfa.yubikey.json" %}
