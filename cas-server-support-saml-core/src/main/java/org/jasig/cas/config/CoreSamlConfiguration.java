package org.jasig.cas.config;

import com.sun.org.apache.xerces.internal.util.SecurityManager;
import net.shibboleth.utilities.java.support.velocity.SLF4JLogChute;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.jasig.cas.support.saml.OpenSamlConfigBean;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link CoreSamlConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("coreSamlConfiguration")
public class CoreSamlConfiguration {
    
    /**
     * The constant POOL_SIZE.
     */
    private static final int POOL_SIZE = 100;
    
    /**
     * Velocity engine velocity engine factory bean.
     *
     * @return the velocity engine factory bean
     */
    @Bean(name = "shibboleth.VelocityEngine")
    public VelocityEngineFactoryBean velocityEngineFactoryBean() {
        final VelocityEngineFactoryBean bean = new VelocityEngineFactoryBean();

        final Properties properties = new Properties();
        properties.put("runtime.log.logsystem.class", SLF4JLogChute.class.getName());
        properties.put("input.encoding", "UTF-8");
        properties.put("output.encoding", "UTF-8");
        properties.put("resource.loader", "file, classpath, string");
        properties.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        properties.put("string.resource.loader.class", StringResourceLoader.class.getName());
        properties.put("file.resource.loader.class", FileResourceLoader.class.getName());

        properties.put("file.resource.loader.path", FileUtils.getTempDirectory().getAbsolutePath());
        properties.put("file.resource.loader.cache", false);
        bean.setOverrideLogging(false);
        bean.setVelocityProperties(properties);
        return bean;
    }

    /**
     * Open saml config bean open saml config bean.
     *
     * @return the open saml config bean
     */
    @Bean(name="shibboleth.OpenSAMLConfig")
    @DependsOn("shibboleth.ParserPool")
    public OpenSamlConfigBean openSamlConfigBean() {
        return new OpenSamlConfigBean();
    }

    /**
     * Parser pool basic parser pool.
     *
     * @return the basic parser pool
     */
    @Bean(name="shibboleth.ParserPool", initMethod = "initialize")
    public BasicParserPool parserPool() {
        final BasicParserPool pool = new BasicParserPool();
        pool.setMaxPoolSize(POOL_SIZE);
        pool.setCoalescing(true);
        pool.setIgnoreComments(true);
        pool.setXincludeAware(false);
        pool.setExpandEntityReferences(false);
        pool.setIgnoreComments(true);
        pool.setNamespaceAware(true);

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("http://apache.org/xml/properties/security-manager",
                new SecurityManager());
        pool.setBuilderAttributes(attributes);

        final Map<String, Boolean> features = new HashMap<>();
        features.put("http://apache.org/xml/features/disallow-doctype-decl", Boolean.TRUE);
        features.put("http://apache.org/xml/features/validation/schema/normalized-value",
                Boolean.FALSE);
        features.put("http://javax.xml.XMLConstants/feature/secure-processing",
                Boolean.TRUE);
        features.put("http://xml.org/sax/features/external-general-entities",
                Boolean.FALSE);
        features.put("http://xml.org/sax/features/external-parameter-entities",
                Boolean.FALSE);
        pool.setBuilderFeatures(features);
        return pool;
    }

    /**
     * Builder factory xml object builder factory.
     *
     * @return the xml object builder factory
     */
    @Bean(name="shibboleth.BuilderFactory")
    @DependsOn("shibboleth.OpenSAMLConfig")
    public XMLObjectBuilderFactory builderFactory() {
        return XMLObjectProviderRegistrySupport.getBuilderFactory();
    }

    /**
     * Marshaller factory marshaller factory.
     *
     * @return the marshaller factory
     */
    @Bean(name="shibboleth.MarshallerFactory")
    @DependsOn("shibboleth.OpenSAMLConfig")
    public MarshallerFactory marshallerFactory() {
        return XMLObjectProviderRegistrySupport.getMarshallerFactory();
    }

    /**
     * Unmarshaller factory unmarshaller factory.
     *
     * @return the unmarshaller factory
     */
    @Bean(name="shibboleth.MarshallerFactory")
    @DependsOn("shibboleth.OpenSAMLConfig")
    public UnmarshallerFactory unmarshallerFactory() {
        return XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
    }
}
