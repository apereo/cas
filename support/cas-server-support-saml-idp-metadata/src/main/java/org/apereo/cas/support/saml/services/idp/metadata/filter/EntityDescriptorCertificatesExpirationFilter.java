package org.apereo.cas.support.saml.services.idp.metadata.filter;

import module java.base;
import org.apereo.cas.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.metadata.resolver.filter.AbstractMetadataFilter;
import org.opensaml.saml.metadata.resolver.filter.FilterException;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterContext;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.x509.X509Support;
import java.security.cert.CertificateException;

/**
 * This is {@link EntityDescriptorCertificatesExpirationFilter}.
 * Metadata whose root is an entity with an expired or invalid certificate is refused. In an aggregate,
 * every entity (including those of nested groups) with an expired or invalid certificate is removed,
 * and the remaining entities stay available. The filter must run after signature validation,
 * since removing entities changes the signed content.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
public class EntityDescriptorCertificatesExpirationFilter extends AbstractMetadataFilter {

    private static void verifyCertificates(final EntityDescriptor entityDescriptor) throws FilterException {
        for (val role : entityDescriptor.getRoleDescriptors()) {
            for (val kd : role.getKeyDescriptors()) {
                val ki = kd.getKeyInfo();
                for (val x509 : ki.getX509Datas()) {
                    for (val xmlCert : x509.getX509Certificates()) {
                        try {
                            val cert = X509Support.decodeCertificate(xmlCert.getValue());
                            LOGGER.debug("Evaluating certificate [{}] in metadata for [{}]. Not Before [{}], Not After [{}]",
                                cert.getSubjectX500Principal().getName(), entityDescriptor.getEntityID(), cert.getNotBefore(), cert.getNotAfter());
                            cert.checkValidity();
                        } catch (final CertificateException e) {
                            LoggingUtils.error(LOGGER, e);
                            throw new FilterException("Expired or invalid certificate in metadata for " + entityDescriptor.getEntityID(), e);
                        }
                    }
                }
            }
        }
    }

    private static boolean hasValidCertificates(final EntityDescriptor entityDescriptor) {
        try {
            verifyCertificates(entityDescriptor);
            return true;
        } catch (final FilterException e) {
            LOGGER.warn("Removing entity [{}] from metadata: [{}]", entityDescriptor.getEntityID(), e.getMessage());
            return false;
        }
    }

    private static void filterEntitiesDescriptor(final EntitiesDescriptor entitiesDescriptor) {
        val entities = entitiesDescriptor.getEntityDescriptors();
        val refused = entities.stream().filter(Predicate.not(EntityDescriptorCertificatesExpirationFilter::hasValidCertificates)).toList();
        entities.removeAll(refused);
        entitiesDescriptor.getEntitiesDescriptors().forEach(EntityDescriptorCertificatesExpirationFilter::filterEntitiesDescriptor);
    }

    @Override
    public XMLObject filter(@Nullable final XMLObject metadata,
                            @NonNull final MetadataFilterContext context)
            throws FilterException {
        if (metadata instanceof final EntityDescriptor entityDescriptor) {
            verifyCertificates(entityDescriptor);
        } else if (metadata instanceof final EntitiesDescriptor entitiesDescriptor) {
            filterEntitiesDescriptor(entitiesDescriptor);
        }
        return metadata;
    }
}
