package org.apereo.cas.support.saml.mdui.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.mdui.AbstractMetadataResolverAdapter;
import org.apereo.cas.support.saml.mdui.ChainingMetadataResolverAdapter;
import org.apereo.cas.support.saml.mdui.DynamicMetadataResolverAdapter;
import org.apereo.cas.support.saml.mdui.MetadataResolverAdapter;
import org.apereo.cas.support.saml.mdui.StaticMetadataResolverAdapter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.jooq.lambda.Unchecked;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.metadata.resolver.filter.impl.RequiredValidUntilFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.SignatureValidationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link SamlMetadataUIConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("samlMetadataUIConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SamlMetadataUIConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlMetadataUIConfiguration.class);

    private static final String DEFAULT_SEPARATOR = "::";

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private OpenSamlConfigBean openSamlConfigBean;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired(required = false)
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired(required = false)
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> serviceFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @ConditionalOnMissingBean(name = "chainingSamlMetadataUIMetadataResolverAdapter")
    @Bean
    public MetadataResolverAdapter chainingSamlMetadataUIMetadataResolverAdapter() {
        return new ChainingMetadataResolverAdapter(CollectionUtils.wrapList(getStaticMetadataResolverAdapter(), getDynamicMetadataResolverAdapter()));
    }

    private MetadataResolverAdapter configureAdapter(final AbstractMetadataResolverAdapter adapter) {
        final Map<Resource, MetadataFilterChain> resources = new HashMap<>();
        final MetadataFilterChain chain = new MetadataFilterChain();
        casProperties.getSamlMetadataUi().getResources().forEach(Unchecked.consumer(r -> configureResource(resources, chain, r)));
        adapter.setRequireValidMetadata(casProperties.getSamlMetadataUi().isRequireValidMetadata());
        adapter.setMetadataResources(resources);
        adapter.setConfigBean(openSamlConfigBean);
        return adapter;
    }

    private void configureResource(final Map<Resource, MetadataFilterChain> resources,
                                   final MetadataFilterChain chain, final String r) {
        final String[] splitArray = org.springframework.util.StringUtils.commaDelimitedListToStringArray(r);

        Arrays.stream(splitArray).forEach(Unchecked.consumer(entry -> {
            final String[] arr = entry.split(DEFAULT_SEPARATOR);
            final String metadataFile = arr[0];
            final String signingKey = arr.length > 1 ? arr[1] : null;

            final List<MetadataFilter> filters = new ArrayList<>();
            if (casProperties.getSamlMetadataUi().getMaxValidity() > 0) {
                filters.add(new RequiredValidUntilFilter(casProperties.getSamlMetadataUi().getMaxValidity()));
            }

            boolean addResource = true;
            if (StringUtils.isNotBlank(signingKey)) {
                final SignatureValidationFilter sigFilter = SamlUtils.buildSignatureValidationFilter(this.resourceLoader, signingKey);
                if (sigFilter != null) {
                    sigFilter.setRequireSignedRoot(casProperties.getSamlMetadataUi().isRequireSignedRoot());
                    filters.add(sigFilter);
                } else {
                    LOGGER.warn("Failed to locate the signing key [{}] for [{}]", signingKey, metadataFile);
                    addResource = false;
                }
            }
            chain.setFilters(filters);

            final Resource resource = this.resourceLoader.getResource(metadataFile);
            if (addResource && ResourceUtils.doesResourceExist(resource)) {
                resources.put(resource, chain);
            } else {
                LOGGER.warn("Skipping metadata [{}]; Either the resource cannot be retrieved or its signing key is missing", metadataFile);
            }
        }));
    }

    private MetadataResolverAdapter getDynamicMetadataResolverAdapter() {
        final DynamicMetadataResolverAdapter adapter = new DynamicMetadataResolverAdapter();
        configureAdapter(adapter);
        return adapter;
    }

    private MetadataResolverAdapter getStaticMetadataResolverAdapter() {
        final StaticMetadataResolverAdapter adapter = new StaticMetadataResolverAdapter();
        configureAdapter(adapter);
        adapter.buildMetadataResolverAggregate();
        return adapter;
    }
}
