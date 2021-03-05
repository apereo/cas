package org.apereo.cas.support.saml.authentication.principal;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.config.SamlConfiguration;
import org.apereo.cas.config.authentication.support.SamlAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.authentication.support.SamlServiceFactoryConfiguration;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;

import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link SamlServiceFactory}
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@SpringBootTest(classes = {
    SamlAuthenticationEventExecutionPlanConfiguration.class,
    SamlServiceFactoryConfiguration.class,
    SamlConfiguration.class,
    AbstractOpenSamlTests.SharedTestConfiguration.class
}, properties = "spring.main.allow-bean-definition-overriding=true")
@Tag("SAML")
public class SamlServiceFactoryTests extends AbstractOpenSamlTests {

    private static final String BODY = "<!--    Licensed to Jasig under one or more contributor license    agreements. See the NOTICE file distributed with this work"
        + "for additional information regarding copyright ownership.    Jasig licenses this file to you under the Apache License,    "
        + "Version 2.0 (the \"License\"); you may not use this file    except in compliance with the License.  You may obtain a    "
        + "copy of the License at the following location:      http://www.apache.org/licenses/LICENSE-2.0    Unless required by applicable law or agreed to in writing,"
        + "software distributed under the License is distributed on an    \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY    KIND, either express or implied."
        + "See the License for the    specific language governing permissions and limitations    under the License.-->"
        + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns=\"urn:oasis:names:tc:SAML:1.0:protocol\">    <soap:Header/>   "
        + "<soap:Body>        <Request MajorVersion=\"1\" MinorVersion=\"1\" RequestID=\"_e444ee1af9a7f6d656d76e8810299544\" IssueInstant=\"2018-05-10T16:39:46Z\">"
        + "<AssertionArtifact>ST-AAHJJ4pD5ZyoQkY9i08GsvYRVOyKeWws4SA4xwv+5HX9UgL7fCRBp2Ad</AssertionArtifact>        </Request>    </soap:Body></soap:Envelope>";

    @Autowired
    @Qualifier("samlServiceFactory")
    private ServiceFactory<SamlService> samlServiceFactory;

    @Test
    public void verifyObtainService() {
        val request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "test");

        val service = samlServiceFactory.createService(request);
        assertEquals("test", service.getId());
    }

    @Test
    public void verifyServiceDoesNotExist() {
        val request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        assertNull(samlServiceFactory.createService(request));
    }

    @Test
    public void verifyPayloadCanBeParsedProperly() {
        val request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "test");
        request.setContent(BODY.getBytes(StandardCharsets.UTF_8));
        request.setRequestURI(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE);
        assertNotNull(samlServiceFactory.createService(request));
    }

    @Test
    public void verifyPayloadAsAttr() {
        val request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "test");
        request.setAttribute(SamlProtocolConstants.PARAMETER_SAML_REQUEST, BODY);
        request.setRequestURI(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE);
        assertNotNull(samlServiceFactory.createService(request));
        assertThrows(NotImplementedException.class, () -> samlServiceFactory.createService("serviceid"));
    }

    @Test
    public void verifyEmptyBody() {
        val body = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
            + "xmlns=\"urn:oasis:names:tc:SAML:1.0:protocol\"><soap:Header/></soap:Envelope>";
        val request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "test");
        request.setAttribute(SamlProtocolConstants.PARAMETER_SAML_REQUEST, body);
        request.setRequestURI(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE);
        val service = samlServiceFactory.createService(request);
        assertNull(service.getArtifactId());
        assertNull(service.getRequestId());
    }

    @Test
    public void verifyNoReqIdBody() {
        val body = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
            + "xmlns=\"urn:oasis:names:tc:SAML:1.0:protocol\"><soap:Header/><soap:Body>"
            + "<Request MajorVersion=\"1\" MinorVersion=\"1\" IssueInstant=\"2018-05-10T16:39:46Z\">"
            + "<AssertionArtifact>ST-AAHJJ4pD5ZyoQkY9i08GsvYRVOyKeWws4SA4xwv+5HX9UgL7fCRBp2Ad</AssertionArtifact></Request>"
            + "</soap:Body></soap:Envelope>";
        val request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "test");
        request.setAttribute(SamlProtocolConstants.PARAMETER_SAML_REQUEST, body);
        request.setRequestURI(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE);
        val service = samlServiceFactory.createService(request);
        assertNotNull(service.getArtifactId());
        assertNull(service.getRequestId());
    }

    @Test
    public void verifyArtifactInBody() {
        val body = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
            + "xmlns=\"urn:oasis:names:tc:SAML:1.0:protocol\"><soap:Header/><soap:Body>"
            + "<Request MajorVersion=\"1\" MinorVersion=\"1\" IssueInstant=\"2018-05-10T16:39:46Z\">"
            + "</Request></soap:Body></soap:Envelope>";
        val request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "test");
        request.setAttribute(SamlProtocolConstants.PARAMETER_SAML_REQUEST, body);
        request.setRequestURI(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE);
        val service = samlServiceFactory.createService(request);
        assertNull(service.getArtifactId());
    }

    @Test
    public void verifyBadBody() {
        val body = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
            + "xmlns=\"urn:oasis:names:tc:SAML:1.0:protocol\"><soap:Header/><soap:Body>"
            + "</Request></soap:Envelope>";
        val request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());
        request.setParameter(SamlProtocolConstants.CONST_PARAM_TARGET, "test");
        request.setAttribute(SamlProtocolConstants.PARAMETER_SAML_REQUEST, body);
        request.setRequestURI(SamlProtocolConstants.ENDPOINT_SAML_VALIDATE);
        val service = samlServiceFactory.createService(request);
        assertNull(service.getArtifactId());
        assertNull(service.getArtifactId());
    }


}
