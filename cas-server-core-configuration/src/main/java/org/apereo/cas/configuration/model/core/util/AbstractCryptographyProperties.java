package org.apereo.cas.configuration.model.core.util;


/**
 * Common properties for all cryptography related configs.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public abstract class AbstractCryptographyProperties {

    private Encryption encryption = new Encryption();

    private Signing signing = new Signing();

    private Secretkey secretkey = new Secretkey();

    public Encryption getEncryption() {
        return encryption;
    }

    public void setEncryption(final Encryption encryption) {
        this.encryption = encryption;
    }

    public Signing getSigning() {
        return signing;
    }

    public void setSigning(final Signing signing) {
        this.signing = signing;
    }

    public Secretkey getSecretkey() {
        return secretkey;
    }

    public void setSecretkey(final Secretkey secretkey) {
        this.secretkey = secretkey;
    }

    /**
     * Encryption.
     */
    public static class Encryption {
        private String key = "";
        private int keySize = 16;

        public String getKey() {
            return key;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public int getKeySize() {
            return keySize;
        }

        public void setKeySize(final int keySize) {
            this.keySize = keySize;
        }
    }

    /**
     * Signing.
     */
    public static class Signing {
        private String key = "";
        private int keySize = 512;

        public String getKey() {
            return key;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public int getKeySize() {
            return keySize;
        }

        public void setKeySize(final int keySize) {
            this.keySize = keySize;
        }
    }

    /**
     * Secretkey.
     */
    public static class Secretkey {
        private String alg = "AES";

        public String getAlg() {
            return alg;
        }

        public void setAlg(final String alg) {
            this.alg = alg;
        }
    }
}
