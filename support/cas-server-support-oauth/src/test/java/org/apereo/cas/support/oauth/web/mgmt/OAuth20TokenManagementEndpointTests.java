package org.apereo.cas.support.oauth.web.mgmt;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.spring.DirectObjectProvider;
import lombok.val;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link OAuth20TokenManagementEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "management.endpoint.oauthTokens.access=UNRESTRICTED",
    "management.endpoints.web.exposure.include=*"
})
@Tag("OAuthWeb")
class OAuth20TokenManagementEndpointTests extends AbstractOAuth20Tests {
    @Test
    void verifyOperationWithJwt() throws Throwable {
        val registeredService = getRegisteredService("example1", "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);
        
        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        mockMvc.perform(get("/actuator/oauthTokens/{token}", at)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists());

        mockMvc.perform(get("/actuator/oauthTokens")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()", Matchers.greaterThan(0)));

        mockMvc.perform(delete("/actuator/oauthTokens/{token}", at)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful());
    }

    @Test
    void verifyOperation() throws Throwable {
        val registeredService = getRegisteredService("example2", "secret", new LinkedHashSet<>());
        servicesManager.save(registeredService);

        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        mockMvc.perform(get("/actuator/oauthTokens/{token}", at)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void verifyTokensAreScannedInTicketTransaction() {
        val inTransaction = new AtomicBoolean();
        val scans = new ArrayList<Boolean>();
        val registry = mock(TicketRegistry.class);
        when(registry.getTickets(ArgumentMatchers.<Predicate<Ticket>>any())).thenAnswer(_ -> {
            scans.add(inTransaction.get());
            return Stream.empty();
        });
        val ticketTransactionOperations = new TransactionOperations() {
            @Override
            public <T> T execute(final TransactionCallback<T> action) {
                inTransaction.set(true);
                try {
                    return action.doInTransaction(new SimpleTransactionStatus());
                } finally {
                    inTransaction.set(false);
                }
            }
        };
        val endpoint = new OAuth20TokenManagementEndpoint(casProperties, new DirectObjectProvider<>(registry),
            new DirectObjectProvider<>(mock(JwtBuilder.class)), ticketTransactionOperations);
        assertTrue(endpoint.getTokens().isEmpty());
        assertEquals(List.of(true), scans);
    }

    @Test
    void verifyBadOperation() throws Throwable {
        mockMvc.perform(get("/actuator/oauthTokens/{token}", "unknown")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
