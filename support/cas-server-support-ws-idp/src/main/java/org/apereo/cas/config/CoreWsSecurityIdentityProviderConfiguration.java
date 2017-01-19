package org.apereo.cas.config;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import java.util.Collections;

/**
 * This is {@link CoreWsSecurityIdentityProviderConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("coreWsSecuritySecurityTokenServiceConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportResource(locations = {"classpath:META-INF/cxf/cxf.xml"})
public class CoreWsSecurityIdentityProviderConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    public ServletRegistrationBean wsIdpMetadataServlet() {
        final ServletRegistrationBean bean = new ServletRegistrationBean();
        bean.setEnabled(true);
        bean.setName("cxfServlet");
        /**
         * Fediz dependencies are only available inside the fediz-idp war file
         * Need to see if including the idp as a war dependency allow us to use
         * sources buried in the war. war is published to central.
         */
        bean.setServlet(new MetadataServlet());
        bean.setUrlMappings(Collections.singleton("/ws/idp/*"));
        bean.setAsyncSupported(true);
        return bean;
    }

}
