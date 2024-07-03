package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apereo.inspektr.audit.annotation.Audit;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link GroovyResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class GroovyResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    public GroovyResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                          final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Audit(action = AuditableActions.SAML2_METADATA_RESOLUTION,
        actionResolverName = AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER)
    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        return FunctionUtils.doAndHandle(() -> {
            val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
            LOGGER.info("Loading SAML metadata via [{}]", metadataLocation);
            val metadataResource = ResourceUtils.getResourceFrom(metadataLocation);
            val args = new Object[]{service, this.configBean, this.samlIdPProperties, criteriaSet, LOGGER};
            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            val metadataResolver = scriptFactory.fromResource(metadataResource).execute(args, MetadataResolver.class, true);
            return CollectionUtils.wrap(metadataResolver);
        }, e -> new ArrayList<MetadataResolver>()).get();
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
        val scriptFactoryInstance = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
        return scriptFactoryInstance.isPresent() && scriptFactoryInstance.get().isExternalScript(metadataLocation);
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        if (supports(service)) {
            val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
            return ResourceUtils.doesResourceExist(metadataLocation);
        }
        return false;
    }
}
