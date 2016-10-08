package org.apereo.cas.support.saml.web.view;

import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Unit test for {@link Saml10FailureResponseView} class
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 *
 */
public class Saml10FailureResponseViewTests extends AbstractOpenSamlTests {

    private Saml10FailureResponseView view;
    
    @Before
    public void setUp() throws Exception {
        view = new Saml10FailureResponseView();
        final Saml10ObjectBuilder builder = new Saml10ObjectBuilder();
        builder.setConfigBean(this.configBean);
        this.view.setSamlObjectBuilder(builder);
    }
    
    @Test
    public void verifyResponse() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        request.addParameter("TARGET", "service");

        final String description = "Validation failed";
        this.view.renderMergedOutputModel(
                Collections.<String, Object>singletonMap("description", description), request, response);

        final String responseText = response.getContentAsString();
        assertTrue(responseText.contains("Status"));
        assertTrue(responseText.contains(description));
    }

}
