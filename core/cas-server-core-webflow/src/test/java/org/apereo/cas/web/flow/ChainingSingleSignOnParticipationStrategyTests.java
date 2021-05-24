package org.apereo.cas.web.flow;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingSingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Authentication")
public class ChainingSingleSignOnParticipationStrategyTests {
    @Test
    public void verifyVotesNoInChain() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val chain = new ChainingSingleSignOnParticipationStrategy();
        chain.addStrategy(SingleSignOnParticipationStrategy.alwaysParticipating());
        chain.addStrategy(SingleSignOnParticipationStrategy.neverParticipating());

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertFalse(chain.isParticipating(ssoRequest));
    }

    @Test
    public void verifyEmptyChain() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val chain = new ChainingSingleSignOnParticipationStrategy();
        chain.addStrategy(List.of());

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertTrue(chain.isParticipating(ssoRequest));
    }

    @Test
    public void verifyVotesYesInChain() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val chain = new ChainingSingleSignOnParticipationStrategy();
        chain.addStrategy(SingleSignOnParticipationStrategy.alwaysParticipating());

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertTrue(chain.isParticipating(ssoRequest));
    }

    @Test
    public void verifyVotesNoInChainWithoutSupport() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val chain = new ChainingSingleSignOnParticipationStrategy();
        chain.addStrategy(new SingleSignOnParticipationStrategy() {
            @Override
            public boolean isParticipating(final SingleSignOnParticipationRequest ssoRequest) {
                return true;
            }

            @Override
            public boolean supports(final SingleSignOnParticipationRequest ssoRequest) {
                return false;
            }
        });
        chain.addStrategy(SingleSignOnParticipationStrategy.neverParticipating());

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertFalse(chain.isParticipating(ssoRequest));
    }
}
