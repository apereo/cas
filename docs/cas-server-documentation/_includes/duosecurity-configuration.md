```properties
# cas.authn.mfa.duo[0].duo-secret-key=
# cas.authn.mfa.duo[0].rank=0
# cas.authn.mfa.duo[0].duo-application-key=
# cas.authn.mfa.duo[0].duo-integration-key=
# cas.authn.mfa.duo[0].duo-api-host=
# cas.authn.mfa.duo[0].trusted-device-enabled=false
# cas.authn.mfa.duo[0].id=mfa-duo
# cas.authn.mfa.duo[0].registration-url=https://registration.example.org/duo-enrollment
# cas.authn.mfa.duo[0].name=
# cas.authn.mfa.duo[0].order=
```

- Web SDK Configuration

The `duo-application-key` is a required string, at least 40 characters long, that you
generate and keep secret from Duo. You can generate a random string in Python with:

```python
import os, hashlib
print hashlib.sha1(os.urandom(32)).hexdigest()
```

- Universal Prompt Configuration

Universal Prompt no longer requires you to generate and use a application 
key value. Instead, it requires a *client id* and *client secret*, which 
are known and taught CAS using the integration key and secret key 
configuration settings. You will need get your integration key, secret key, and API 
hostname from Duo Security when you register CAS as a protected application. 

