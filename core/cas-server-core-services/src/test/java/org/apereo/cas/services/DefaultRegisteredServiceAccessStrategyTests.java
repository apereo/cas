package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
public class DefaultRegisteredServiceAccessStrategyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "x509CertificateCredential.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

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

    private static Map<String, Object> getPrincipalAttributes() {
        val map = new HashMap<String, Object>();
        map.put(CN, CAS);
        map.put(GIVEN_NAME, Arrays.asList(CAS, KAZ));
        map.put("sn", "surname");
        map.put(PHONE, "123-456-7890");

        return map;
    }

    @Test
    public void checkDefaultImpls() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        assertEquals(0, authz.getOrder());
    }

    @Test
    public void checkDefaultInterfaceImpls() {
        val authz = new RegisteredServiceAccessStrategy() {
            private static final long serialVersionUID = -6993120869616143038L;
        };
        assertEquals(Integer.MAX_VALUE, authz.getOrder());
        assertTrue(authz.isServiceAccessAllowed());
        assertTrue(authz.isServiceAccessAllowedForSso());
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(null, null));
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
    public void checkAuthzPrincipalNoAttrRequirements() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(TEST, new HashMap<>()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsEmptyPrincipal() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(TEST, new HashMap<>()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsAll() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(TEST, getPrincipalAttributes()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsMissingOne() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());

        val pAttrs = getPrincipalAttributes();
        pAttrs.remove(CN);

        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(TEST, pAttrs));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsMissingOneButNotAllNeeded() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());
        authz.setRequireAllAttributes(false);
        val pAttrs = getPrincipalAttributes();
        pAttrs.remove(CN);

        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(TEST, pAttrs));
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
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(TEST, pAttrs));
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
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(TEST, pAttrs));
    }

    @Test
    public void checkRejectedAttributesNotAvailable() {
        val authz = new DefaultRegisteredServiceAccessStrategy();

        val reqs = getRequiredAttributes();
        authz.setRequiredAttributes(reqs);
        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        val pAttrs = getPrincipalAttributes();
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(TEST, pAttrs));
    }

    @Test
    public void checkRejectedAttributesAvailable() {
        val authz = new DefaultRegisteredServiceAccessStrategy();

        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        val pAttrs = getPrincipalAttributes();
        pAttrs.put("address", "1234 Main Street");
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(TEST, pAttrs));
    }

    @Test
    public void checkRejectedAttributesAvailableRequireAll() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequireAllAttributes(true);

        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        val pAttrs = getPrincipalAttributes();
        pAttrs.put("address", "1234 Main Street");
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(TEST, pAttrs));
    }

    @Test
    public void checkRejectedAttributesAvailableRequireAll3() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequireAllAttributes(false);
        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        val pAttrs = getPrincipalAttributes();
        pAttrs.put("role", "nomatch");
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(TEST, pAttrs));
    }

    @Test
    public void checkRejectedAttributesAvailableRequireAll2() {
        val authz = new DefaultRegisteredServiceAccessStrategy();
        authz.setRequireAllAttributes(false);
        val rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        val pAttrs = getPrincipalAttributes();

        pAttrs.put("role", "staff");
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess(TEST, pAttrs));
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
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(TEST, pAttrs));
    }

    @Test
    public void checkAuthzPrincipalWithAttrValuePatternComparison() {
        val authz = new DefaultRegisteredServiceAccessStrategy();

        val reqs = getRequiredAttributes();
        reqs.remove(CN);
        reqs.remove(GIVEN_NAME);

        authz.setRequiredAttributes(reqs);
        val pAttrs = getPrincipalAttributes();

        assertTrue(authz.doPrincipalAttributesAllowServiceAccess(TEST, pAttrs));
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
}
