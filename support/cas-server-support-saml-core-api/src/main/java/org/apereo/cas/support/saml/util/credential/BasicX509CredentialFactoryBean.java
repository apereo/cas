package org.apereo.cas.support.saml.util.credential;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.shibboleth.utilities.java.support.collection.LazyList;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.crypto.KeySupport;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Support;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

import java.security.PrivateKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link BasicX509CredentialFactoryBean}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@Setter
public class BasicX509CredentialFactoryBean implements FactoryBean<BasicX509Credential> {
    /**
     * The specification of where the entity Resource is to be found.
     */
    private Resource entityResource;

    /**
     * Where the certificates are to be found.
     */
    private List<Resource> certificateResources;

    /**
     * Where the private key is to be found.
     */
    private Resource privateKeyResource;

    /**
     * Where the crls are to be found.
     */
    private List<Resource> crlResources;

    /**
     * Usage type of the credential.
     */
    private String usageType;

    /**
     * Names for the key represented by the credential.
     */
    private List<String> keyNames;

    /**
     * Identifier for the owner of the credential.
     */
    private String entityID;

    /**
     * The privateKey Password (if any).
     */
    private char[] privateKeyPassword;

    @Override
    public BasicX509Credential getObject() throws Exception {
        val certificates = getCertificates();
        if (certificates.isEmpty()) {
            throw new BeanCreationException("No Certificates provided");
        }

        var entityCertificate = getEntityCertificate();
        if (null == entityCertificate) {
            entityCertificate = certificates.get(0);
        }

        val privateKey = getPrivateKey();
        var credential = (BasicX509Credential) null;
        if (null == privateKey) {
            credential = new BasicX509Credential(entityCertificate);
        } else {
            credential = new BasicX509Credential(entityCertificate, privateKey);

            if (!KeySupport.matchKeyPair(entityCertificate.getPublicKey(), privateKey)) {
                throw new BeanCreationException("Public and private keys do not match");
            }
        }

        credential.setEntityCertificateChain(certificates);

        val crls = getCRLs();
        if (null != crls && !crls.isEmpty()) {
            credential.setCRLs(crls);
        }

        if (null != getUsageType()) {
            credential.setUsageType(UsageType.valueOf(getUsageType()));
        }

        if (null != getEntityID()) {
            credential.setEntityId(getEntityID());
        }

        if (this.keyNames != null) {
            credential.getKeyNames().addAll(this.keyNames);
        }

        return credential;
    }

    @Override
    public Class<?> getObjectType() {
        return BasicX509Credential.class;
    }

    private X509Certificate getEntityCertificate() {
        if (null == entityResource) {
            return null;
        }
        try {
            val certs = X509Support.decodeCertificates(entityResource.getInputStream());
            if (certs.size() > 1) {
                throw new BeanCreationException("Configuration element indicated an entityCertificate,"
                    + " but multiple certificates were decoded");
            }
            return certs.iterator().next();
        } catch (final Exception e) {
            throw new BeanCreationException("Could not decode provided Entity Certificate file "
                + entityResource.getDescription(), e);
        }
    }

    private List<X509Certificate> getCertificates() {
        if (certificateResources == null) {
            return new ArrayList<>();
        }

        val certificates = new LazyList<X509Certificate>();
        for (val r : certificateResources) {
            try (val is = r.getInputStream()) {
                certificates.addAll(X509Support.decodeCertificates(is));
            } catch (final Exception e) {
                throw new BeanCreationException("Could not decode provided CertificateFile: " + r.getDescription(), e);
            }
        }
        return certificates;
    }

    private PrivateKey getPrivateKey() {
        if (null == privateKeyResource) {
            return null;
        }
        try (val is = privateKeyResource.getInputStream()) {
            return KeySupport.decodePrivateKey(is, getPrivateKeyPassword());
        } catch (final Exception e) {
            throw new BeanCreationException("Could not decode provided KeyFile " + privateKeyResource.getDescription(), e);
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private List<X509CRL> getCRLs() {
        if (null == crlResources) {
            return null;
        }
        val crls = new LazyList<X509CRL>();
        for (val crl : crlResources) {
            try (val is = crl.getInputStream()) {
                crls.addAll(X509Support.decodeCRLs(is));
            } catch (final Exception e) {
                throw new BeanCreationException("Could not decode provided CRL file " + crl.getDescription(), e);
            }
        }
        return crls;
    }
}
