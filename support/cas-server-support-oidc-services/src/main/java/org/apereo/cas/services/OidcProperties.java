package org.apereo.cas.services;

public interface OidcProperties {

    enum Scopes {
      PROFILE("Profile", "profile"),
      EMAIL("Email", "email"),
      ADDRESS("Address", "address"),
      PHONE("Phone", "phone"),
      USER_DEFINED("User Defined", "user_defined");

      private final String display;
      private final String value;

      Scopes(final String display, final String value) {
          this.display = display;
          this.value = value;
      }
    }

    enum EncryptAlgOptions {
        RSA1_5,
        RSA_OAEP,
        RSA_OAEP_256,
        ECDH_ES,
        ECDH_ES_PLUS_A128KW,
        ECDH_ES_PLUS_A192KW,
        ECDH_ES_PLUS_A256KW,
        A128KW,
        A192KW,
        A256KW,
        A128GCMKW,
        A192GXMKW,
        A256GCMKW,
        PBES2_HS256_PLUS_A128KW,
        PBES2_HS384_PLUS_A192KW,
        PBES2_HS512_PLUS_A256KW
    }

    enum EncodingAlgOptions {
        A128CBC_HS256,
        A192CBC_HS384,
        A256CBC_HS512,
        A128GCM,
        A192GCM,
        A256GCM
    }
}
