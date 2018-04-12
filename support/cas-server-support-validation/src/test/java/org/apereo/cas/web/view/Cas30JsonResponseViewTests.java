package org.apereo.cas.web.view;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicy;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.web.view.AbstractCasView;
import org.apereo.cas.web.view.attributes.DefaultCas30ProtocolAttributesRenderer;
import org.apereo.cas.web.view.json.Cas30JsonResponseView;
import org.apereo.cas.web.view.json.CasJsonServiceResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.servlet.View;

import java.util.Map;

/**
 * Unit tests for {@link Cas30ResponseView}.
 *
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@DirtiesContext
@Slf4j
public class Cas30JsonResponseViewTests extends Cas30ResponseViewTests {
    @Override
    protected AbstractCasView getCasViewToRender(final ProtocolAttributeEncoder encoder, final View viewDelegated) {
        return new Cas30JsonResponseView(true,
            encoder,
            servicesManager,
            "attribute",
            viewDelegated,
            true,
            new DefaultAuthenticationAttributeReleasePolicy(),
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            new DefaultCas30ProtocolAttributesRenderer());
    }

    @Override
    protected Map getRenderedViewModelMap(final MockHttpServletRequest req) {
        final var response = (CasJsonServiceResponse)
            req.getAttribute(Cas30JsonResponseView.ATTRIBUTE_NAME_MODEL_SERVICE_RESPONSE);
        return response.getAuthenticationSuccess().getAttributes();
    }
}
