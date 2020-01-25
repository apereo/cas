package org.apereo.cas.web.flow;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingSingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Webflow")
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
        assertFalse(chain.isParticipating(context));
    }

    @Test
    public void verifyVotesYesInChain() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val chain = new ChainingSingleSignOnParticipationStrategy();
        chain.addStrategy(SingleSignOnParticipationStrategy.alwaysParticipating());
        assertTrue(chain.isParticipating(context));
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
            public boolean isParticipating(final RequestContext context) {
                return true;
            }

            @Override
            public boolean supports(final RequestContext context) {
                return false;
            }
        });
        chain.addStrategy(SingleSignOnParticipationStrategy.neverParticipating());
        assertFalse(chain.isParticipating(context));
    }
}
