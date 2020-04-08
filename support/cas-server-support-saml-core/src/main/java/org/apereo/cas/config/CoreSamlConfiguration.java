package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;

import lombok.SneakyThrows;
import lombok.val;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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

    private static final int POOL_SIZE = 200;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Lazy
    @Bean(name = "shibboleth.VelocityEngine")
    @ConditionalOnMissingBean(name = "velocityEngineFactoryBean")
    public VelocityEngine velocityEngineFactoryBean() {
        val properties = new Properties();
        properties.put(RuntimeConstants.INPUT_ENCODING, StandardCharsets.UTF_8.name());
        properties.put(RuntimeConstants.ENCODING_DEFAULT, StandardCharsets.UTF_8.name());
        properties.put(RuntimeConstants.RESOURCE_LOADERS, "classpath, string, file");
        properties.put(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, FileUtils.getTempDirectory().getAbsolutePath());
        properties.put(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, Boolean.FALSE);
        properties.put(String.format("%s.classpath.class", RuntimeConstants.RESOURCE_LOADER), ClasspathResourceLoader.class.getName());
        properties.put(String.format("%s.string.class", RuntimeConstants.RESOURCE_LOADER), StringResourceLoader.class.getName());
        properties.put(String.format("%s.file.class", RuntimeConstants.RESOURCE_LOADER), FileResourceLoader.class.getName());
        return new VelocityEngine(properties);
    }

    @Bean(name = "shibboleth.OpenSAMLConfig")
    public OpenSamlConfigBean openSamlConfigBean() {
        return new OpenSamlConfigBean(parserPool());
    }

    @SneakyThrows
    @Bean(name = "shibboleth.ParserPool", initMethod = "initialize")
    public BasicParserPool parserPool() {
        val pool = new BasicParserPool();
        pool.setMaxPoolSize(POOL_SIZE);
        pool.setCoalescing(true);
        pool.setIgnoreComments(true);
        pool.setXincludeAware(false);
        pool.setExpandEntityReferences(false);
        pool.setIgnoreComments(true);
        pool.setNamespaceAware(true);

        val attributes = new HashMap<String, Object>();
        val clazz = ClassUtils.getClass(casProperties.getSamlCore().getSecurityManager());
        attributes.put("http://apache.org/xml/properties/security-manager", clazz.getDeclaredConstructor().newInstance());
        pool.setBuilderAttributes(attributes);

        val features = new HashMap<String, Boolean>();
        features.put("http://apache.org/xml/features/disallow-doctype-decl", Boolean.TRUE);
        features.put("http://apache.org/xml/features/validation/schema/normalized-value", Boolean.FALSE);
        features.put("http://javax.xml.XMLConstants/feature/secure-processing", Boolean.TRUE);
        features.put("http://xml.org/sax/features/external-general-entities", Boolean.FALSE);
        features.put("http://xml.org/sax/features/external-parameter-entities", Boolean.FALSE);
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
