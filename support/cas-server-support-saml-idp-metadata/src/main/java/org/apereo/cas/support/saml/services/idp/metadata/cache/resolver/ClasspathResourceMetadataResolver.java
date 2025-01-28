package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apereo.inspektr.audit.annotation.Audit;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link ClasspathResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class ClasspathResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    public ClasspathResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                             final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Audit(action = AuditableActions.SAML2_METADATA_RESOLUTION,
        actionResolverName = AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER)
    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
        LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);
        try (val in = ResourceUtils.getResourceFrom(metadataLocation).getInputStream()) {
            LOGGER.debug("Parsing metadata from [{}]", metadataLocation);
            val document = this.configBean.getParserPool().parse(in);

            val metadataRoot = document.getDocumentElement();
            val metadataProvider = new DOMMetadataResolver(metadataRoot);
            metadataProvider.setResolveViaPredicatesOnly(true);
            configureAndInitializeSingleMetadataResolver(metadataProvider, service);
            return CollectionUtils.wrap(metadataProvider);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new ArrayList<>();
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
            val metadataResource = ResourceUtils.getResourceFrom(metadataLocation);
            return metadataResource instanceof ClassPathResource;
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
