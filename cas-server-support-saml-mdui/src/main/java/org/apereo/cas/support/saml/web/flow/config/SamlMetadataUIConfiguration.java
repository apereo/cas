package org.apereo.cas.support.saml.web.flow.config;

import com.google.common.collect.ImmutableList;
import net.shibboleth.idp.profile.spring.relyingparty.security.credential.impl.BasicResourceCredentialFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.web.flow.SamlMetadataUIParserAction;
import org.apereo.cas.support.saml.web.flow.SamlMetadataUIWebConfigurer;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
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
public class SamlMetadataUIConfiguration {

    private static final String DEFAULT_SEPARATOR = "::";

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public CasWebflowConfigurer samlMetadataUIWebConfigurer() {
        return new SamlMetadataUIWebConfigurer();
    }

    @Bean
    public Action samlMetadataUIParserAction() {
        final String parameter = StringUtils.defaultIfEmpty(casProperties.getSamlMetadataUIProperties().getParameter(),
                SamlProtocolConstants.PARAMETER_ENTITY_ID);
        return new SamlMetadataUIParserAction(parameter, metadataAdapter());
    }

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
        casProperties.getSamlMetadataUIProperties().getResources().forEach(Unchecked.consumer(r -> configureResource(resources, chain, r)));
        adapter.setRequireValidMetadata(casProperties.getSamlMetadataUIProperties().isRequireValidMetadata());
        adapter.setMetadataResources(resources);

        return adapter;
    }

    private void configureResource(final Map<Resource, MetadataFilterChain> resources,
                                   final MetadataFilterChain chain,
                                   final String r) throws Exception {
        final String[] splitArray = org.springframework.util.StringUtils.commaDelimitedListToStringArray(r);

        Arrays.stream(splitArray).forEach(Unchecked.consumer(entry -> {
            final String metadataFile = entry.split(DEFAULT_SEPARATOR)[0];
            final String signingKey = entry.split(DEFAULT_SEPARATOR)[1];

            final List<MetadataFilter> filters = new ArrayList<>();
            if (casProperties.getSamlMetadataUIProperties().getMaxValidity() > 0) {
                filters.add(new RequiredValidUntilFilter(casProperties.getSamlMetadataUIProperties().getMaxValidity()));
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
                sigFilter.setRequireSignedRoot(casProperties.getSamlMetadataUIProperties().isRequireSignedRoot());
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
        return adapter;
    }
}
