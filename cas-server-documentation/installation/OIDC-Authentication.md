---
layout: default
title: CAS - OIDC Authentication
---

# OpenID Connect Authentication

## Configuration
Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-oidc</artifactId>
  <version>${cas.version}</version>
</dependency>
```

## Endpoints

| Field                                     | Description
|-------------------------------------------+------------------------------------------------------+
| `/oidc/.well-known`                       | Discovery endpoint.
| `/oidc/.well-known/openid-configuration`  | Discovery endpoint.
| `/oidc/authorize`                         | Authorization requests are handled here.
| `/oidc/profile`                           | User profile requests are handled here.
| `/oidc/jwks`                              | Provides an aggregate of all keystores
| `/oidc/accessToken`                       | Produces authorized access tokens.

## Register Clients

OpenID Connect clients can be registered with CAS as such:

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId" : "^<https://the-redirect-uri>",
  "signIdToken": true,
  "name": "OIDC",
  "id": 1000,
  "jwks": "..."
}
```

| Field                                | Description
|--------------------------------------+-----------------------------------------------------------------+
| `serviceId`                   | The redirect URI for this OIDC client.
| `signIdToken`                 | Whether ID tokens should be signed.
| `jwks`                        | Path to the location of the keystore that holds the signing keys. If none defined, default below will be used.

## Settings

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).

## Keystores

Each registered application in CAS can contain its own keytore as a `jwks` resource. By default,
a global keystore can be expected and defined via the above property configuration. The format of the keystore
file is similar to the following:

```json
{
    "d": "N70Ylz9rzmTxEXwEbRysF-Us9XoViIYrZtAy_-J_n4hZ804zKjDmQCAkfptwsi_CTLDcfQvLHAp6JTfHVJviBvGbLW_wtETKvYEXd7HO78tqqcEHvZJAaFnMNrCFylGCVJVBPEmiWjnkKzJ-G2C-BRCC_7lguOQ-buuiuzpXHxHsQbrhWehxRnWT9YmjdmRFR9lsKPMLsuoGLVq6d_H8WVwexVmNLgqGH-X-5JYXWljrM_CzL__Jv_nbPpk3Al6lyk0b7jLPcUcI3MckWkDBuiEySg8kh5EJFb3FvzQiFsmY9SEZ3HkR-P-Cm4m9vjzxNgk8yOnzeBzOCrHPBxBdcQ",
    "dp": "AmagGLoRCVs-qU4KLI3kT0GPFdycG3vtjLo47-_J0JXpWJ4qyt9MRGOZmToa0VhJltUfvr8ik94LjeDRXCi_FNc5J6RnmjuvlVmHIRaVIw4ziL9a3uHRITSXEduuWITVIisJ01o6oZwk_3pbXqPlx1WUUZ0kYy5aOaUz09JjJEE",
    "dq": "i_EoRl4_2qIvMSjCg2Cdj13T9yvrrqa05DZZShP9eDe-eHrFYA7BNWVB9IhFD6Q7fr9sJNHm7267_rPhG51mj3az6ryAGmb7e2OHsWRyqfAvSFkdnIUjzCmt8xv0YdqK8iyZmHjB9eNHyzdmkWWBNTgj0_w-YlQWrXKD_HGKgP8",
    "e": "AQAB",
    "kty": "RSA",
    "n": "uLKF_1O7u5BDY-nQOgB7SGxgINAR2CEvxl6dp7EWyAFdhswgugb-y-t0Fq0tZs33TsOr_o3QGzPG8Lm5EtyYJpFBZ6rbvAoIDgddVYW-Agy3c_IKSPnxLKHoI18kaJNpNRbvEFH-V2Ya1VihGGWZnO-dN4iCgGnsG5InzM8GkVoP8NnjL5uhBYxsCo1a9HGd6rziyRaQBUI81t-YbHbP-m7mF315yFLX7uZyoBZSgxjezYo5s1va4uVUXXwVDQuUtvSJ1jV6aNgvZEmCFL7BmJWm0tQxLdKj_nweLNOXfjoDbtKpdDptNVR7t1H0E3whRgdNlpjUGaWU1n2TmmZs9w",
    "p": "_ChowodJnANKElbCtIozV72ttgdfN88xf-gRS8jsdgqjf88SfHq5Trb4Ilb-IhgtlnJJywcbfPky1yCCCrXXoB__iNhONdykhwUenJmHPHJLDlBA6vLga19K2b5xUBK6qry3InvkCVgLVRGUGfSvRkz8Yw-XG2Flvh_IoBaMkh0",
    "q": "u4L4r4UDQUNNlIEBfg5l_VPUVTAcMXv2_esKXD18k0_b0uOy8q6YAaq2v8ZZwzgZQ6-EKje4R8Gg8psE_u4ATt9aMcxlriq60FF_vZIjgmuAkMDku_TtMMs3Ol_PlaO6lHxxOjg3I2dtZfrur1QryplvUpERP79QJVBtiBjdzyM",
    "qi": "x3HwBVFfnR2wa3jKLQxlADcZw0I2Be6tqTrJ9erfWfsznCL6zH6beClyObVF9pdDXujZTUVYyhgHMZ15zRVQ2UgCa9k5x8KJn2y-OFz3m2gWu4UAvfM7UotpdMcZlDiFm8J1BWv8QKYjmFNBAE_gAf7k9YiSxJTAsChCks1Q5gk"
}
```
