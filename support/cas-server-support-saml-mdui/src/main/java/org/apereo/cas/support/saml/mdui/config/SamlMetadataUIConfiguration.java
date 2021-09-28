package org.apereo.cas.support.saml.mdui.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.mdui.AbstractMetadataResolverAdapter;
import org.apereo.cas.support.saml.mdui.ChainingMetadataResolverAdapter;
import org.apereo.cas.support.saml.mdui.DynamicMetadataResolverAdapter;
import org.apereo.cas.support.saml.mdui.MetadataResolverAdapter;
import org.apereo.cas.support.saml.mdui.StaticMetadataResolverAdapter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.metadata.resolver.filter.impl.RequiredValidUntilFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link SamlMetadataUIConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "samlMetadataUIConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SamlMetadataUIConfiguration {

    private static final String DEFAULT_SEPARATOR = "::";

    private static MetadataResolverAdapter configureAdapter(final AbstractMetadataResolverAdapter adapter,
                                                            final ConfigurableApplicationContext applicationContext,
                                                            final CasConfigurationProperties casProperties,
                                                            final OpenSamlConfigBean openSamlConfigBean) {
        val resources = new HashMap<Resource, MetadataFilterChain>();
        val chain = new MetadataFilterChain();
        casProperties.getSamlMetadataUi().getResources()
            .forEach(Unchecked.consumer(r -> configureResource(applicationContext, resources, chain, r, casProperties)));
        adapter.setRequireValidMetadata(casProperties.getSamlMetadataUi().isRequireValidMetadata());
        adapter.setMetadataResources(resources);
        adapter.setConfigBean(openSamlConfigBean);
        return adapter;
    }

    private static void configureResource(
        final ConfigurableApplicationContext applicationContext,
        final Map<Resource, MetadataFilterChain> resources,
        final MetadataFilterChain chain,
        final String resourceArray,
        final CasConfigurationProperties casProperties) {
        val splitArray = org.springframework.util.StringUtils.commaDelimitedListToStringArray(resourceArray);
        Arrays.stream(splitArray).forEach(Unchecked.consumer(entry -> {
            val arr = Splitter.on(DEFAULT_SEPARATOR).splitToList(entry);
            val metadataFile = arr.get(0);
            val signingKey = arr.size() > 1 ? arr.get(1) : null;
            val filters = new ArrayList<MetadataFilter>();
            if (casProperties.getSamlMetadataUi().getMaxValidity() > 0) {
                val filter = new RequiredValidUntilFilter();
                filter.setMaxValidityInterval(Duration.ofSeconds(casProperties.getSamlMetadataUi().getMaxValidity()));
                filters.add(filter);
            }
            var addResource = true;
            if (StringUtils.isNotBlank(signingKey)) {
                val sigFilter = SamlUtils.buildSignatureValidationFilter(applicationContext, signingKey);
                if (sigFilter != null) {
                    sigFilter.setRequireSignedRoot(casProperties.getSamlMetadataUi().isRequireSignedRoot());
                    filters.add(sigFilter);
                } else {
                    LOGGER.warn("Failed to locate the signing key [{}] for [{}]", signingKey, metadataFile);
                    addResource = false;
                }
            }
            chain.setFilters(filters);
            val resource = applicationContext.getResource(metadataFile);
            if (addResource && ResourceUtils.doesResourceExist(resource)) {
                resources.put(resource, chain);
            } else {
                LOGGER.warn("Skipping metadata [{}]; Either the resource cannot be retrieved or its signing key is missing", metadataFile);
            }
        }));
    }

    @ConditionalOnMissingBean(name = "chainingSamlMetadataUIMetadataResolverAdapter")
    @Bean
    @Autowired
    public MetadataResolverAdapter chainingSamlMetadataUIMetadataResolverAdapter(final CasConfigurationProperties casProperties,
                                                                                 final ConfigurableApplicationContext applicationContext,
                                                                                 @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
                                                                                 final OpenSamlConfigBean openSamlConfigBean) {
        val staticAdapter = new StaticMetadataResolverAdapter();
        configureAdapter(staticAdapter, applicationContext, casProperties, openSamlConfigBean);
        staticAdapter.buildMetadataResolverAggregate();

        val dynaAdapter = new DynamicMetadataResolverAdapter();
        configureAdapter(dynaAdapter, applicationContext, casProperties, openSamlConfigBean);
        return new ChainingMetadataResolverAdapter(CollectionUtils.wrapSet(staticAdapter, dynaAdapter));
    }
}
