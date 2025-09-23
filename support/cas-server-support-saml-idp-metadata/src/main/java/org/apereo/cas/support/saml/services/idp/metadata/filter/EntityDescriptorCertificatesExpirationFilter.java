package org.apereo.cas.support.saml.services.idp.metadata.filter;

import org.apereo.cas.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.metadata.resolver.filter.AbstractMetadataFilter;
import org.opensaml.saml.metadata.resolver.filter.FilterException;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterContext;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.x509.X509Support;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.cert.CertificateException;

/**
 * This is {@link EntityDescriptorCertificatesExpirationFilter}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
public class EntityDescriptorCertificatesExpirationFilter extends AbstractMetadataFilter {

    @Override
    public XMLObject filter(
        @Nullable
        final XMLObject metadata,
        @Nonnull
        final MetadataFilterContext context) throws FilterException {

        if (metadata instanceof final EntityDescriptor ed) {
            for (val role : ed.getRoleDescriptors()) {
                for (val kd : role.getKeyDescriptors()) {
                    val ki = kd.getKeyInfo();
                    for (val x509 : ki.getX509Datas()) {
                        for (val xmlCert : x509.getX509Certificates()) {
                            try {
                                val cert = X509Support.decodeCertificate(xmlCert.getValue());
                                LOGGER.debug("Evaluating certificate [{}] in metadata for [{}]. Not Before [{}], Not After [{}]",
                                    cert.getSubjectX500Principal().getName(), ed.getEntityID(), cert.getNotBefore(), cert.getNotAfter());
                                cert.checkValidity();
                            } catch (final CertificateException e) {
                                LoggingUtils.error(LOGGER, e);
                                throw new FilterException("Expired or invalid certificate in metadata for " + ed.getEntityID());
                            }
                        }
                    }
                }
            }
        }
        return metadata;
    }
}

