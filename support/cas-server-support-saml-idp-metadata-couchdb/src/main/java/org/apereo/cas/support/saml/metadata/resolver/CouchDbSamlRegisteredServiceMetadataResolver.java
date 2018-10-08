package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.couchdb.saml.CouchDbSamlMetadataDocument;
import org.apereo.cas.couchdb.saml.SamlMetadataDocumentCouchDbRepository;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ektorp.DocumentNotFoundException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link CouchDbSamlRegisteredServiceMetadataResolver}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Slf4j
public class CouchDbSamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {

    private SamlMetadataDocumentCouchDbRepository couchDb;

    public CouchDbSamlRegisteredServiceMetadataResolver(final SamlIdPProperties idp, final OpenSamlConfigBean openSamlConfigBean,
                                                        final SamlMetadataDocumentCouchDbRepository couchDb) {
        super(idp, openSamlConfigBean);
        this.couchDb = couchDb;
    }

    @Override
    public Collection<MetadataResolver> resolve(final SamlRegisteredService service) {
        try {
            return couchDb.getAll().stream().map(doc -> buildMetadataResolverFrom(service, doc)).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (final DocumentNotFoundException e) {
            LOGGER.debug(e.getMessage());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            val metadataLocation = service.getMetadataLocation();
            return metadataLocation.trim().startsWith("couchdb://");
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public void saveOrUpdate(final SamlMetadataDocument document) {
        val coudbDbDocument = couchDb.findFirstByName(document.getName());
        if (coudbDbDocument == null) {
            couchDb.add(new CouchDbSamlMetadataDocument(document));
        } else {
            couchDb.update(coudbDbDocument.merge(document));
        }
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return supports(service);
    }
}
