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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

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

    @Autowired
    @Qualifier("shibboleth.OpenSAMLConfig")
    private ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ResourceLoader resourceLoader;

    @ConditionalOnMissingBean(name = "chainingSamlMetadataUIMetadataResolverAdapter")
    @Bean
    public MetadataResolverAdapter chainingSamlMetadataUIMetadataResolverAdapter() {
        return new ChainingMetadataResolverAdapter(CollectionUtils.wrapSet(getStaticMetadataResolverAdapter(), getDynamicMetadataResolverAdapter()));
    }

    private MetadataResolverAdapter configureAdapter(final AbstractMetadataResolverAdapter adapter) {
        val resources = new HashMap<Resource, MetadataFilterChain>();
        val chain = new MetadataFilterChain();
        casProperties.getSamlMetadataUi().getResources().forEach(Unchecked.consumer(r -> configureResource(resources, chain, r)));
        adapter.setRequireValidMetadata(casProperties.getSamlMetadataUi().isRequireValidMetadata());
        adapter.setMetadataResources(resources);
        adapter.setConfigBean(openSamlConfigBean.getObject());
        return adapter;
    }

    private void configureResource(final Map<Resource, MetadataFilterChain> resources,
                                   final MetadataFilterChain chain, final String r) {
        val splitArray = org.springframework.util.StringUtils.commaDelimitedListToStringArray(r);

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
                val sigFilter = SamlUtils.buildSignatureValidationFilter(this.resourceLoader, signingKey);
                if (sigFilter != null) {
                    sigFilter.setRequireSignedRoot(casProperties.getSamlMetadataUi().isRequireSignedRoot());
                    filters.add(sigFilter);
                } else {
                    LOGGER.warn("Failed to locate the signing key [{}] for [{}]", signingKey, metadataFile);
                    addResource = false;
                }
            }
            chain.setFilters(filters);

            val resource = this.resourceLoader.getResource(metadataFile);
            if (addResource && ResourceUtils.doesResourceExist(resource)) {
                resources.put(resource, chain);
            } else {
                LOGGER.warn("Skipping metadata [{}]; Either the resource cannot be retrieved or its signing key is missing", metadataFile);
            }
        }));
    }

    private MetadataResolverAdapter getDynamicMetadataResolverAdapter() {
        val adapter = new DynamicMetadataResolverAdapter();
        configureAdapter(adapter);
        return adapter;
    }

    private MetadataResolverAdapter getStaticMetadataResolverAdapter() {
        val adapter = new StaticMetadataResolverAdapter();
        configureAdapter(adapter);
        adapter.buildMetadataResolverAggregate();
        return adapter;
    }
}
