package org.apereo.cas.services;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.Arrays;
import java.util.Collections;
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
public class DefaultRegisteredServiceAccessStrategyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "DefaultRegisteredServiceAccessStrategyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final String TEST = "test";

    private static final String PHONE = "phone";

    private static final String GIVEN_NAME = "givenName";

    private static final String CAS = "cas";

    private static final String KAZ = "KAZ";

    private static final String CN = "cn";

    private static Map<String, Set<String>> getRequiredAttributes() {
        val map = new HashMap<String, Set<String>>();
        map.put(CN, Stream.of(CAS, "SSO").collect(Collectors.toSet()));
        map.put(GIVEN_NAME, Stream.of("CAS", KAZ).collect(Collectors.toSet()));
        map.put(PHONE, Collections.singleton("\\d\\d\\d-\\d\\d\\d-\\d\\d\\d"));
        return map;
    }

    private static Map<String, Set<String>> getRejectedAttributes() {
        val map = new HashMap<String, Set<String>>();
        map.put("address", Collections.singleton(".+"));
        map.put("role", Collections.singleton("staff"));
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
    public void checkLoad() {
        val authz = new DefaultRegisteredServiceAccessStrategy(getRequiredAttributes(), getRejectedAttributes());
        authz.postLoad();
        assertEquals(0, authz.getOrder());
    }

    @Test
    public void checkDefaultInterfaceImpls() {
        val authz = new RegisteredServiceAccessStrategy() {
            @Serial
            private static final long serialVersionUID = -6993120869616143038L;
        };
        assertEquals(Integer.MAX_VALUE, authz.getOrder());
        assertTrue(authz.isServiceAccessAllowed());
        assertTrue(authz.isServiceAccessAllowedForSso());
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder().build()));
        assertNull(authz.getUnauthorizedRedirectUrl());
    }

    @Test
    public void checkDefaultAuthzStrategyConfig() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        assertTrue(authz.isServiceAccessAllowed());
        assertTrue(authz.isServiceAccessAllowedForSso());
    }

    @Test
    public void checkDisabledAuthzStrategyConfig() {
        val authz = new DefaultRegisteredServiceAccessStrategy(false, true);
        assertFalse(authz.isServiceAccessAllowed());
        assertTrue(authz.isServiceAccessAllowedForSso());
    }

    @Test
    public void checkDisabledSsoAuthzStrategyConfig() {
        val authz = new DefaultRegisteredServiceAccessStrategy(true, false);
        assertTrue(authz.isServiceAccessAllowed());
        assertFalse(authz.isServiceAccessAllowedForSso());
    }

    @Test
    public void setAuthzStrategyConfig() {
        val authz = new DefaultRegisteredServiceAccessStrategy(false, false);
        authz.setEnabled(true);
        authz.setSsoEnabled(true);
        assertTrue(authz.isServiceAccessAllowed());
        assertTrue(authz.isServiceAccessAllowedForSso());
        assertTrue(authz.isRequireAllAttributes());
    }

    @Test
    public void checkAuthzPrincipalInactive() {
        val authz = new DefaultRegisteredServiceAccessStrategy()
            .setActivationCriteria((RegisteredServiceAccessStrategyActivationCriteria) request -> false);
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).build()));
    }

    @Test
    public void checkAuthzPrincipalNoAttrRequirements() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder().principalId(TEST).build()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsEmptyPrincipal() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder().principalId(TEST).build()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsAll() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).attributes(getPrincipalAttributes()).build()));
    }

    @Test
    public void checkAuthzWithAttributeRequirementAsGroovy() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        val required = new HashMap<String, Set<String>>();
        required.put(CN, Set.of("groovy { return attributes.containsKey('name') && currentValues.contains('admin') }"));
        authz.setRequiredAttributes(required);
        var request = RegisteredServiceAccessStrategyRequest.builder()
            .attributes(Map.of(PHONE, List.of("1234567890")))
            .principalId(TEST).build();
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(request));
        request = request.withAttributes(Map.of(CN, List.of("admin"), "name", List.of("casuser")));
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(request));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsMissingOne() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());
        val pAttrs = getPrincipalAttributes();
        pAttrs.remove(CN);
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsMissingOneButNotAllNeeded() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());
        authz.setRequireAllAttributes(false);
        val pAttrs = getPrincipalAttributes();
        pAttrs.remove(CN);

        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsNoValueMatch() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        val reqs = getRequiredAttributes();
        reqs.remove(PHONE);
        authz.setRequiredAttributes(reqs);
        authz.setRequireAllAttributes(false);
        val pAttrs = getPrincipalAttributes();
        pAttrs.remove(CN);
        pAttrs.put(GIVEN_NAME, "theName");
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).attributes(pAttrs).build()));
    }

    /**
     * It is important that the non-matching attribute is not the first one, due to the
     * use of anyMatch and allMatch in the access strategy.
     */
    @Test
    public void checkAuthzPrincipalWithAttrRequirementsWrongValue() {
        val reqAttrs = getRequiredAttributes();
        reqAttrs.put(GIVEN_NAME, Collections.singleton("not present"));

        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequireAllAttributes(true);
        authz.setRequiredAttributes(reqAttrs);

        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).attributes(getPrincipalAttributes()).build()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrValueCaseSensitiveComparison() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        val reqs = getRequiredAttributes();
        reqs.remove(PHONE);
        authz.setRequiredAttributes(reqs);
        val pAttrs = getPrincipalAttributes();
        pAttrs.put(CN, "CAS");
        pAttrs.put(GIVEN_NAME, "kaz");
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    public void checkRejectedAttributesNotAvailable() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        val reqs = getRequiredAttributes();
        authz.setRequiredAttributes(reqs);
        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        val pAttrs = getPrincipalAttributes();
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    public void checkRejectedAttributesAvailable() {
        val authz = new DefaultRegisteredServiceAccessStrategy();

        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        val pAttrs = getPrincipalAttributes();
        pAttrs.put("address", "1234 Main Street");
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    public void checkRejectedAttributesAvailableRequireAll() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequireAllAttributes(true);

        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        val pAttrs = getPrincipalAttributes();
        pAttrs.put("address", "1234 Main Street");
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    public void checkRejectedAttributesAvailableRequireAll3() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequireAllAttributes(false);
        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);
        val pAttrs = getPrincipalAttributes();
        pAttrs.put("role", "nomatch");
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    public void checkRejectedAttributesAvailableRequireAll2() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequireAllAttributes(false);
        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);
        val pAttrs = getPrincipalAttributes();
        pAttrs.put("role", "staff");
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrValueCaseInsensitiveComparison() {
        val authz = new DefaultRegisteredServiceAccessStrategy();

        val reqs = getRequiredAttributes();
        authz.setRequiredAttributes(reqs);

        val pAttrs = getPrincipalAttributes();
        authz.setCaseInsensitive(true);

        pAttrs.put(CN, CAS);
        pAttrs.put(GIVEN_NAME, "kaz");
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrValuePatternComparison() {
        val authz = new DefaultRegisteredServiceAccessStrategy();

        val reqs = getRequiredAttributes();
        reqs.remove(CN);
        reqs.remove(GIVEN_NAME);

        authz.setRequiredAttributes(reqs);
        val pAttrs = getPrincipalAttributes();

        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).attributes(pAttrs).build()));
    }

    @Test
    public void verifySerializeADefaultRegisteredServiceAccessStrategyToJson() throws IOException {
        val strategyWritten = new DefaultRegisteredServiceAccessStrategy();

        val reqs = getRequiredAttributes();
        reqs.remove(CN);
        reqs.remove(GIVEN_NAME);

        strategyWritten.setRequiredAttributes(reqs);
        strategyWritten.setRejectedAttributes(getRejectedAttributes());

        MAPPER.writeValue(JSON_FILE, strategyWritten);
        val strategyRead = MAPPER.readValue(JSON_FILE, DefaultRegisteredServiceAccessStrategy.class);
        assertEquals(strategyWritten, strategyRead);
    }

    @Test
    public void verifyRejectedAttributesMoreThanPrincipal() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRejectedAttributes(getRejectedAttributes());
        authz.setRequiredAttributes(Map.of(CN, Set.of(CAS)));
        val request = RegisteredServiceAccessStrategyRequest.builder()
            .principalId(TEST).attributes(Map.of(CN, List.of(CAS))).build();
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(request));
    }
}
