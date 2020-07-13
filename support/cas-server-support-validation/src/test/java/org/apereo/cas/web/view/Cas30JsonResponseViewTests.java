package org.apereo.cas.web.view;

import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.web.view.AbstractCasView;
import org.apereo.cas.web.view.attributes.DefaultCas30ProtocolAttributesRenderer;
import org.apereo.cas.web.view.json.Cas30JsonResponseView;
import org.apereo.cas.web.view.json.CasJsonServiceResponse;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.servlet.View;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Cas30ResponseView}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@DirtiesContext
@Tag("Simple")
public class Cas30JsonResponseViewTests extends Cas30ResponseViewTests {
    @Override
    protected AbstractCasView getCasViewToRender(final ProtocolAttributeEncoder encoder, final View viewDelegated) {
        return new Cas30JsonResponseView(true,
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
}
