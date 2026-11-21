package org.apereo.cas.web.report;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.AttributeBasedRegisteredServiceAccessStrategyActivationCriteria;
import org.apereo.cas.services.BaseRegisteredServiceAccessStrategy;
import org.apereo.cas.services.ChainingRegisteredServiceAccessStrategy;
import org.apereo.cas.services.ChainingRegisteredServiceAccessStrategyActivationCriteria;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.LogicalOperatorTypes;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyActivationCriteria;
import org.apereo.cas.services.RegisteredServiceAccessStrategyRequest;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link RegisteredServiceAccessEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("ActuatorEndpoint")
@TestPropertySource(properties = {
    "cas.authn.attribute-repository.stub.attributes.uid=cas",
    "cas.authn.attribute-repository.stub.attributes.givenName=apereo-cas",
    "cas.authn.attribute-repository.stub.attributes.phone=123456789",
    "management.endpoint.serviceAccess.access=UNRESTRICTED",
    "cas.monitor.endpoints.endpoint.defaults.access=ANONYMOUS"
})
class RegisteredServiceAccessEndpointTests extends AbstractCasEndpointTests {
    @Test
    void verifyForbiddenOperation() throws Throwable {
        mockMvc.perform(post("/actuator/serviceAccess")
            .param("service", "https://unknown.example.edu")
            .param("username", "casuser")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @Test
    void verifyUnknownApplicationIsExplained() throws Throwable {
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("service", "https://nowhere.example.edu")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.allowed").value(false))
            .andExpect(jsonPath("$.steps[0].id").value("application"))
            .andExpect(jsonPath("$.steps[0].outcome").value("FAILED"))
            .andExpect(jsonPath("$.reason", containsString("nowhere.example.edu")));
    }

    @Test
    void verifyAllowedAccessIsExplained() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://explained.example.edu");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setRequiredAttributes(Map.of("phone", Set.of("123456789"))));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("service", "https://explained.example.edu")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allowed").value(true))
            .andExpect(jsonPath("$.steps[*].id", hasItems("application", "enabled", "expiration", "sso", "authentication", "required", "rejected")))
            .andExpect(jsonPath("$.steps[?(@.id == 'required')].outcome", hasItem("PASSED")))
            .andExpect(jsonPath("$.steps[?(@.id == 'required')].rules[0].matched", hasItem(true)))
            .andExpect(jsonPath("$.steps[?(@.id == 'rejected')].outcome", hasItem("SKIPPED")))
            .andExpect(jsonPath("$.registeredService.name").value(registeredService.getName()))
            .andExpect(jsonPath("$.registeredService.accessStrategy").doesNotExist())
            .andExpect(jsonPath("$.authentication").exists());
    }

    @Test
    void verifyMissingRequiredAttributeIsExplained() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://required.example.edu");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setRequiredAttributes(Map.of("phone", Set.of("123456789"), "memberOf", Set.of("admins"))));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("service", "https://required.example.edu")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.allowed").value(false))
            .andExpect(jsonPath("$.reason", startsWith("Required Attributes: memberOf needs admins, and has none")))
            .andExpect(jsonPath("$.steps[?(@.id == 'required')].outcome", hasItem("FAILED")))
            .andExpect(jsonPath("$.steps[?(@.id == 'required')].rules[0].attribute", hasItem("memberOf")))
            .andExpect(jsonPath("$.steps[?(@.id == 'required')].rules[0].matched", hasItem(false)))
            .andExpect(jsonPath("$.steps[?(@.id == 'required')].rules[1].matched", hasItem(true)))
            .andExpect(jsonPath("$.authentication").exists());
    }

