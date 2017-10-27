package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Interface used to group and provide user selection options for OIDC service fields.
 *
 * @author Travis Schmidt
 * @since 5.2.0
 */
public interface OidcProperties {

    /**
     * Provides a list of Scopes that can be returned with an OIDC Token.
     */
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    enum Scopes {
        /**
         * Used to select profile scope.
         */
        PROFILE("Profile", "profile"),
        /**
         * Used to select email scope.
         */
        EMAIL("Email", "email"),
        /**
         * Used to select address scope.
         */
        ADDRESS("Address", "address"),
        /**
         * Used to select phone scope.
         */
        PHONE("Phone", "phone"),
        /**
         * USed to select user_defined scopes.
         */
        USER_DEFINED("User Defined", "user_defined");

        /**
         * Display for the property.
         */
        private final String display;

        /**
         * Value of the property.
         */
        private final String value;

        Scopes(final String display, final String value) {
            this.display = display;
            this.value = value;
        }

        /**
         * Returns the display string for this property.
         *
         * @return - String to display
         */
        public String getDisplay() {
            return this.display;
        }

        /**
         * Returns the value to be stored for this property.
         *
         * @return - String value of the property
         */
        public String getValue() {
            return this.value;
        }
    }

    /**
     * Provides a list of default encryptions algorithms.
     */
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    enum EncryptAlgOptions {
        /**
         * RSA1-5.
         */
        RSA1_5("RSA-5"),
        /**
         * RSA-OAEP.
         */
        RSA_OAEP("RSA-OAEP"),
        /**
         * RSA-OAEP-256.
         */
        RSA_OAEP_256("RSA-OAEP-256"),
        /**
         * ECDH-ES.
         */
        ECDH_ES("ECDH-ES"),
        /**
         * ECDH-ES+A128KW.
         */
        ECDH_ES_PLUS_A128KW("ECDH-ES+A128KW"),
        /**
         * ECDH-ES+A192KW.
         */
        ECDH_ES_PLUS_A192KW("ECDH-ES+A192KW"),
        /**
         * ECDH-ES+A256KW.
         */
        ECDH_ES_PLUS_A256KW("ECDH-ES+A256KW"),
        /**
         * A128KW.
         */
        A128KW("A128KW"),
        /**
         * A192KW.
         */
        A192KW("A192KW"),
        /**
         * A256KW.
         */
        A256KW("A256KW"),
        /**
         * A128GCMKW.
         */
        A128GCMKW("A128GCMKW"),
        /**
         * A192GXMKW.
         */
        A192GXMKW("A192GXMKW"),
        /**
         * A256GCMKW.
         */
        A256GCMKW("A256GCMKW"),
        /**
         * PBES2-HS256+A128KW.
         */
        PBES2_HS256_PLUS_A128KW("PBES2-HS256+A128KW"),
        /**
         * PBES2-HS384+A192KW.
         */
        PBES2_HS384_PLUS_A192KW("PBES2-HS384+A192KW"),
        /**
         * PBES2-HS512+A256KW.
         */
        PBES2_HS512_PLUS_A256KW("PBES2-HS512+A256KW");

        /**
         * Display for the property.
         */
        private final String display;

        EncryptAlgOptions(final String display) {
            this.display = display;
        }

        /**
         * Returns the display string for this property.
         *
         * @return - String to display
         */
        public String getDisplay() {
            return this.display;
        }
    }

    /**
     * Provides a list of default encoding options.
     */
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    enum EncodingAlgOptions {
        /**
         * A128CBC-HS256.
         */
        A128CBC_HS256("A128CBC-HS256"),
        /**
         * A192CBC-HS384.
         */
        A192CBC_HS384("A192CBC-HS384"),
        /**
         * A256CBC-HS512.
         */
        A256CBC_HS512("A256CBC_HS512"),
        /**
         * A128GCM.
         */
        A128GCM("A128GCM"),
        /**
         * A192GCM.
         */
        A192GCM("A192GCM"),
        /**
         * A256GCM.
         */
        A256GCM("A256GCM");

        /**
         * Display for the property.
         */
        private final String display;

        EncodingAlgOptions(final String display) {
            this.display = display;
        }

        /**
         * Returns the display string for this property.
         *
         * @return - String to display
         */
        public String getDisplay() {
            return this.display;
        }
    }
}
