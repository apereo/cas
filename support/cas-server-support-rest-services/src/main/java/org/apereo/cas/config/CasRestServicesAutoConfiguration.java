package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.rest.RegisteredServiceResource;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * This is {@link CasRestServicesAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.ServiceRegistry, module = "rest")
@AutoConfiguration
public class CasRestServicesAutoConfiguration {
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
        final ConfigurableApplicationContext applicationContext) {
        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        return new MappingJackson2HttpMessageConverter(serializer.getObjectMapper());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RegisteredServiceResource registeredServiceResourceRestController(
        final CasConfigurationProperties casProperties,
        @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
        final AuthenticationSystemSupport authenticationSystemSupport) {
        val rest = casProperties.getRest().getServices();
        if (StringUtils.isBlank(rest.getAttributeName()) || StringUtils.isBlank(rest.getAttributeValue())) {
            throw new BeanCreationException(
                "No attribute name or value is defined to enforce authorization when adding services via CAS REST APIs. "
                + "This is likely due to misconfiguration in CAS settings where the attribute name/value definition is absent");
        }
        return new RegisteredServiceResource(authenticationSystemSupport, webApplicationServiceFactory,
            servicesManager, rest.getAttributeName(), rest.getAttributeValue());
    }
}
