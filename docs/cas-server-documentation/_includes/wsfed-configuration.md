Allow CAS to act as an identity provider and security token service
to support the WS-Federation protocol:

```properties
# cas.authn.wsfed-idp.idp.realm=urn:org:apereo:cas:ws:idp:realm-CAS
# cas.authn.wsfed-idp.idp.realm-name=CAS

# cas.authn.wsfed-idp.sts.signing-keystore-file=/etc/cas/config/ststrust.jks
# cas.authn.wsfed-idp.sts.signing-keystore-password=storepass
# cas.authn.wsfed-idp.sts.encryption-keystore-file=/etc/cas/config/stsencrypt.jks
# cas.authn.wsfed-idp.sts.encryption-keystore-password=storepass

# cas.authn.wsfed-idp.sts.subject-name-id-format=unspecified
# cas.authn.wsfed-idp.sts.subject-name-qualifier=http://cxf.apache.org/sts
# cas.authn.wsfed-idp.sts.encrypt-tokens=true
# cas.authn.wsfed-idp.sts.sign-tokens=true

# cas.authn.wsfed-idp.sts.conditions-accept-client-lifetime=true
# cas.authn.wsfed-idp.sts.conditions-fail-lifetime-exceedance=false
# cas.authn.wsfed-idp.sts.conditions-future-time-to-live=PT60S
# cas.authn.wsfed-idp.sts.conditions-lifetime=PT30M
# cas.authn.wsfed-idp.sts.conditions-max-lifetime=PT12H

# cas.authn.wsfed-idp.sts.realm.keystore-file=/etc/cas/config/stscasrealm.jks
# cas.authn.wsfed-idp.sts.realm.keystore-password=storepass
# cas.authn.wsfed-idp.sts.realm.keystore-alias=realmcas
# cas.authn.wsfed-idp.sts.realm.key-password=cas
# cas.authn.wsfed-idp.sts.realm.issuer=CAS
```

{% include {{ version }}/signing-encryption-configuration.md configKey="cas.authn.wsfed-idp.sts" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}