    @Test
    void verifyRejectedAttributeIsExplained() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://rejected.example.edu");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setRejectedAttributes(Map.of("phone", Set.of("1234.+"))));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("service", "https://rejected.example.edu")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.steps[?(@.id == 'rejected')].outcome", hasItem("FAILED")))
            .andExpect(jsonPath("$.steps[?(@.id == 'rejected')].rules[0].matched", hasItem(true)));
    }

    @Test
    void verifyDisabledApplicationIsExplained() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://disabled.example.edu");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, true));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("service", "https://disabled.example.edu")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.steps[?(@.id == 'enabled')].outcome", hasItem("FAILED")))
            .andExpect(jsonPath("$.steps[*].id", not(hasItem("authentication"))));
    }

    @Test
    void verifyChainedStrategiesAreExplained() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://chained.example.edu");
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(LogicalOperatorTypes.OR);
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy().setRequiredAttributes(Map.of("memberOf", Set.of("admins"))));
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy().setRequiredAttributes(Map.of("phone", Set.of("123456789"))));
        registeredService.setAccessStrategy(chain);
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("service", "https://chained.example.edu")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.steps[?(@.id == 'chain')].outcome", hasItem("PASSED")))
            .andExpect(jsonPath("$.steps[?(@.id == 'chain')].steps[0].outcome", hasItem("FAILED")))
            .andExpect(jsonPath("$.steps[?(@.id == 'chain')].steps[1].outcome", hasItem("PASSED")));
    }

    @Test
    void verifyHead() throws Throwable {
        mockMvc.perform(head("/actuator/serviceAccess")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void verifyAccessAllowedOperation() throws Exception {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://canvas.example.edu");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setRequiredAttributes(Map.of("phone", Set.of("123456789"))));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
            .param("service", "https://canvas.example.edu")
            .param("username", "casuser")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void verifyAccessWithPassword() throws Exception {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://psw.example.edu");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setRequiredAttributes(Map.of("givenName", Set.of("apereo.+"))));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
            .param("service", "https://psw.example.edu")
            .param("username", "casuser")
            .param("password", "casuser")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void verifyAccessWithInvalidPassword() throws Exception {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://valid.example.edu");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setRequiredAttributes(Map.of("givenName", Set.of("apereo.+"))));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
            .param("service", "https://valid.example.edu")
            .param("username", "casuser")
            .param("password", "bad-password")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void verifyClientIdAloneNeverSelectsCasApplication() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(".+/oauth2\\.0/callbackAuthorize");
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("client_id", "client-alone")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.service.id", endsWith("/oauth2.0/callbackAuthorize")))
            .andExpect(jsonPath("$.reason").value("No application matches client id client-alone"));
    }

    @Test
    void verifyUnknownEntityIdIsNamedInTheReason() throws Throwable {
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("entityId", "https://unknown-sp.example.edu/shibboleth")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.reason").value("No application matches entity id https://unknown-sp.example.edu/shibboleth"));
    }

    @Test
    void verifyEntityIdAloneNeverSelectsCasApplication() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://sp.alone.example.edu/shibboleth");
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("entityId", "https://sp.alone.example.edu/shibboleth")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.service.id").value("https://sp.alone.example.edu/shibboleth"))
            .andExpect(jsonPath("$.reason").value("No application matches entity id https://sp.alone.example.edu/shibboleth"));
    }

    @Test
    void verifyRefusedCheckSummarizesTheApplication() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://summary.example.edu");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setRequiredAttributes(Map.of("memberOf", Set.of("admins"))));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("service", "https://summary.example.edu")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.registeredService.name").value(registeredService.getName()))
            .andExpect(jsonPath("$.registeredService.accessStrategy").doesNotExist())
            .andExpect(jsonPath("$.service.attributes").doesNotExist());
    }

    @Test
    void verifyOutsideStrategiesAreAskedOnce() throws Throwable {
        CountingAccessStrategy.CALLS.set(0);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://counted.example.edu");
        registeredService.setAccessStrategy(new CountingAccessStrategy());
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("service", "https://counted.example.edu")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.steps[?(@.title == 'Decided by Counting')].outcome", hasItem("FAILED")));
        assertEquals(1, CountingAccessStrategy.CALLS.get());

        CountingAccessStrategy.CALLS.set(0);
        val chained = RegisteredServiceTestUtils.getRegisteredService("https://counted-chain.example.edu");
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(LogicalOperatorTypes.OR);
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy().setRequiredAttributes(Map.of("memberOf", Set.of("admins"))));
        chain.addStrategy(new CountingAccessStrategy());
        chained.setAccessStrategy(chain);
        servicesManager.save(chained);
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("service", "https://counted-chain.example.edu")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.steps[?(@.id == 'chain')].outcome", hasItem("FAILED")))
            .andExpect(jsonPath("$.steps[?(@.id == 'chain')].steps[1].outcome", hasItem("INFO")));
        assertEquals(1, CountingAccessStrategy.CALLS.get());
    }

    @Test
    void verifyActivationNotExplainedLocallyIsAskedOnce() throws Throwable {
        CountingActivationCriteria.CALLS.set(0);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://counted-activation.example.edu");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setRequiredAttributes(Map.of("memberOf", Set.of("admins")))
            .setActivationCriteria(new CountingActivationCriteria()));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("service", "https://counted-activation.example.edu")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.steps[?(@.title == 'Decided by Default')].outcome", hasItem("FAILED")));
        assertEquals(1, CountingActivationCriteria.CALLS.get());
    }

    @Test
    void verifyFailureToCheckIsNotADecision() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://broken.example.edu");
        registeredService.setAccessStrategy(new BrokenAccessStrategy());
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("service", "https://broken.example.edu")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.allowed").value(false))
            .andExpect(jsonPath("$.reason", startsWith("Access could not be checked")));
    }

    @Test
    void verifyChainedActivationIsExplainedPerCondition() throws Throwable {
        val activation = new ChainingRegisteredServiceAccessStrategyActivationCriteria();
        activation.setOperator(LogicalOperatorTypes.OR);
        activation.addConditions(
            new AttributeBasedRegisteredServiceAccessStrategyActivationCriteria().setRequiredAttributes(Map.of("memberOf", List.of("admins"))),
            new AttributeBasedRegisteredServiceAccessStrategyActivationCriteria().setRequiredAttributes(Map.of("uid", List.of("cas"))));
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://chained-activation.example.edu");
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy()
            .setRequiredAttributes(Map.of("phone", Set.of("123456789")))
            .setActivationCriteria(activation));
        servicesManager.save(registeredService);
        mockMvc.perform(post("/actuator/serviceAccess")
                .param("service", "https://chained-activation.example.edu")
                .param("username", "casuser")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.steps[?(@.id == 'activation')].outcome", hasItem("PASSED")))
            .andExpect(jsonPath("$.steps[?(@.id == 'activation')].detail", hasItem(startsWith("Any one of 2 conditions must hold"))))
            .andExpect(jsonPath("$.steps[?(@.id == 'activation')].steps[0].outcome", hasItem("FAILED")))
            .andExpect(jsonPath("$.steps[?(@.id == 'activation')].steps[1].outcome", hasItem("PASSED")))
            .andExpect(jsonPath("$.steps[?(@.id == 'activation')].steps[1].title", hasItem("Attributes: uid")));
    }

    static class BrokenAccessStrategy extends BaseRegisteredServiceAccessStrategy {
        @Serial
        private static final long serialVersionUID = 6619082316404720881L;

        @Override
        public boolean isServiceAccessAllowed(final RegisteredService registeredService, @Nullable final Service service) {
            throw new IllegalStateException("The strategy is broken");
        }
    }

    static class CountingActivationCriteria implements RegisteredServiceAccessStrategyActivationCriteria {
        static final AtomicInteger CALLS = new AtomicInteger();

        @Serial
        private static final long serialVersionUID = 1726304815563140197L;

        @Override
        public boolean shouldActivate(final RegisteredServiceAccessStrategyRequest request) {
            CALLS.incrementAndGet();
            return true;
        }
    }

    static class CountingAccessStrategy extends BaseRegisteredServiceAccessStrategy {
        static final AtomicInteger CALLS = new AtomicInteger();

        @Serial
        private static final long serialVersionUID = 4471395219263474322L;

        @Override
        public boolean authorizeRequest(final RegisteredServiceAccessStrategyRequest request) {
            CALLS.incrementAndGet();
            return false;
        }
    }
}
