Allow CAS to become an OpenID Connect provider (OP).

```properties
# cas.authn.oidc.issuer=http://localhost:8080/cas/oidc
# cas.authn.oidc.skew=5

# cas.authn.oidc.dynamic-client-registration-mode=OPEN|PROTECTED

# cas.authn.oidc.subject-types=public,pairwise
# cas.authn.oidc.scopes=openid,profile,email,address,phone,offline_access
# cas.authn.oidc.claims=sub,name,preferred_username,family_name, \
#    given_name,middle_name,given_name,profile, \
#    picture,nickname,website,zoneinfo,locale,updated_at,birthdate, \
#    email,email_verified,phone_number,phone_number_verified,address

# cas.authn.oidc.response-types-supported=code,token,id_token token
# cas.authn.oidc.introspection-supported-authentication-methods=client_secret_basic
# cas.authn.oidc.claim-types-supported=normal
# cas.authn.oidc.grant-types-supported=authorization_code,password,client_credentials,refresh_token
# cas.authn.oidc.token-endpoint-auth-methods-supported=client_secret_basic,client_secret_post,private_key_jwt,client_secret_jwt
# cas.authn.oidc.code-challenge-methods-supported=plain,S256

# cas.authn.oidc.id-token-signing-alg-values-supported=none,RS256,RS384,RS512,PS256,PS384,PS512,ES256,ES384,ES512,HS256,HS384,HS512
# cas.authn.oidc.id-token-encryption-alg-values-supported=RSA1_5,RSA-OAEP,RSA-OAEP-256,A128KW,A192KW,A256KW,\
    A128GCMKW,A192GCMKW,A256GCMKW,ECDH-ES,ECDH-ES+A128KW,ECDH-ES+A192KW,ECDH-ES+A256KW
# cas.authn.oidc.id-token-encryption-encoding-values-supported=A128CBC-HS256,A192CBC-HS384,A256CBC-HS512,A128GCM,A192GCM,A256GCM

# cas.authn.oidc.user-info-signing-alg-values-supported=none,RS256,RS384,RS512,PS256,PS384,PS512,ES256,ES384,ES512,HS256,HS384,HS512
# cas.authn.oidc.user-info-encryption-alg-values-supported=RSA1_5,RSA-OAEP,RSA-OAEP-256,A128KW,A192KW,A256KW,\
    A128GCMKW,A192GCMKW,A256GCMKW,ECDH-ES,ECDH-ES+A128KW,ECDH-ES+A192KW,ECDH-ES+A256KW
# cas.authn.oidc.user-info-encryption-encoding-values-supported=A128CBC-HS256,A192CBC-HS384,A256CBC-HS512,A128GCM,A192GCM,A256GCM
```
