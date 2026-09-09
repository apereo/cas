package org.apereo.cas.web.view;

import module java.base;
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
    void verifySuccessViewRejectsLineBreaksInPrincipalId() throws Throwable {
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser\nyes\nadministrator", Map.<String, List<Object>>of());
        val testService = CoreAuthenticationTestUtils.getWebApplicationService("TestService");
        val forgedModel = new HashMap<String, Object>();
        forgedModel.put("assertion", DefaultAssertionBuilder.builder()
            .primaryAuthentication(CoreAuthenticationTestUtils.getAuthentication(principal))
            .authentications(List.of(CoreAuthenticationTestUtils.getAuthentication(principal)))
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService(testService.getId()))
            .service(testService)
            .newLogin(true)
            .build()
            .assemble());

        val response = new MockHttpServletResponse();
        val view = new Cas10ResponseView(true, new NoOpProtocolAttributeEncoder(),
            mock(ServicesManager.class), mock(AuthenticationAttributeReleasePolicy.class),
            new DefaultAuthenticationServiceSelectionPlan(),
            NoOpProtocolAttributesRenderer.INSTANCE, mock(AttributeDefinitionStore.class));
        view.render(forgedModel, new MockHttpServletRequest(), response);
        assertEquals("yes\ncasuseryesadministrator\n", response.getContentAsString());
    }

    @Test
    void verifySuccessViewRejectsLineBreaksInAttributeValues() throws Throwable {
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            Map.of("memberOf", List.<Object>of("staff\nadministrator=true")));
        val testService = CoreAuthenticationTestUtils.getWebApplicationService("TestService");
        val forgedModel = new HashMap<String, Object>();
        forgedModel.put("assertion", DefaultAssertionBuilder.builder()
            .primaryAuthentication(CoreAuthenticationTestUtils.getAuthentication(principal))
            .authentications(List.of(CoreAuthenticationTestUtils.getAuthentication(principal)))
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService(testService.getId()))
            .service(testService)
            .newLogin(true)
            .build()
            .assemble());

        val response = new MockHttpServletResponse();
        val view = new Cas10ResponseView(true, new NoOpProtocolAttributeEncoder(),
            mock(ServicesManager.class), mock(AuthenticationAttributeReleasePolicy.class),
            new DefaultAuthenticationServiceSelectionPlan(),
            new AttributeValuesPerLineProtocolAttributesRenderer(), mock(AttributeDefinitionStore.class));
        view.render(forgedModel, new MockHttpServletRequest(), response);

        val output = response.getContentAsString();
        assertTrue(output.startsWith("yes\ncasuser\n"));
        assertTrue(output.lines().noneMatch(line -> "administrator=true".equals(line)));
        assertTrue(output.lines().anyMatch(line -> line.contains("staffadministrator=true")));
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
