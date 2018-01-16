package org.apereo.cas.support.rest.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.rest.RestProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.apereo.cas.support.rest.RegisteredServiceResource;
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
@Configuration("restServicesConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class RestServicesConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired(required = false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        final DefaultRegisteredServiceJsonSerializer serializer = new DefaultRegisteredServiceJsonSerializer();
        return new MappingJackson2HttpMessageConverter(serializer.getObjectMapper());
    }

    @Bean
    public RegisteredServiceResource registeredServiceResourceRestController() {
        final RestProperties rest = casProperties.getRest();
        if (StringUtils.isBlank(rest.getAttributeName())) {
            throw new BeanCreationException("No attribute name is defined to enforce authorization when adding services via CAS REST APIs. "
            + "This is likely due to misconfiguration in CAS settings where the attribute name definition is absent");
        }
        if (StringUtils.isBlank(rest.getAttributeValue())) {
            throw new BeanCreationException("No attribute value is defined to enforce authorization when adding services via CAS REST APIs. "
                + "This is likely due to misconfiguration in CAS settings where the attribute value definition is absent");
        }
        return new RegisteredServiceResource(authenticationSystemSupport, webApplicationServiceFactory,
            servicesManager, rest.getAttributeName(), rest.getAttributeValue());
    }
}



