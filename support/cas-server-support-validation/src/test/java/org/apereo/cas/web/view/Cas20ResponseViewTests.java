package org.apereo.cas.web.view;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.DefaultAuthenticationContextValidator;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultMultifactorTriggerSelectionStrategy;
import org.apereo.cas.services.web.support.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.web.AbstractServiceValidateController;
import org.apereo.cas.web.AbstractServiceValidateControllerTests;
import org.apereo.cas.web.ServiceValidateController;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashSet;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Cas20ResponseView}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class Cas20ResponseViewTests extends AbstractServiceValidateControllerTests {

    @Autowired
    @Qualifier("cas3ServiceJsonView")
    private View cas3ServiceJsonView;

    @Autowired
    @Qualifier("cas2SuccessView")
    private View cas2SuccessView;

    @Autowired
    @Qualifier("cas2ServiceFailureView")
    private View cas2ServiceFailureView;

    @Override
    public AbstractServiceValidateController getServiceValidateControllerInstance() throws Exception {
        return new ServiceValidateController(
                getValidationSpecification(),
                getAuthenticationSystemSupport(), getServicesManager(),
                getCentralAuthenticationService(),
                getProxyHandler(),
                getArgumentExtractor(),
                new DefaultMultifactorTriggerSelectionStrategy("", ""),
                new DefaultAuthenticationContextValidator("", "OPEN", "test"),
                cas3ServiceJsonView, cas2SuccessView, 
                cas2ServiceFailureView, "authenticationContext",
                new LinkedHashSet<>()
        );
    }

    @Test
    public void verifyView() throws Exception {
        final ModelAndView modelAndView = this.getModelAndViewUponServiceValidationWithSecurePgtUrl();
        final MockHttpServletRequest req = new MockHttpServletRequest(new MockServletContext());
        req.setAttribute(RequestContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE, new GenericWebApplicationContext(req.getServletContext()));
        
        final MockHttpServletResponse resp = new MockHttpServletResponse();
        final View delegatedView = new View() {
            @Override
            public String getContentType() {
                return "text/html";
            }

            @Override
            public void render(final Map<String, ?> map, final HttpServletRequest request, final HttpServletResponse response) {
                map.forEach(request::setAttribute);
            }
        };
        final Cas20ResponseView view = new Cas20ResponseView(true, null,
                null, "attribute", delegatedView, new DefaultAuthenticationAttributeReleasePolicy(),
                new DefaultAuthenticationServiceSelectionPlan());
        view.render(modelAndView.getModel(), req, resp);

        assertNotNull(req.getAttribute(CasViewConstants.MODEL_ATTRIBUTE_NAME_CHAINED_AUTHENTICATIONS));
        assertNotNull(req.getAttribute(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRIMARY_AUTHENTICATION));
        assertNotNull(req.getAttribute(CasViewConstants.MODEL_ATTRIBUTE_NAME_PRINCIPAL));
        assertNotNull(req.getAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_PROXY_GRANTING_TICKET_IOU));
    }
}
