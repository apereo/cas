package org.apereo.cas.support.saml.web.view;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.authentication.principal.SamlServiceFactory;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.junit.Test;
import org.junit.Before;
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
@Slf4j
public class Saml10FailureResponseViewTests extends AbstractOpenSamlTests {

    private Saml10FailureResponseView view;

    @Before
    public void initialize() {

        final var builder = new Saml10ObjectBuilder(this.configBean);
        view = new Saml10FailureResponseView(null, null, "attribute",
                builder, new DefaultArgumentExtractor(new SamlServiceFactory(new Saml10ObjectBuilder(configBean))),
                StandardCharsets.UTF_8.name(), 0, 30, null);
    }

    @Test
    public void verifyResponse() throws Exception {
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        request.addParameter("TARGET", "service");

        final var description = "Validation failed";
        this.view.renderMergedOutputModel(
                Collections.singletonMap("description", description), request, response);

        final var responseText = response.getContentAsString();
        assertTrue(responseText.contains("Status"));
        assertTrue(responseText.contains(description));
    }

}
