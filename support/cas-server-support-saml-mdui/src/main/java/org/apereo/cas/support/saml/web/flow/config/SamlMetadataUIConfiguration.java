package org.apereo.cas.support.saml.web.flow.config;

import com.google.common.collect.ImmutableList;
import net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl.BasicResourceCredentialFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.web.flow.SamlMetadataUIParserAction;
import org.apereo.cas.support.saml.web.flow.SamlMetadataUIWebflowConfigurer;
import org.apereo.cas.support.saml.web.flow.mdui.AbstractMetadataResolverAdapter;
import org.apereo.cas.support.saml.web.flow.mdui.ChainingMetadataResolverAdapter;
import org.apereo.cas.support.saml.web.flow.mdui.DynamicMetadataResolverAdapter;
import org.apereo.cas.support.saml.web.flow.mdui.MetadataResolverAdapter;
import org.apereo.cas.support.saml.web.flow.mdui.StaticMetadataResolverAdapter;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.jooq.lambda.Unchecked;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilter;
import org.opensaml.saml.metadata.resolver.filter.MetadataFilterChain;
import org.opensaml.saml.metadata.resolver.filter.impl.RequiredValidUntilFilter;
import org.opensaml.saml.metadata.resolver.filter.impl.SignatureValidationFilter;
import org.opensaml.security.credential.impl.StaticCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.BasicProviderKeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.provider.DEREncodedKeyValueProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.DSAKeyValueProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.InlineX509DataProvider;
import org.opensaml.xmlsec.keyinfo.impl.provider.RSAKeyValueProvider;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

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

    @javax.annotation.Resource(name = "webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> serviceFactory;

    @ConditionalOnMissingBean(name = "samlMetadataUIWebConfigurer")
    @Bean
    public CasWebflowConfigurer samlMetadataUIWebConfigurer() {
        final SamlMetadataUIWebflowConfigurer w = new SamlMetadataUIWebflowConfigurer();
        w.setSamlMetadataUIParserAction(samlMetadataUIParserAction());
        w.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
        w.setFlowBuilderServices(flowBuilderServices);
        return w;
    }

    @ConditionalOnMissingBean(name = "samlMetadataUIParserAction")
    @Bean
    public Action samlMetadataUIParserAction() {
        final String parameter = StringUtils.defaultIfEmpty(casProperties.getSamlMetadataUi().getParameter(),
                SamlProtocolConstants.PARAMETER_ENTITY_ID);
        final SamlMetadataUIParserAction a = new SamlMetadataUIParserAction(parameter, metadataAdapter());

        a.setServiceFactory(this.serviceFactory);
        a.setServicesManager(this.servicesManager);
        return a;
    }

    @ConditionalOnMissingBean(name = "metadataAdapter")
    @Bean
    public MetadataResolverAdapter metadataAdapter() {
        final ChainingMetadataResolverAdapter adapter = new ChainingMetadataResolverAdapter();
        adapter.getAdapters().add(getStaticMetadataResolverAdapter());
        adapter.getAdapters().add(getDynamicMetadataResolverAdapter());
        return adapter;
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
                                   final MetadataFilterChain chain,
                                   final String r) throws Exception {
        final String[] splitArray = org.springframework.util.StringUtils.commaDelimitedListToStringArray(r);

        Arrays.stream(splitArray).forEach(Unchecked.consumer(entry -> {
            final String[] arr = entry.split(DEFAULT_SEPARATOR);

            final String metadataFile = arr[0];
            final String signingKey = arr.length > 1 ? arr[1] : null;

            final List<MetadataFilter> filters = new ArrayList<>();
            if (casProperties.getSamlMetadataUi().getMaxValidity() > 0) {
                filters.add(new RequiredValidUntilFilter(casProperties.getSamlMetadataUi().getMaxValidity()));
            }

            if (StringUtils.isNotEmpty(signingKey)) {
                final BasicResourceCredentialFactoryBean credential = new BasicResourceCredentialFactoryBean();
                credential.setPublicKeyInfo(this.resourceLoader.getResource(signingKey));
                credential.afterPropertiesSet();
                final StaticCredentialResolver credentialResolver =
                        new StaticCredentialResolver(credential.getObject());

                final BasicProviderKeyInfoCredentialResolver keyInfoResolver =
                        new BasicProviderKeyInfoCredentialResolver(
                                ImmutableList.of(
                                        new RSAKeyValueProvider(),
                                        new DSAKeyValueProvider(),
                                        new DEREncodedKeyValueProvider(),
                                        new InlineX509DataProvider()
                                )
                        );
                final ExplicitKeySignatureTrustEngine engine =
                        new ExplicitKeySignatureTrustEngine(credentialResolver, keyInfoResolver);

                final SignatureValidationFilter sigFilter = new SignatureValidationFilter(engine);
                sigFilter.setRequireSignedRoot(casProperties.getSamlMetadataUi().isRequireSignedRoot());
                filters.add(sigFilter);
            }
            chain.setFilters(filters);
            resources.put(this.resourceLoader.getResource(metadataFile), chain);
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
