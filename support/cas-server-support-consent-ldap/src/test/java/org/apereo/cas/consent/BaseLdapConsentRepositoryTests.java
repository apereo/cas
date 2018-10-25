package org.apereo.cas.consent;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.category.LdapCategory;
import org.apereo.cas.config.CasConsentLdapConfiguration;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchScope;
import lombok.Getter;
import lombok.val;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseLdapConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasConsentLdapConfiguration.class,
    RefreshAutoConfiguration.class
})
@Category(LdapCategory.class)
@Getter
public abstract class BaseLdapConsentRepositoryTests extends BaseConsentRepositoryTests {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final String ATTR_NAME = "description";
    private static final String USER_CN = "casuser";
    private static final String USER_DN = "cn=casuser,ou=people,dc=example,dc=org";
    private static final String USER2_CN = "casuser2";
    private static final String USER2_DN = "cn=casuser2,ou=people,dc=example,dc=org";
    private static final Service SVC2 = RegisteredServiceTestUtils.getService2();
    private static final AbstractRegisteredService REG_SVC2 = RegisteredServiceTestUtils.getRegisteredService(SVC2.getId());
    private static final String DEF_FILTER = "(objectClass=*)";

    @Autowired
    @Qualifier("consentRepository")
    protected ConsentRepository repository;

    @AfterEach
    public void cleanDecisions() {
        val conn = getConnection();
        try {
            val res = conn.search(USER_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
            if (res.getEntryCount() != 0 && res.getSearchEntry(USER_DN).hasAttribute(ATTR_NAME)) {
                conn.modify(USER_DN, new Modification(ModificationType.DELETE, ATTR_NAME));
            }
            val res2 = conn.search(USER2_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
            if (res2.getEntryCount() != 0 && res2.getSearchEntry(USER2_DN).hasAttribute(ATTR_NAME)) {
                conn.modify(USER2_DN, new Modification(ModificationType.DELETE, ATTR_NAME));
            }
        } catch (final LDAPException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void verifyConsentDecisionIsNotMistaken() {
        try {
            val decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
            decision.setId(1);
            val mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
            assertEquals(ResultCode.SUCCESS, getConnection().modify(USER_DN, mod).getResultCode());

            val d = this.repository.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication("unknownUser"));
            assertNull(d);

            val d2 = this.repository.findConsentDecision(RegisteredServiceTestUtils.getService2(),
                REG_SVC, CoreAuthenticationTestUtils.getAuthentication(USER_CN));
            assertNull(d2);
        } catch (final LDAPException | JsonProcessingException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void verifyAllConsentDecisionsAreFoundForSingleUser() {
        try {
            val decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
            decision.setId(1);
            val mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
            assertEquals(ResultCode.SUCCESS, getConnection().modify(USER_DN, mod).getResultCode());
            val decision2 = BUILDER.build(SVC, REG_SVC, USER2_CN, ATTR);
            decision2.setId(2);
            val mod2 = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision2));
            assertEquals(ResultCode.SUCCESS, getConnection().modify(USER2_DN, mod2).getResultCode());

            val d = this.repository.findConsentDecisions(USER_CN);
            assertNotNull(d);
            assertEquals(1, d.size());
            assertEquals(USER_CN, d.iterator().next().getPrincipal());
        } catch (final LDAPException | JsonProcessingException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void verifyAllConsentDecisionsAreFoundForAllUsers() {
        try {
            val decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
            decision.setId(1);
            val mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
            assertEquals(ResultCode.SUCCESS, getConnection().modify(USER_DN, mod).getResultCode());
            val decision2 = BUILDER.build(SVC, REG_SVC, USER2_CN, ATTR);
            decision2.setId(2);
            val mod2 = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision2));
            assertEquals(ResultCode.SUCCESS, getConnection().modify(USER2_DN, mod2).getResultCode());

            val d = this.repository.findConsentDecisions();
            assertNotNull(d);
            assertFalse(d.isEmpty());
            assertEquals(2, d.size());
        } catch (final LDAPException | JsonProcessingException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void verifyConsentDecisionIsStored() {
        try {
            val decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
            assertTrue(this.repository.storeConsentDecision(decision));
            val r = getConnection().search(USER_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
            assertTrue(r.getEntryCount() > 0);
            val d = MAPPER.readValue(r.getSearchEntry(USER_DN).getAttributeValue(ATTR_NAME), ConsentDecision.class);
            assertNotNull(d);
            assertEquals(USER_CN, d.getPrincipal());
        } catch (final LDAPException | IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void verifyConsentDecisionIsUpdated() {
        try {
            val decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
            decision.setId(1);
            val mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
            assertEquals(ResultCode.SUCCESS, getConnection().modify(USER_DN, mod).getResultCode());

            val t = LocalDateTime.now();
            assertNotEquals(t, decision.getCreatedDate());
            decision.setCreatedDate(t);
            this.repository.storeConsentDecision(decision);

            val r2 = getConnection().search(USER_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
            assertTrue(r2.getEntryCount() > 0);
            val d = MAPPER.readValue(r2.getSearchEntry(USER_DN).getAttributeValue(ATTR_NAME), ConsentDecision.class);
            assertNotNull(d);
            assertEquals(d.getId(), decision.getId());
            assertEquals(d.getCreatedDate(), t);
        } catch (final LDAPException | IOException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void verifyConsentDecisionIsDeleted() {
        try {
            val decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
            decision.setId(1);
            val mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
            assertEquals(ResultCode.SUCCESS, getConnection().modify(USER_DN, mod).getResultCode());
            val decision2 = BUILDER.build(SVC2, REG_SVC2, USER_CN, ATTR);
            decision2.setId(2);
            val mod2 = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision2));
            assertEquals(ResultCode.SUCCESS, getConnection().modify(USER_DN, mod2).getResultCode());
            val decision3 = BUILDER.build(SVC, REG_SVC, USER2_CN, ATTR);
            decision3.setId(3);
            val mod3 = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision3));
            assertEquals(ResultCode.SUCCESS, getConnection().modify(USER2_DN, mod3).getResultCode());

            assertTrue(this.repository.deleteConsentDecision(decision2.getId(), USER_CN));

            val r = getConnection().search(USER_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
            assertTrue(r.getEntryCount() > 0);
            assertEquals(1, r.getSearchEntry(USER_DN).getAttributeValues(ATTR_NAME).length);
        } catch (final LDAPException | JsonProcessingException e) {
            throw new AssertionError(e);
        }
    }

    public abstract LDAPConnection getConnection();
}
