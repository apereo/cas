package org.jasig.cas.support.saml.web.view;

import static org.junit.Assert.*;

import java.util.Collections;

import org.jasig.cas.support.saml.AbstractOpenSamlTests;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit test for {@link Saml10FailureResponseView} class
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 *
 */
public class Saml10FailureResponseViewTests extends AbstractOpenSamlTests {

    private Saml10FailureResponseView view = new Saml10FailureResponseView();

    @Test
    public void verifyResponse() throws Exception {
        final MockHttpServletRequest request =  new MockHttpServletRequest();
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
