package org.apereo.cas.support.saml.util.credential;

import com.google.common.io.ByteStreams;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.cryptacular.util.KeyPairUtil;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.crypto.KeySupport;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * This is {@link BasicResourceCredentialFactoryBean}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@Setter
public class BasicResourceCredentialFactoryBean implements FactoryBean<BasicCredential> {
    /**
     * The SecretKey algorithm.
     */
    private String secretKeyAlgorithm;

    /**
     * The privateKey Password (if any).
     */
    private char[] privateKeyPassword;

    /**
     * Configured public key Info.
     */
    private Resource publicKeyInfo;

    /**
     * Configured private key Info.
     */
    private Resource privateKeyInfo;

    /**
     * Configured secret key Info.
     */
    private Resource secretKeyInfo;

    /**
     * Usage type of the credential.
     */
    private String usageType;

    /**
     * The SecretKey encoding used.
     */
    private SecretKeyEncoding secretKeyEncoding = SecretKeyEncoding.BASE64;

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public Class<?> getObjectType() {
        return BasicCredential.class;
    }

    @Override
    public BasicCredential getObject() throws Exception {
        val privateKey = getPrivateKey();
        val publicKey = getPublicKey();
        val secretKey = getSecretKey();

        var credential = (BasicCredential) null;
        if (null != publicKey) {
            if (null == privateKey) {
                credential = new BasicCredential(publicKey);
            } else {
                if (!KeySupport.matchKeyPair(publicKey, privateKey)) {
                    throw new BeanCreationException("Public and private keys do not match");
                }
                credential = new BasicCredential(publicKey, privateKey);
            }
        } else if (null != secretKey) {
            credential = new BasicCredential(secretKey);
        } else {
            throw new BeanCreationException("Neither public key nor secret key specified");
        }

        if (null != getUsageType()) {
            credential.setUsageType(UsageType.valueOf(getUsageType()));
        }
        return credential;
    }

    protected PublicKey getPublicKey() {
        if (null == getPublicKeyInfo()) {
            return null;
        }
        try (val is = getPublicKeyInfo().getInputStream()) {
            return KeyPairUtil.readPublicKey(is);
        } catch (final Exception e) {
            throw new FatalBeanException("Could not decode public key", e);
        }
    }

    private PrivateKey getPrivateKey() {
        if (null == getPrivateKeyInfo()) {
            return null;
        }
        try (val is = getPrivateKeyInfo().getInputStream()) {
            return KeySupport.decodePrivateKey(is, getPrivateKeyPassword());
        } catch (final Exception e) {
            throw new BeanCreationException("Could not decode private key", e);
        }
    }

    private byte[] decodeSecretKey(final byte[] data) {
        switch (getSecretKeyEncoding()) {
            case BINARY:
                return data;
            case HEX:
                return Hex.decode(data);
            case BASE64:
                return Base64.decodeBase64(data);
            default:
                throw new IllegalArgumentException("Unsupported encoding");

        }
    }

    private SecretKey getSecretKey() {
        if (null == getSecretKeyInfo()) {
            return null;
        }
        try (val is = getSecretKeyInfo().getInputStream()) {
            return KeySupport.decodeSecretKey(decodeSecretKey(ByteStreams.toByteArray(is)), getSecretKeyAlgorithm());
        } catch (final Exception e) {
            throw new BeanCreationException("Could not decode secret key", e);
        }
    }

    /**
     * Form of encoding for SecretKey info.
     */
    enum SecretKeyEncoding {
        /**
         * Raw binary encoding.
         */
        BINARY,
        /**
         * Hexidecimal encoding.
         */
        HEX,
        /**
         * Base64 encoding.
         */
        BASE64
    }
}
