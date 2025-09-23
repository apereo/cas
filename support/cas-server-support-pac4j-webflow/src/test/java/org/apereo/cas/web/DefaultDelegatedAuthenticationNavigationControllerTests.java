package org.apereo.cas.web;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.flow.controller.DefaultDelegatedAuthenticationNavigationController;

import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.RedirectView;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDelegatedAuthenticationNavigationControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Delegation")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DefaultDelegatedAuthenticationNavigationControllerTests {

    @Autowired
    @Qualifier("defaultDelegatedAuthenticationNavigationController")
    private DefaultDelegatedAuthenticationNavigationController controller;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @BeforeEach
    void beforeEach() {
        servicesManager.deleteAll();
    }

    @Test
    void verifyRedirectByParam() throws Throwable {
        val request = new MockHttpServletRequest();
        request.addParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "CasClient");
        request.addParameter("customParam", "customValue");
        val response = new MockHttpServletResponse();
        var view = controller.redirectResponseToFlow("CASClient", request, response);
        assertInstanceOf(RedirectView.class, view);
        assertTrue(new URIBuilder(((AbstractUrlBasedView) view).getUrl()).getQueryParams()
            .stream().anyMatch(valuePair -> valuePair.getName().equals(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER)));
        view = controller.postResponseToFlow("CASClient", request, response);
        assertInstanceOf(RedirectView.class, view);
        assertTrue(new URIBuilder(((AbstractUrlBasedView) view).getUrl()).getQueryParams()
            .stream().anyMatch(valuePair -> valuePair.getName().equals(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER)));
    }

}
