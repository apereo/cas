package org.jasig.cas.config;

import org.jasig.cas.support.saml.services.SamlIdPSingleLogoutServiceLogoutUrlBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * The {@link SamlIdPConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("samlIdPConfiguration")
public class SamlIdPConfiguration {
    
    /**
     * Template sp metadata resource.
     *
     * @return the resource
     */
    @Bean(name="templateSpMetadata")
    public Resource templateSpMetadata() {
        return new ClassPathResource("template-sp-metadata.xml");
    }

    /**
     * Saml id p single logout service logout url builder saml id p single logout service logout url builder.
     *
     * @return the saml idp single logout service logout url builder
     */
    @Bean(name={"defaultSingleLogoutServiceLogoutUrlBuilder",
                "samlIdPSingleLogoutServiceLogoutUrlBuilder"})
    public SamlIdPSingleLogoutServiceLogoutUrlBuilder samlIdPSingleLogoutServiceLogoutUrlBuilder() {
        return new SamlIdPSingleLogoutServiceLogoutUrlBuilder();
    }
    
}
