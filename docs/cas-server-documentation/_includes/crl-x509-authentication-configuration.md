
CAS provides a flexible policy engine for certificate revocation checking. This facility arose due to lack of configurability
in the revocation machinery built into the JSSE.

Available policies cover the following events:

- CRL Expiration
- CRL Unavailability

In either event, the following options are available:

| Type                    | Description
|-------------------------|----------------------------------------------------------------------------------------------------
| `ALLOW`                 | Allow authentication to proceed.
| `DENY`                  | Deny authentication and block.
| `THRESHOLD`             | Applicable to CRL expiration, throttle the request whereby expired data is permitted up to a threshold period of time but not afterward.


Revocation certificate checking can be carried out in one of the following ways:

| Type                    | Description
|-------------------------|----------------------------------------------------------------------------------------------------
| `NONE`                  | No revocation is performed.
| `CRL`                   | The CRL URI(s) mentioned in the certificate `cRLDistributionPoints` extension field. Caches are available to prevent excessive IO against CRL endpoints; CRL data is fetched if does not exist in the cache or if it is expired.
| `RESOURCE`              | A CRL hosted at a fixed location. The CRL is fetched at periodic intervals and cached.


To fetch CRLs, the following options are available:

| Type                    | Description
|-------------------------|----------------------------------------------------------------------------------------------------
| `RESOURCE`              | By default, all revocation checks use fixed resources to fetch the CRL resource from the specified location.
| `LDAP`                  | A CRL resource may be fetched from a pre-configured attribute, in the event that the CRL resource location is an LDAP URI

```properties
# cas.authn.x509.crl-expired-policy=DENY|ALLOW|THRESHOLD
# cas.authn.x509.crl-unavailable-policy=DENY|ALLOW|THRESHOLD
# cas.authn.x509.crl-resource-expired-policy=DENY|ALLOW|THRESHOLD
# cas.authn.x509.crl-resource-unavailable-policy=DENY|ALLOW|THRESHOLD

# cas.authn.x509.revocation-checker=NONE|CRL|RESOURCE
# cas.authn.x509.crl-fetcher=RESOURCE|LDAP

# cas.authn.x509.crl-resources[0]=file:/...

# cas.authn.x509.cache-max-elements-in-memory=1000
# cas.authn.x509.cache-disk-overflow=false
# cas.authn.x509.cache-disk-size=100MB
# cas.authn.x509.cache-eternal=false
# cas.authn.x509.cache-time-to-live-seconds=7200
```  
