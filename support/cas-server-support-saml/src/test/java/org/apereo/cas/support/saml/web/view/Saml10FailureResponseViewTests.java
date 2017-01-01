package org.apereo.cas.support.saml.web.view;

import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.junit.Before;
import org.junit.Test;
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

    @Before
    public void setUp() throws Exception {

        final Saml10ObjectBuilder builder = new Saml10ObjectBuilder(this.configBean);
        view = new Saml10FailureResponseView(null, null, "attribute",
                builder, new DefaultArgumentExtractor(new SamlServiceFactory()),
                StandardCharsets.UTF_8.name(), 0);
    }

    @Test
    public void verifyResponse() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.addParameter("TARGET", "service");

        final String description = "Validation failed";
        this.view.renderMergedOutputModel(
                Collections.singletonMap("description", description), request, response);

        final String responseText = response.getContentAsString();
        assertTrue(responseText.contains("Status"));
        assertTrue(responseText.contains(description));
    }

}
