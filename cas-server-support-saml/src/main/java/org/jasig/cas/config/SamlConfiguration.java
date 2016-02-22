package org.jasig.cas.config;

import com.sun.org.apache.xerces.internal.util.SecurityManager;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.jasig.cas.authentication.support.CasAttributeEncoder;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.saml.OpenSamlConfigBean;
import org.jasig.cas.support.saml.web.view.Saml10FailureResponseView;
import org.jasig.cas.support.saml.web.view.Saml10SuccessResponseView;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link SamlConfiguration} that creates the necessary opensaml context and beans.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("samlConfiguration")
public class SamlConfiguration {

    /**
     * The constant POOL_SIZE.
     */
    private static final int POOL_SIZE = 100;

    /**
     * The Issuer.
     */
    @Value("${cas.saml.response.issuer:localhost}")
    private String issuer;

    /**
     * The Skew allowance.
     */
    @Value("${cas.saml.response.skewAllowance:0}")
    private int skewAllowance;

    /**
     * The Services manager.
     */
    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    /**
     * The Cas attribute encoder.
     */
    @Autowired
    @Qualifier("casAttributeEncoder")
    private CasAttributeEncoder casAttributeEncoder;

    /**
     * Cas saml service success view saml 10 success response view.
     *
     * @return the saml 10 success response view
     */
    @Bean(name="casSamlServiceSuccessView")
    public Saml10SuccessResponseView casSamlServiceSuccessView() {
        final Saml10SuccessResponseView view = new Saml10SuccessResponseView();
        view.setServicesManager(this.servicesManager);
        view.setCasAttributeEncoder(this.casAttributeEncoder);
        view.setIssuer(this.issuer);
        view.setSkewAllowance(this.skewAllowance);
        return view;
    }

    /**
     * Cas saml service failure view saml 10 failure response view.
     *
     * @return the saml 10 failure response view
     */
    @Bean(name="casSamlServiceFailureView")
    public Saml10FailureResponseView casSamlServiceFailureView() {
        final Saml10FailureResponseView view = new Saml10FailureResponseView();
        view.setServicesManager(this.servicesManager);
        view.setCasAttributeEncoder(this.casAttributeEncoder);
        return view;
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
