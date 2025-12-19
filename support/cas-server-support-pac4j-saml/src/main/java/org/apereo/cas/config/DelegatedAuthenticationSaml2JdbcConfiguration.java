package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.metadata.jdbc.SAML2JdbcMetadataGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This is {@link DelegatedAuthenticationSaml2JdbcConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.DelegatedAuthentication, module = "saml-jdbc", enabledByDefault = false)
@Configuration(value = "DelegatedAuthenticationSaml2JdbcConfiguration", proxyBeanMethods = false)
@ConditionalOnClass(JpaBeanFactory.class)
class DelegatedAuthenticationSaml2JdbcConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "delegatedSaml2ClientJdbcMetadataCustomizer")
    public DelegatedClientFactoryCustomizer delegatedSaml2ClientJdbcMetadataCustomizer(
        final CasConfigurationProperties casProperties) {
        return client -> {
            if (client instanceof final SAML2Client saml2Client) {
                val configuration = saml2Client.getConfiguration();
                casProperties.getAuthn().getPac4j().getSaml()
                    .stream()
                    .map(saml -> saml.getMetadata().getServiceProvider().getJdbc())
                    .filter(saml -> StringUtils.isNotBlank(saml.getUrl()) && StringUtils.isNotBlank(saml.getTableName()))
                    .forEach(saml -> {
                        val datasource = JpaBeans.newDataSource(saml);
                        val metadataGenerator = new SAML2JdbcMetadataGenerator(new JdbcTemplate(datasource), configuration.getServiceProviderEntityId());
                        metadataGenerator.setTableName(saml.getTableName());
                        configuration.setServiceProviderMetadataResource(ResourceUtils.NULL_RESOURCE);
                        configuration.setMetadataGenerator(metadataGenerator);
                    });
            }
        };
    }
}
