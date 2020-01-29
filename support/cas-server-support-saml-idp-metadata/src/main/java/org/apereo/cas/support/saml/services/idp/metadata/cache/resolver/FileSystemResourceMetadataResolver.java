package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.opensaml.core.xml.persist.FilesystemLoadSaveManager;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.LocalDynamicMetadataResolver;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link FileSystemResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class FileSystemResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    public FileSystemResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                              final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        try {
            val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
            LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);
            val metadataResource = ResourceUtils.getResourceFrom(metadataLocation);

            val metadataFile = metadataResource.getFile();
            val metadataResolver = getMetadataResolver(metadataResource, metadataFile);
            configureAndInitializeSingleMetadataResolver(metadataResolver, service);
            return CollectionUtils.wrap(metadataResolver);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    @SneakyThrows
    private AbstractMetadataResolver getMetadataResolver(final AbstractResource metadataResource, final File metadataFile) {
        if (metadataFile.isDirectory()) {
            return new LocalDynamicMetadataResolver(new FilesystemLoadSaveManager<>(metadataFile, configBean.getParserPool()));
        }
        return new InMemoryResourceMetadataResolver(metadataResource, configBean);
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
            val metadataResource = ResourceUtils.getResourceFrom(metadataLocation);
            return metadataResource instanceof FileSystemResource;
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return supports(service);
    }
}
