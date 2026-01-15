package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationErrorViewResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.sso.services.allow-missing-service-parameter=false")
@Tag("Webflow")
@ExtendWith(CasTestExtension.class)
class DelegatedAuthenticationErrorViewResolverTests {
    @Autowired
    @Qualifier("delegatedAuthenticationErrorViewResolver")
    private ErrorViewResolver resolver;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION)
    private Action delegatedAuthenticationAction;

    @Test
    void verifyOperationWithEx() {
        assertNotNull(delegatedAuthenticationAction);

        val request = new MockHttpServletRequest();
        request.addParameter("templates/error", "failure");
        request.setAttribute("jakarta.servlet.error.exception", UnauthorizedServiceException.denied("templates/error"));
        val mv = resolver.resolveErrorView(request, HttpStatus.FORBIDDEN, Map.of());
        assertEquals(HttpStatus.FORBIDDEN, mv.getStatus());
        assertEquals(CasWebflowConstants.VIEW_ID_DELEGATED_AUTHN_ERROR_VIEW, mv.getViewName());
    }

    @Test
    void verifyOperationWithoutEx() {
        val request = new MockHttpServletRequest();
        val mv = resolver.resolveErrorView(request, HttpStatus.INTERNAL_SERVER_ERROR, Map.of());
        assertEquals(CasWebflowConstants.VIEW_ID_DELEGATED_AUTHENTICATION_STOP_WEBFLOW, mv.getViewName());
    }
}
