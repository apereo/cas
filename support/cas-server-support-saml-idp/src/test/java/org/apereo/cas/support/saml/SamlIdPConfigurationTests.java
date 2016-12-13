package org.apereo.cas.support.saml;

import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.SamlIdPConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.common.binding.BindingDescriptor;
import org.opensaml.saml.common.binding.impl.SAMLSOAPDecoderBodyHandler;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPSOAP11Decoder;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link SamlIdPConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CasCoreAuthenticationConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreWebflowConfiguration.class,
        SamlIdPConfiguration.class,
        RefreshAutoConfiguration.class,
        AopAutoConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreValidationConfiguration.class,
        CasCoreConfiguration.class,
        CoreSamlConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreUtilConfiguration.class})
public class SamlIdPConfigurationTests {

    @Autowired
    @Qualifier("shibboleth.ParserPool")
    private BasicParserPool parserPool;

    @Test
    public void verifyEcp() throws Exception {
        final MockHttpServletRequest mockRequest =
                new MockHttpServletRequest("POST", "https://sso.example.org");
        mockRequest.setContentType(MediaType.TEXT_XML_VALUE);
        mockRequest.setContent(IOUtils.toByteArray(
                new ClassPathResource("sample-ecp-request.xml").getInputStream()));

        final HTTPSOAP11Decoder decoder = new HTTPSOAP11Decoder();
        decoder.setParserPool(parserPool);
        decoder.setHttpServletRequest(mockRequest);
        decoder.setBindingDescriptor(new BindingDescriptor());
        decoder.setBodyHandler(new SAMLSOAPDecoderBodyHandler());
        decoder.initialize();
        decoder.decode();
        final SOAP11Context context = decoder.getMessageContext().getSubcontext(SOAP11Context.class);
        assertNotNull(context);
        assertNotNull(context.getEnvelope());
    }
}
