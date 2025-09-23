package org.apereo.cas.web.view;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.support.NoOpProtocolAttributeEncoder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.DefaultAssertionBuilder;
import org.apereo.cas.web.view.attributes.AttributeValuesPerLineProtocolAttributesRenderer;
import org.apereo.cas.web.view.attributes.NoOpProtocolAttributesRenderer;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link Cas10ResponseView} class.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Tag("CAS")
class Cas10ResponseViewTests {

    private Map<String, Object> model;

    @BeforeEach
    void initialize() {
        this.model = new HashMap<>();
        val list = new ArrayList<Authentication>();
        list.add(CoreAuthenticationTestUtils.getAuthentication("someothername"));
        val testService = CoreAuthenticationTestUtils.getWebApplicationService("TestService");
        model.put("assertion", DefaultAssertionBuilder.builder()
            .primaryAuthentication(CoreAuthenticationTestUtils.getAuthentication())
            .authentications(list)
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService(testService.getId()))
            .service(testService)
            .newLogin(true)
            .build()
            .assemble());
    }

    @Test
    void verifySuccessView() throws Throwable {
        val response = new MockHttpServletResponse();
        val view = new Cas10ResponseView(true, new NoOpProtocolAttributeEncoder(),
            mock(ServicesManager.class), mock(AuthenticationAttributeReleasePolicy.class), new DefaultAuthenticationServiceSelectionPlan(),
            new AttributeValuesPerLineProtocolAttributesRenderer(), mock(AttributeDefinitionStore.class));
        view.render(model, new MockHttpServletRequest(), response);
        assertTrue(response.getContentAsString().startsWith("yes\ntest\n"));
    }

    @Test
    void verifyFailureView() throws Throwable {
        val response = new MockHttpServletResponse();
        val view = new Cas10ResponseView(false, new NoOpProtocolAttributeEncoder(),
            mock(ServicesManager.class), mock(AuthenticationAttributeReleasePolicy.class),
            new DefaultAuthenticationServiceSelectionPlan(),
            NoOpProtocolAttributesRenderer.INSTANCE, mock(AttributeDefinitionStore.class));
        view.render(model, new MockHttpServletRequest(), response);
        assertEquals("no\n\n", response.getContentAsString());
    }
}
