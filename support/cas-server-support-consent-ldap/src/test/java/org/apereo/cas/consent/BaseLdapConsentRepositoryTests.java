package org.apereo.cas.consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchScope;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.category.LdapCategory;
import org.apereo.cas.config.CasConsentLdapConfiguration;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link BaseLdapConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {CasConsentLdapConfiguration.class, RefreshAutoConfiguration.class})
@Category(LdapCategory.class)
public abstract class BaseLdapConsentRepositoryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final DefaultConsentDecisionBuilder BUILDER = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());

    private static final String ATTR_NAME = "description";
    private static final String USER_CN = "consentTest";
    private static final String USER_DN = "cn=consentTest,ou=people,dc=example,dc=org";
    private static final String USER2_CN = "consentTest2";
    private static final String USER2_DN = "cn=consentTest2,ou=people,dc=example,dc=org";
    private static final Service SVC = RegisteredServiceTestUtils.getService();
    private static final Service SVC2 = RegisteredServiceTestUtils.getService2();
    private static final AbstractRegisteredService REG_SVC = RegisteredServiceTestUtils.getRegisteredService(SVC.getId());
    private static final AbstractRegisteredService REG_SVC2 = RegisteredServiceTestUtils.getRegisteredService(SVC2.getId());
    private static final Map<String, Object> ATTR = CollectionUtils.wrap("attribute", "value");
    private static final String DEF_FILTER = "(objectClass=*)";

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    @Qualifier("consentRepository")
    private ConsentRepository repository;

    @After
    public void cleanDecisions() throws Exception {
        final var conn = getConnection();
        final var res = conn.search(USER_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
        if (res.getEntryCount() != 0 && res.getSearchEntry(USER_DN).hasAttribute(ATTR_NAME)) {
            conn.modify(USER_DN, new Modification(ModificationType.DELETE, ATTR_NAME));
        }
        final var res2 = conn.search(USER2_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
        if (res2.getEntryCount() != 0 && res2.getSearchEntry(USER2_DN).hasAttribute(ATTR_NAME)) {
            conn.modify(USER2_DN, new Modification(ModificationType.DELETE, ATTR_NAME));
        }
    }

    @Test
    public void verifyConsentDecisionIsNotFound() {
        final var d = this.repository.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication(USER_CN));
        assertNull(d);
    }

    @Test
    public void verifyConsentDecisionIsNotMistaken() throws Exception {
        final var decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        decision.setId(1);
        final var mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals(ResultCode.SUCCESS, getConnection().modify(USER_DN, mod).getResultCode());

        final var d = this.repository.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication("unknownUser"));
        assertNull(d);

        final var d2 = this.repository.findConsentDecision(RegisteredServiceTestUtils.getService2(),
            REG_SVC, CoreAuthenticationTestUtils.getAuthentication(USER_CN));
        assertNull(d2);
    }

    @Test
    public void verifyConsentDecisionIsFound() throws Exception {
        final var decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        decision.setId(1);
        final var mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals(ResultCode.SUCCESS, getConnection().modify(USER_DN, mod).getResultCode());

        final var d = this.repository.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication(USER_CN));
        assertNotNull(d);
        assertEquals(USER_CN, d.getPrincipal());
    }

    @Test
    public void verifyAllConsentDecisionsAreFoundForSingleUser() throws Exception {
        final var decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        decision.setId(1);
        final var mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals(ResultCode.SUCCESS, getConnection().modify(USER_DN, mod).getResultCode());
        final var decision2 = BUILDER.build(SVC, REG_SVC, USER2_CN, ATTR);
        decision2.setId(2);
        final var mod2 = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision2));
        assertEquals(ResultCode.SUCCESS, getConnection().modify(USER2_DN, mod2).getResultCode());

        final var d = this.repository.findConsentDecisions(USER_CN);
        assertNotNull(d);
        assertEquals(1, d.size());
        assertEquals(USER_CN, d.iterator().next().getPrincipal());
    }

    @Test
    public void verifyAllConsentDecisionsAreFoundForAllUsers() throws Exception {
        final var decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        decision.setId(1);
        final var mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals(ResultCode.SUCCESS, getConnection().modify(USER_DN, mod).getResultCode());
        final var decision2 = BUILDER.build(SVC, REG_SVC, USER2_CN, ATTR);
        decision2.setId(2);
        final var mod2 = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision2));
        assertEquals(ResultCode.SUCCESS, getConnection().modify(USER2_DN, mod2).getResultCode());

        final var d = this.repository.findConsentDecisions();
        assertNotNull(d);
        assertFalse(d.isEmpty());
        assertEquals(2, d.size());
    }

    @Test
    public void verifyConsentDecisionIsStored() throws Exception {
        final var decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        assertTrue(this.repository.storeConsentDecision(decision));
        final var r = getConnection().search(USER_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
        assertTrue(r.getEntryCount() > 0);
        final var d = MAPPER.readValue(r.getSearchEntry(USER_DN).getAttributeValue(ATTR_NAME), ConsentDecision.class);
        assertNotNull(d);
        assertEquals(USER_CN, d.getPrincipal());
    }

    @Test
    public void verifyConsentDecisionIsUpdated() throws Exception {
        final var decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        decision.setId(1);
        final var mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals(ResultCode.SUCCESS, getConnection().modify(USER_DN, mod).getResultCode());

        final var t = LocalDateTime.now();
        assertNotEquals(t, decision.getCreatedDate());
        decision.setCreatedDate(t);
        this.repository.storeConsentDecision(decision);

        final var r2 = getConnection().search(USER_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
        assertTrue(r2.getEntryCount() > 0);
        final var d = MAPPER.readValue(r2.getSearchEntry(USER_DN).getAttributeValue(ATTR_NAME), ConsentDecision.class);
        assertNotNull(d);
        assertEquals(d.getId(), decision.getId());
        assertEquals(d.getCreatedDate(), t);
    }

    @Test
    public void verifyConsentDecisionIsDeleted() throws Exception {
        final var decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        decision.setId(1);
        final var mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals(ResultCode.SUCCESS, getConnection().modify(USER_DN, mod).getResultCode());
        final var decision2 = BUILDER.build(SVC2, REG_SVC2, USER_CN, ATTR);
        decision2.setId(2);
        final var mod2 = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision2));
        assertEquals(ResultCode.SUCCESS, getConnection().modify(USER_DN, mod2).getResultCode());
        final var decision3 = BUILDER.build(SVC, REG_SVC, USER2_CN, ATTR);
        decision3.setId(3);
        final var mod3 = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision3));
        assertEquals(ResultCode.SUCCESS, getConnection().modify(USER2_DN, mod3).getResultCode());

        assertTrue(this.repository.deleteConsentDecision(decision2.getId(), USER_CN));

        final var r = getConnection().search(USER_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
        assertTrue(r.getEntryCount() > 0);
        assertEquals(1, r.getSearchEntry(USER_DN).getAttributeValues(ATTR_NAME).length);
    }

    public abstract LDAPConnection getConnection();
}
