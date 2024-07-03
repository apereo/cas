package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.opensaml.core.xml.persist.FilesystemLoadSaveManager;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.DefaultLocalDynamicSourceKeyGenerator;
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

    @Audit(action = AuditableActions.SAML2_METADATA_RESOLUTION,
        actionResolverName = AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER)
    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        val listOfResolvers = new ArrayList<MetadataResolver>();
        try {
            val metadataLocations = org.springframework.util.StringUtils.commaDelimitedListToSet(
                SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation())
            );
            for (val metadataLocation : metadataLocations) {
                if (isMetadataFileSystemResource(metadataLocation)) {
                    LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);
                    val metadataResource = ResourceUtils.getResourceFrom(metadataLocation);
                    val metadataFile = metadataResource.getFile();
                    val metadataResolver = getMetadataResolver(metadataResource, metadataFile);
                    configureAndInitializeSingleMetadataResolver(metadataResolver, service);
                    listOfResolvers.add(metadataResolver);
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return listOfResolvers;
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        return FunctionUtils.doAndHandle(() -> {
            val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
            val metadataLocations = org.springframework.util.StringUtils.commaDelimitedListToSet(metadataLocation);
            return metadataLocations.stream().anyMatch(FileSystemResourceMetadataResolver::isMetadataFileSystemResource);
        }, throwable -> false).get();
    }

    private static boolean isMetadataFileSystemResource(final String location) {
        val metadataResource = FunctionUtils.doUnchecked(() -> ResourceUtils.isUrl(location) ? null : ResourceUtils.getResourceFrom(location));
        val scriptFactory = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
        return metadataResource instanceof FileSystemResource
            && (scriptFactory.isEmpty() || !scriptFactory.get().isScript(location));
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return supports(service);
    }

    private AbstractMetadataResolver getMetadataResolver(final AbstractResource metadataResource,
                                                         final File metadataFile) throws Exception {
        if (metadataFile.isDirectory()) {
            val sourceStrategy = new DefaultLocalDynamicSourceKeyGenerator(StringUtils.EMPTY, ".xml", StringUtils.EMPTY);
            val manager = new FilesystemLoadSaveManager<>(metadataFile, configBean.getParserPool());
            return new LocalDynamicMetadataResolver(manager, sourceStrategy);
        }
        return new InMemoryResourceMetadataResolver(metadataResource, configBean);
    }
}
