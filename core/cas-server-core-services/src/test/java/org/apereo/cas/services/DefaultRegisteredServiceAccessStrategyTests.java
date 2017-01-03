package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * This is test cases for
 * {@link DefaultRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1
 */
public class DefaultRegisteredServiceAccessStrategyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "x509CertificateCredential.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
     public void checkDefaultAuthzStrategyConfig() {
        final RegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        assertTrue(authz.isServiceAccessAllowed());
        assertTrue(authz.isServiceAccessAllowedForSso());
    }

    @Test
    public void checkDisabledAuthzStrategyConfig() {
        final RegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy(false, true);
        assertFalse(authz.isServiceAccessAllowed());
        assertTrue(authz.isServiceAccessAllowedForSso());
    }

    @Test
    public void checkDisabledSsoAuthzStrategyConfig() {
        final RegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy(true, false);
        assertTrue(authz.isServiceAccessAllowed());
        assertFalse(authz.isServiceAccessAllowedForSso());
    }

    @Test
    public void setAuthzStrategyConfig() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy(false, false);
        authz.setEnabled(true);
        authz.setSsoEnabled(true);
        assertTrue(authz.isServiceAccessAllowed());
        assertTrue(authz.isServiceAccessAllowedForSso());
        assertTrue(authz.isRequireAllAttributes());
    }

    @Test
    public void checkAuthzPrincipalNoAttrRequirements() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess("test", new HashMap<>()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsEmptyPrincipal() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess("test", new HashMap<>()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsAll() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess("test",
                getPrincipalAttributes()));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsMissingOne() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());

        final Map<String, Object> pAttrs = getPrincipalAttributes();
        pAttrs.remove("cn");

        assertFalse(authz.doPrincipalAttributesAllowServiceAccess("test", pAttrs));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsMissingOneButNotAllNeeded() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        authz.setRequiredAttributes(getRequiredAttributes());
        authz.setRequireAllAttributes(false);
        final Map<String, Object> pAttrs = getPrincipalAttributes();
        pAttrs.remove("cn");

        assertTrue(authz.doPrincipalAttributesAllowServiceAccess("test", pAttrs));
    }

    @Test
    public void checkAuthzPrincipalWithAttrRequirementsNoValueMatch() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        final Map<String, Set<String>> reqs = getRequiredAttributes();
        reqs.remove("phone");
        authz.setRequiredAttributes(reqs);
        authz.setRequireAllAttributes(false);
        final Map<String, Object> pAttrs = getPrincipalAttributes();
        pAttrs.remove("cn");
        pAttrs.put("givenName", "theName");
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess("test", pAttrs));
    }

    @Test
    public void checkAuthzPrincipalWithAttrValueCaseSensitiveComparison() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();

        final Map<String, Set<String>> reqs = getRequiredAttributes();
        reqs.remove("phone");
        authz.setRequiredAttributes(reqs);

        final Map<String, Object> pAttrs = getPrincipalAttributes();
        pAttrs.put("cn", "CAS");
        pAttrs.put("givenName", "kaz");
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess("test", pAttrs));
    }

    @Test
    public void checkRejectedAttributesNotAvailable() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();

        final Map<String, Set<String>> reqs = getRequiredAttributes();
        authz.setRequiredAttributes(reqs);
        final Map<String, Set<String>> rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        final Map<String, Object> pAttrs = getPrincipalAttributes();
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess("test", pAttrs));
    }

    @Test
    public void checkRejectedAttributesAvailable() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();

        final Map<String, Set<String>> rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        final Map<String, Object> pAttrs = getPrincipalAttributes();
        pAttrs.put("address", "1234 Main Street");
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess("test", pAttrs));
    }

    @Test
    public void checkRejectedAttributesAvailableRequireAll() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        authz.setRequireAllAttributes(true);

        final Map<String, Set<String>> rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        final Map<String, Object> pAttrs = getPrincipalAttributes();
        pAttrs.put("address", "1234 Main Street");
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess("test", pAttrs));
    }

    @Test
    public void checkRejectedAttributesAvailableRequireAll3() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        authz.setRequireAllAttributes(false);
        final Map<String, Set<String>> rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        final Map<String, Object> pAttrs = getPrincipalAttributes();
        pAttrs.put("role", "nomatch");
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess("test", pAttrs));
    }

    @Test
    public void checkRejectedAttributesAvailableRequireAll2() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();
        authz.setRequireAllAttributes(false);
        final Map<String, Set<String>> rejectedAttributes = getRejectedAttributes();
        authz.setRejectedAttributes(rejectedAttributes);

        final Map<String, Object> pAttrs = getPrincipalAttributes();

        pAttrs.put("role", "staff");
        assertFalse(authz.doPrincipalAttributesAllowServiceAccess("test", pAttrs));
    }

    @Test
    public void checkAuthzPrincipalWithAttrValueCaseInsensitiveComparison() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();

        final Map<String, Set<String>> reqs = getRequiredAttributes();
        authz.setRequiredAttributes(reqs);

        final Map<String, Object> pAttrs = getPrincipalAttributes();
        authz.setCaseInsensitive(true);

        pAttrs.put("cn", "cas");
        pAttrs.put("givenName", "kaz");
        assertTrue(authz.doPrincipalAttributesAllowServiceAccess("test", pAttrs));
    }

    @Test
    public void checkAuthzPrincipalWithAttrValuePatternComparison() {
        final DefaultRegisteredServiceAccessStrategy authz =
                new DefaultRegisteredServiceAccessStrategy();

        final Map<String, Set<String>> reqs = getRequiredAttributes();
        reqs.remove("cn");
        reqs.remove("givenName");

        authz.setRequiredAttributes(reqs);
        final Map<String, Object> pAttrs = getPrincipalAttributes();

        assertTrue(authz.doPrincipalAttributesAllowServiceAccess("test", pAttrs));
    }

    @Test
    public void verifySerializeADefaultRegisteredServiceAccessStrategyToJson() throws IOException {
        final DefaultRegisteredServiceAccessStrategy strategyWritten = new DefaultRegisteredServiceAccessStrategy();

        final Map<String, Set<String>> reqs = getRequiredAttributes();
        reqs.remove("cn");
        reqs.remove("givenName");

        strategyWritten.setRequiredAttributes(reqs);
        strategyWritten.setRejectedAttributes(getRejectedAttributes());

        MAPPER.writeValue(JSON_FILE, strategyWritten);

        final RegisteredServiceAccessStrategy strategyRead = MAPPER.readValue(JSON_FILE, DefaultRegisteredServiceAccessStrategy.class);

        assertEquals(strategyWritten, strategyRead);
    }

    private static Map<String, Set<String>> getRequiredAttributes() {
        final Map<String, Set<String>> map = new HashMap<>();
        map.put("cn", Stream.of("cas", "SSO").collect(Collectors.toSet()));
        map.put("givenName", Stream.of("CAS", "KAZ").collect(Collectors.toSet()));
        map.put("phone", Collections.singleton("\\d\\d\\d-\\d\\d\\d-\\d\\d\\d"));
        return map;
    }

    private static Map<String, Set<String>> getRejectedAttributes() {
        final Map<String, Set<String>> map = new HashMap<>();
        map.put("address", Collections.singleton(".+"));
        map.put("role", Collections.singleton("staff"));
        return map;
    }
    
    private static Map<String, Object> getPrincipalAttributes() {
        final Map<String, Object> map = new HashMap<>();
        map.put("cn", "cas");
        map.put("givenName", Arrays.asList("cas", "KAZ"));
        map.put("sn", "surname");
        map.put("phone", "123-456-7890");

        return map;
    }
}
