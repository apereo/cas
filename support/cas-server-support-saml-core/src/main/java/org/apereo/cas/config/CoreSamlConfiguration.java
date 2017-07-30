package org.apereo.cas.config;

import net.shibboleth.utilities.java.support.velocity.SLF4JLogChute;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import java.nio.charset.StandardCharsets;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CoreSamlConfiguration {
    
    private static final int POOL_SIZE = 100;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Lazy
    @Bean(name = "shibboleth.VelocityEngine")
    public VelocityEngineFactoryBean velocityEngineFactoryBean() {
        final VelocityEngineFactoryBean bean = new VelocityEngineFactoryBean();

        final Properties properties = new Properties();
        properties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, SLF4JLogChute.class.getName());
        properties.put(RuntimeConstants.INPUT_ENCODING, StandardCharsets.UTF_8.name());
        properties.put(RuntimeConstants.OUTPUT_ENCODING, StandardCharsets.UTF_8.name());
        properties.put(RuntimeConstants.ENCODING_DEFAULT, StandardCharsets.UTF_8.name());
        properties.put(RuntimeConstants.RESOURCE_LOADER, "file, classpath, string");
        properties.put(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, FileUtils.getTempDirectory().getAbsolutePath());
        properties.put(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, Boolean.FALSE);
        properties.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        properties.put("string.resource.loader.class", StringResourceLoader.class.getName());
        properties.put("file.resource.loader.class", FileResourceLoader.class.getName());
        bean.setOverrideLogging(false);
        bean.setVelocityProperties(properties);
        return bean;
    }
    
    @Bean(name = "shibboleth.OpenSAMLConfig")
    public OpenSamlConfigBean openSamlConfigBean() {
        return new OpenSamlConfigBean(parserPool());
    }
    
    @Bean(name = "shibboleth.ParserPool", initMethod = "initialize")
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
        try {
            final Class clazz = ClassUtils.getClass(casProperties.getSamlCore().getSecurityManager());
            attributes.put("http://apache.org/xml/properties/security-manager", clazz.newInstance());
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
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


    @Bean(name = "shibboleth.BuilderFactory")
    @DependsOn("shibboleth.OpenSAMLConfig")
    public XMLObjectBuilderFactory builderFactory() {
        return XMLObjectProviderRegistrySupport.getBuilderFactory();
    }

 
    @Bean(name = "shibboleth.MarshallerFactory")
    @DependsOn("shibboleth.OpenSAMLConfig")
    public MarshallerFactory marshallerFactory() {
        return XMLObjectProviderRegistrySupport.getMarshallerFactory();
    }
    
    @Bean(name = "shibboleth.UnmarshallerFactory")
    @DependsOn("shibboleth.OpenSAMLConfig")
    public UnmarshallerFactory unmarshallerFactory() {
        return XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
    }
}
