package org.apereo.cas.support.openid.web;

import org.apereo.cas.web.AbstractDelegateController;
import org.apereo.cas.web.DelegatingController;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatingControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 * @deprecated Since 6.2.0
 */
@Deprecated(since = "6.2.0")
@Tag("Web")
public class DelegatingControllerTests {

    @Test
    public void verifyFails() throws Exception {
        val request = new MockHttpServletRequest();
        request.setMethod("POST");
        val response = new MockHttpServletResponse();
        val ctrl = new DelegatingController();

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ctrl.setApplicationContext(applicationContext);

        val mv = ctrl.handleRequest(request, response);
        assertNotNull(mv);
        assertEquals(ctrl.getFailureView(), mv.getViewName());
    }

    @Test
    public void verifyOps() throws Exception {
        val request = new MockHttpServletRequest();
        request.setMethod("POST");
        val response = new MockHttpServletResponse();
        val ctrl = new DelegatingController();
        ctrl.setDelegates(List.of(new AbstractDelegateController() {
            @Override
            public boolean canHandle(final HttpServletRequest request, final HttpServletResponse response) {
                return true;
            }

            @Override
            protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) {
                return new ModelAndView("success");
            }
        }));
        val mv = ctrl.handleRequest(request, response);
        assertNotNull(mv);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, mv.getViewName());
    }

}
