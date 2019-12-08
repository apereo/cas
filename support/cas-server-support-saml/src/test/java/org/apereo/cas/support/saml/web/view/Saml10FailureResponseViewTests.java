package org.apereo.cas.support.saml.web.view;

import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.support.NoOpProtocolAttributeEncoder;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.authentication.SamlResponseBuilder;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.view.attributes.NoOpProtocolAttributesRenderer;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link Saml10FailureResponseView} class
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
@Tag("SAML")
public class Saml10FailureResponseViewTests extends AbstractOpenSamlTests {

    private Saml10FailureResponseView view;

    @BeforeEach
    public void initialize() {

        val builder = new Saml10ObjectBuilder(this.configBean);
        val samlResponseBuilder = new SamlResponseBuilder(builder, null, null, 0, 30,
            new NoOpProtocolAttributeEncoder(), null);
        view = new Saml10FailureResponseView(new NoOpProtocolAttributeEncoder(), null,
            new DefaultArgumentExtractor(new SamlServiceFactory()),
            StandardCharsets.UTF_8.name(),
            null,
            new DefaultAuthenticationServiceSelectionPlan(),
            NoOpProtocolAttributesRenderer.INSTANCE,
            samlResponseBuilder);
    }

    @Test
    public void verifyResponse() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.addParameter("TARGET", "service");

        val description = "Validation failed";
        this.view.renderMergedOutputModel(Collections.singletonMap("description", description), request, response);

        val responseText = response.getContentAsString();
        assertTrue(responseText.contains("Status"));
        assertTrue(responseText.contains(description));
    }

}
