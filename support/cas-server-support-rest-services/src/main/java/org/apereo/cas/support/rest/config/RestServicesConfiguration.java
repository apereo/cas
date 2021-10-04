package org.apereo.cas.support.rest.config;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.rest.RegisteredServiceResource;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * This is {@link RestServicesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "restServicesConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class RestServicesConfiguration {


    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        val serializer = new RegisteredServiceJsonSerializer();
        return new MappingJackson2HttpMessageConverter(serializer.getObjectMapper());
    }

    @Bean
    @Autowired
    public RegisteredServiceResource registeredServiceResourceRestController(
        final CasConfigurationProperties casProperties,
        @Qualifier("webApplicationServiceFactory")
        final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
        final AuthenticationSystemSupport authenticationSystemSupport) {
        val rest = casProperties.getRest()
            .getServices();
        if (StringUtils.isBlank(rest.getAttributeName()) || StringUtils.isBlank(rest.getAttributeValue())) {
            throw new BeanCreationException(
                "No attribute name or value is defined to enforce authorization when adding services via CAS REST APIs. "
                + "This is likely due to misconfiguration in CAS settings where the attribute name/value definition is absent");
        }
        return new RegisteredServiceResource(authenticationSystemSupport, webApplicationServiceFactory,
            servicesManager, rest.getAttributeName(), rest.getAttributeValue());
    }
}
