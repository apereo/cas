package org.apereo.cas.web.view;

import org.apereo.cas.CasViewConstants;
import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.support.NoOpProtocolAttributeEncoder;
import org.apereo.cas.services.web.view.AbstractCasView;
import org.apereo.cas.web.view.attributes.DefaultCas30ProtocolAttributesRenderer;
import org.apereo.cas.web.view.json.Cas30JsonResponseView;
import org.apereo.cas.web.view.json.CasJsonServiceResponse;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.View;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Cas30ResponseView}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Tag("CAS")
public class Cas30JsonResponseViewTests extends Cas30ResponseViewTests {
    @Override
    protected AbstractCasView getCasViewToRender(final ProtocolAttributeEncoder encoder, final View viewDelegated) {
        return getCasView(true, encoder, viewDelegated);
    }

    private Cas30JsonResponseView getCasView(final boolean success, final ProtocolAttributeEncoder encoder, final View viewDelegated) {
        return new Cas30JsonResponseView(success,
            encoder,
            servicesManager,
            viewDelegated,
            new DefaultAuthenticationAttributeReleasePolicy("attribute"),
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultCas30ProtocolAttributesRenderer());
    }

    @Override
    protected Map getRenderedViewModelMap(final MockHttpServletRequest req) {
        val response = (CasJsonServiceResponse) req.getAttribute(Cas30JsonResponseView.ATTRIBUTE_NAME_MODEL_SERVICE_RESPONSE);
        assertNotNull(response, "Response cannot be null");
        val success = response.getAuthenticationSuccess();
        assertNotNull(response, "Authentication success cannot be null");
        return success.getAttributes();
    }

    @Test
    public void verifyFailureView() throws Exception {
        val response = new MockHttpServletResponse();
        val view = getCasView(false, new NoOpProtocolAttributeEncoder(), getDelegatedView());
        val model = new HashMap<String, Object>();
        model.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_CODE, "code");
        model.put(CasViewConstants.MODEL_ATTRIBUTE_NAME_ERROR_DESCRIPTION, "description");
        val request = new MockHttpServletRequest();
        view.render(model, request, response);
        val casResponse = (CasJsonServiceResponse) request.getAttribute(Cas30JsonResponseView.ATTRIBUTE_NAME_MODEL_SERVICE_RESPONSE);
        assertNotNull(casResponse.getAuthenticationFailure().getCode());
        assertNotNull(casResponse.getAuthenticationFailure().getDescription());
    }
}
