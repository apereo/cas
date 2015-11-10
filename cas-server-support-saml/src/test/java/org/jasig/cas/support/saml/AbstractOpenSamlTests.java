package org.jasig.cas.support.saml;

import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;

/**
 * OpenSaml context loading tests.
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/META-INF/spring/*-config.xml"})
@WebAppConfiguration()
public abstract class AbstractOpenSamlTests {

    protected static final String SAML_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
            + "ID=\"5545454455\" Version=\"2.0\" IssueInstant=\"Value\" "
            + "ProtocolBinding=\"urn:oasis:names.tc:SAML:2.0:bindings:HTTP-Redirect\" "
            + "ProviderName=\"https://localhost:8443/myRutgers\" "
            + "AssertionConsumerServiceURL=\"https://localhost:8443/myRutgers\"/>";

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected OpenSamlConfigBean configBean;

    @Autowired
    protected ParserPool parserPool;

    @Autowired
    @Qualifier("shibboleth.BuilderFactory")
    protected XMLObjectBuilderFactory builderFactory;

    @Autowired
    @Qualifier("shibboleth.MarshallerFactory")
    protected MarshallerFactory marshallerFactory;

    @Autowired
    @Qualifier("shibboleth.UnmarshallerFactory")
    protected UnmarshallerFactory unmarshallerFactory;

    @Test
    public void autowireApplicationContext() {
        assertNotNull(this.applicationContext);
        assertNotNull(this.configBean);
        assertNotNull(this.parserPool);
        assertNotNull(this.builderFactory);
        assertNotNull(this.unmarshallerFactory);
        assertNotNull(this.marshallerFactory);
        assertNotNull(this.configBean.getParserPool());
    }

    @Test
    public void loadStaticContextFactories() {
        assertNotNull(XMLObjectProviderRegistrySupport.getParserPool());
        assertNotNull(XMLObjectProviderRegistrySupport.getBuilderFactory());
        assertNotNull(XMLObjectProviderRegistrySupport.getMarshallerFactory());
        assertNotNull(XMLObjectProviderRegistrySupport.getUnmarshallerFactory());
    }


    @Test
    public void ensureParserIsInitialized() throws Exception {
        assertNotNull(this.parserPool);
        assertNotNull(this.parserPool.getBuilder());
    }
}
