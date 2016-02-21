package org.jasig.cas.config;

import net.shibboleth.utilities.java.support.velocity.SLF4JLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.jasig.cas.support.saml.services.SamlIdPSingleLogoutServiceLogoutUrlBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import java.util.Properties;

/**
 * The {@link SamlIdPConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("samlIdPConfiguration")
public class SamlIdPConfiguration {

    /**
     * The Resource loader path.
     */
    @Value("#{'%{idp.views:%{idp.home}/views}'.trim()}")
    private String resourceLoaderPath;

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

    /**
     * Velocity engine velocity engine factory bean.
     *
     * @return the velocity engine factory bean
     */
    @Bean(name="shibboleth.VelocityEngine")
    public VelocityEngineFactoryBean velocityEngine() {
        final VelocityEngineFactoryBean bean = new VelocityEngineFactoryBean();
        
        final Properties properties = new Properties();
        properties.setProperty("runtime.log.logsystem.class", 
                SLF4JLogChute.class.getName());
        properties.setProperty("input.encoding", "UTF-8");
        properties.setProperty("output.encoding", "UTF-8");
        properties.setProperty("resource.loader", "file, classpath, string");
        properties.setProperty("classpath.resource.loader.class",
                ClasspathResourceLoader.class.getName());
        properties.setProperty("string.resource.loader.class",
                StringResourceLoader.class.getName());
        properties.setProperty("file.resource.loader.class",
                FileResourceLoader.class.getName());

        properties.setProperty("file.resource.loader.path", this.resourceLoaderPath);
        properties.setProperty("file.resource.loader.cache", "false");
        bean.setOverrideLogging(false);
        bean.setVelocityProperties(properties);
        return bean;
    }
}
