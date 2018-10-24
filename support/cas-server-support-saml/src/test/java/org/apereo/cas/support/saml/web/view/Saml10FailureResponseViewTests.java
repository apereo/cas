package org.apereo.cas.support.saml.web.view;

import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.view.attributes.NoOpProtocolAttributesRenderer;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Unit test for {@link Saml10FailureResponseView} class
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
public class Saml10FailureResponseViewTests extends AbstractOpenSamlTests {

    private Saml10FailureResponseView view;

    @BeforeEach
    public void initialize() {

        val builder = new Saml10ObjectBuilder(this.configBean);
        view = new Saml10FailureResponseView(null, null,
            builder, new DefaultArgumentExtractor(new SamlServiceFactory()),
            StandardCharsets.UTF_8.name(), 0, 30, null,
            new DefaultAuthenticationServiceSelectionPlan(), new NoOpProtocolAttributesRenderer());
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
