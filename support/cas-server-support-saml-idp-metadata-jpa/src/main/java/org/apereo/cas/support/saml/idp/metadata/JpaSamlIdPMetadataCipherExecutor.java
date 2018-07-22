package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.util.cipher.BaseStringCipherExecutor;

/**
 * This is {@link JpaSamlIdPMetadataCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class JpaSamlIdPMetadataCipherExecutor extends BaseStringCipherExecutor {

    public JpaSamlIdPMetadataCipherExecutor(final String secretKeyEncryption, final String secretKeySigning, final String alg) {
        super(secretKeyEncryption, secretKeySigning, alg);
    }

    public JpaSamlIdPMetadataCipherExecutor(final String secretKeyEncryption, final String secretKeySigning) {
        super(secretKeyEncryption, secretKeySigning);
    }

    @Override
    public String getName() {
        return "JPA Saml IdP Metadata";
    }

    @Override
    protected String getEncryptionKeySetting() {
        return "cas.authn.samlIdp.metadata.jpa.crypto.encryption.key";
    }

    @Override
    protected String getSigningKeySetting() {
        return "cas.authn.samlIdp.metadata.jpa.crypto.signing.key";
    }
}
