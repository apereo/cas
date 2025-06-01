package org.apereo.cas.services;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is test cases for
 * {@link DefaultRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("RegisteredService")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class DefaultRegisteredServiceAccessStrategyTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final String TEST = "test";

    private static final String PHONE = "phone";

    private static final String GIVEN_NAME = "givenName";

    private static final String CAS = "cas";

    private static final String KAZ = "KAZ";

    private static final String CN = "cn";

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    private static Map<String, Set<String>> getRequiredAttributes() {
        val map = new HashMap<String, Set<String>>();
        map.put(CN, Stream.of(CAS, "SSO").collect(Collectors.toSet()));
        map.put(GIVEN_NAME, Stream.of("CAS", KAZ).collect(Collectors.toSet()));
        map.put(PHONE, Set.of("\\d\\d\\d-\\d\\d\\d-\\d\\d\\d"));
        return map;
    }

    private static Map<String, Set<String>> getRejectedAttributes() {
        val map = new HashMap<String, Set<String>>();
        map.put("address", Set.of(".+"));
        map.put("role", Set.of("staff"));
        return map;
    }

    private static Map getPrincipalAttributes() {
        val map = new HashMap<String, Object>();
        map.put(CN, CAS);
        map.put(GIVEN_NAME, Arrays.asList(CAS, KAZ));
        map.put("sn", "surname");
        map.put(PHONE, "123-456-7890");
        return map;
    }

    @Test
    void checkLoad() {
        val authz = new DefaultRegisteredServiceAccessStrategy(getRequiredAttributes(), getRejectedAttributes());
        authz.postLoad();
        assertEquals(0, authz.getOrder());
    }

    @Test
    void checkDefaultInterfaceImpls() throws Throwable {
        val authz = new RegisteredServiceAccessStrategy() {
            @Serial
            private static final long serialVersionUID = -6993120869616143038L;
        };
        assertEquals(Integer.MAX_VALUE, authz.getOrder());
        assertTrue(authz.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), RegisteredServiceTestUtils.getService2()));
        assertTrue(authz.isServiceAccessAllowedForSso(RegisteredServiceTestUtils.getRegisteredService()));
        assertTrue(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext).applicationContext(applicationContext).build()));
        assertNull(authz.getUnauthorizedRedirectUrl());
    }

    @Test
    void checkDefaultAuthzStrategyConfig() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        assertTrue(authz.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), RegisteredServiceTestUtils.getService2()));
        assertTrue(authz.isServiceAccessAllowedForSso(RegisteredServiceTestUtils.getRegisteredService()));
    }

    @Test
    void checkDisabledAuthzStrategyConfig() {
        val authz = new DefaultRegisteredServiceAccessStrategy(false, true);
        assertFalse(authz.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), RegisteredServiceTestUtils.getService2()));
        assertTrue(authz.isServiceAccessAllowedForSso(RegisteredServiceTestUtils.getRegisteredService()));
    }

    @Test
    void checkDisabledSsoAuthzStrategyConfig() {
        val authz = new DefaultRegisteredServiceAccessStrategy(true, false);
        assertTrue(authz.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), RegisteredServiceTestUtils.getService2()));
        assertFalse(authz.isServiceAccessAllowedForSso(RegisteredServiceTestUtils.getRegisteredService()));
    }

    @Test
    void setAuthzStrategyConfig() {
        val authz = new DefaultRegisteredServiceAccessStrategy(false, false);
        authz.setEnabled(true);
        authz.setSsoEnabled(true);
        assertTrue(authz.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), RegisteredServiceTestUtils.getService2()));
        assertTrue(authz.isServiceAccessAllowedForSso(RegisteredServiceTestUtils.getRegisteredService()));
        assertTrue(authz.isRequireAllAttributes());
    }

    @Test
    void checkAuthzPrincipalInactive() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy()
            .setActivationCriteria((RegisteredServiceAccessStrategyActivationCriteria) request -> false);
        assertTrue(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).build()));
    }

    @Test
    void checkAuthzPrincipalNoAttrRequirements() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        assertTrue(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext).principalId(TEST).build()));
    }

    @Test
    void checkAuthzPrincipalWithAttrRequirementsEmptyPrincipal() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());
        assertFalse(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext).principalId(TEST).build()));
    }

    @Test
    void checkAuthzPrincipalWithAttrRequirementsAll() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());
        assertTrue(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).attributes(getPrincipalAttributes()).build()));
    }

    @Test
    void checkAuthzWithAttributeRequirementAsGroovy() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        val required = new HashMap<String, Set<String>>();
        required.put(CN, Set.of("groovy { return attributes.containsKey('name') && currentValues.contains('admin') }"));
        authz.setRequiredAttributes(required);
        var request = RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .attributes(Map.of(PHONE, List.of("1234567890")))
            .principalId(TEST).build();
        assertFalse(authz.authorizeRequest(request));
        request = request.withAttributes(Map.of(CN, List.of("admin"), "name", List.of("casuser")));
        assertTrue(authz.authorizeRequest(request));
    }

    @Test
    void checkAuthzPrincipalWithAttrRequirementsMissingOne() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());
        val pAttrs = getPrincipalAttributes();
        pAttrs.remove(CN);
        assertFalse(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    void checkAuthzPrincipalWithAttrRequirementsMissingOneButNotAllNeeded() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());
        authz.setRequireAllAttributes(false);
        val pAttrs = getPrincipalAttributes();
        pAttrs.remove(CN);

        assertTrue(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    void checkAuthzPrincipalWithAttrRequirementsNoValueMatch() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        val reqs = getRequiredAttributes();
        reqs.remove(PHONE);
        authz.setRequiredAttributes(reqs);
        authz.setRequireAllAttributes(false);
        val pAttrs = getPrincipalAttributes();
        pAttrs.remove(CN);
        pAttrs.put(GIVEN_NAME, "theName");
        assertFalse(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).attributes(pAttrs).build()));
    }

    /**
     * It is important that the non-matching attribute is not the first one, due to the
     * use of anyMatch and allMatch in the access strategy.
     */
    @Test
    void checkAuthzPrincipalWithAttrRequirementsWrongValue() throws Throwable {
        val reqAttrs = getRequiredAttributes();
        reqAttrs.put(GIVEN_NAME, Set.of("not present"));

        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequireAllAttributes(true);
        authz.setRequiredAttributes(reqAttrs);

        assertFalse(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).attributes(getPrincipalAttributes()).build()));
    }

    @Test
    void checkAuthzPrincipalWithAttrValueCaseSensitiveComparison() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        val reqs = getRequiredAttributes();
        reqs.remove(PHONE);
        authz.setRequiredAttributes(reqs);
        val pAttrs = getPrincipalAttributes();
        pAttrs.put(CN, "CAS");
        pAttrs.put(GIVEN_NAME, "kaz");
        assertFalse(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    void checkRejectedAttributesNotAvailable() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        val reqs = getRequiredAttributes();
        authz.setRequiredAttributes(reqs);
        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        val pAttrs = getPrincipalAttributes();
        assertTrue(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    void checkRejectedAttributesAvailable() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();

        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        val pAttrs = getPrincipalAttributes();
        pAttrs.put("address", "1234 Main Street");
        assertTrue(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    void checkRejectedAttributesAvailableRequireAll() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequireAllAttributes(true);

        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        val pAttrs = getPrincipalAttributes();
        pAttrs.put("address", "1234 Main Street");
        assertTrue(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    void checkRejectedAttributesAvailableRequireAll3() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequireAllAttributes(false);
        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);
        val pAttrs = getPrincipalAttributes();
        pAttrs.put("role", "nomatch");
        assertTrue(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    void checkRejectedAttributesAvailableRequireAll2() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequireAllAttributes(false);
        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);
        val pAttrs = getPrincipalAttributes();
        pAttrs.put("role", "staff");
        assertFalse(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    void checkAuthzPrincipalWithAttrValueCaseInsensitiveComparison() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();

        val reqs = getRequiredAttributes();
        authz.setRequiredAttributes(reqs);

        val pAttrs = getPrincipalAttributes();
        authz.setCaseInsensitive(true);

        pAttrs.put(CN, CAS);
        pAttrs.put(GIVEN_NAME, "kaz");
        assertTrue(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    void checkAuthzPrincipalWithAttrValuePatternComparison() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();

        val reqs = getRequiredAttributes();
        reqs.remove(CN);
        reqs.remove(GIVEN_NAME);

        authz.setRequiredAttributes(reqs);
        val pAttrs = getPrincipalAttributes();

        assertTrue(authz.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    void verifySerializeADefaultRegisteredServiceAccessStrategyToJson() throws IOException {
        val strategyWritten = new DefaultRegisteredServiceAccessStrategy();

        val reqs = getRequiredAttributes();
        reqs.remove(CN);
        reqs.remove(GIVEN_NAME);

        strategyWritten.setRequiredAttributes(reqs);
        strategyWritten.setRejectedAttributes(getRejectedAttributes());

        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        MAPPER.writeValue(jsonFile, strategyWritten);
        val strategyRead = MAPPER.readValue(jsonFile, DefaultRegisteredServiceAccessStrategy.class);
        assertEquals(strategyWritten, strategyRead);
    }

    @Test
    void verifyRejectedAttributesMoreThanPrincipal() throws Throwable {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRejectedAttributes(getRejectedAttributes());
        authz.setRequiredAttributes(Map.of(CN, Set.of(CAS)));
        val request = RegisteredServiceAccessStrategyRequest.builder().applicationContext(applicationContext)
            .principalId(TEST).attributes(Map.of(CN, List.of(CAS))).build();
        assertFalse(authz.authorizeRequest(request));
    }
}
