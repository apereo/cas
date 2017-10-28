package org.apereo.cas.consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.cipher.NoOpCipherExecutor;
import org.apereo.cas.adaptors.ldap.AbstractLdapTests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasConsentLdapConfiguration;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link LdapConsentRepository} class.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CasConsentLdapConfiguration.class, RefreshAutoConfiguration.class})
@TestPropertySource(locations = "classpath:/ldapconsent.properties")
public class LdapConsentRepositoryTests extends AbstractLdapTests {
    
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final DefaultConsentDecisionBuilder BUILDER = new DefaultConsentDecisionBuilder(NoOpCipherExecutor.getInstance());
    
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
    
    @Autowired
    @Qualifier("consentRepository")
    private ConsentRepository repository;
    
    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer(1387);
        DIRECTORY.populateEntries(new ClassPathResource("ldif/ldap-consent.ldif").getInputStream());
    }
    
    @After
    public void cleanDecisions() throws Exception {
        final LDAPConnection conn = DIRECTORY.getConnection();
        final SearchResult res = conn.search(USER_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
        if (res.getEntryCount() != 0 && res.getSearchEntry(USER_DN).hasAttribute(ATTR_NAME)) {
            conn.modify(USER_DN, new Modification(ModificationType.DELETE, ATTR_NAME));
        }
        final SearchResult res2 = conn.search(USER2_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
        if (res2.getEntryCount() != 0 && res2.getSearchEntry(USER2_DN).hasAttribute(ATTR_NAME)) {
            conn.modify(USER2_DN, new Modification(ModificationType.DELETE, ATTR_NAME));
        }
    }
    
    @Test
    public void verifyConsentDecisionIsNotFound() {
        final ConsentDecision d = this.repository.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication(USER_CN));
        assertNull(d);
    }
    
    @Test
    public void verifyConsentDecisionIsNotMistaken() throws Exception {
        final ConsentDecision decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        decision.setId(1);
        final Modification mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals(ResultCode.SUCCESS, DIRECTORY.getConnection().modify(USER_DN, mod).getResultCode());
        
        final ConsentDecision d = this.repository.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication("unknownUser"));
        assertNull(d);
        
        final ConsentDecision d2 = this.repository.findConsentDecision(RegisteredServiceTestUtils.getService2(),
                REG_SVC, CoreAuthenticationTestUtils.getAuthentication(USER_CN));
        assertNull(d2);
    }
    
    @Test
    public void verifyConsentDecisionIsFound() throws Exception {
        final ConsentDecision decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        decision.setId(1);
        final Modification mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals(ResultCode.SUCCESS, DIRECTORY.getConnection().modify(USER_DN, mod).getResultCode());
        
        final ConsentDecision d = this.repository.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication(USER_CN));
        assertNotNull(d);
        assertEquals(d.getPrincipal(), USER_CN);
    }
    
    @Test
    public void verifyAllConsentDecisionsAreFoundForSingleUser() throws Exception {
        final ConsentDecision decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        decision.setId(1);
        final Modification mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals(ResultCode.SUCCESS, DIRECTORY.getConnection().modify(USER_DN, mod).getResultCode());
        final ConsentDecision decision2 = BUILDER.build(SVC, REG_SVC, USER2_CN, ATTR);
        decision2.setId(2);
        final Modification mod2 = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision2));
        assertEquals(ResultCode.SUCCESS, DIRECTORY.getConnection().modify(USER2_DN, mod2).getResultCode());
        
        final Collection<ConsentDecision> d = this.repository.findConsentDecisions(USER_CN);
        assertNotNull(d);
        assertEquals(d.size(), 1);
        assertEquals(d.iterator().next().getPrincipal(), USER_CN);
    }
    
    @Test
    public void verifyAllConsentDecisionsAreFoundForAllUsers() throws Exception {
        final ConsentDecision decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        decision.setId(1);
        final Modification mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals(ResultCode.SUCCESS, DIRECTORY.getConnection().modify(USER_DN, mod).getResultCode());
        final ConsentDecision decision2 = BUILDER.build(SVC, REG_SVC, USER2_CN, ATTR);
        decision2.setId(2);
        final Modification mod2 = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision2));
        assertEquals(ResultCode.SUCCESS, DIRECTORY.getConnection().modify(USER2_DN, mod2).getResultCode());
        
        final Collection<ConsentDecision> d = this.repository.findConsentDecisions();
        assertNotNull(d);
        assertFalse(d.isEmpty());
        assertEquals(d.size(), 2);
    }
    
    @Test
    public void verifyConsentDecisionIsStored() throws Exception {
        final ConsentDecision decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        
        this.repository.storeConsentDecision(decision);
        final SearchResult r = DIRECTORY.getConnection().search(USER_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
        assertTrue(r.getEntryCount() > 0);
        final ConsentDecision d = MAPPER.readValue(r.getSearchEntry(USER_DN).getAttributeValue(ATTR_NAME), ConsentDecision.class);
        assertNotNull(d);
        assertEquals(d.getPrincipal(), USER_CN);
    }
    
    @Test
    public void verifyConsentDecisionIsUpdated() throws Exception {
        final ConsentDecision decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        decision.setId(1);
        final Modification mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals(ResultCode.SUCCESS, DIRECTORY.getConnection().modify(USER_DN, mod).getResultCode());
                
        final LocalDateTime t = LocalDateTime.now();
        assertNotEquals(t, decision.getCreatedDate());
        decision.setCreatedDate(t);
        this.repository.storeConsentDecision(decision);
        
        final SearchResult r2 = DIRECTORY.getConnection().search(USER_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
        assertTrue(r2.getEntryCount() > 0);
        final ConsentDecision d = MAPPER.readValue(r2.getSearchEntry(USER_DN).getAttributeValue(ATTR_NAME), ConsentDecision.class);
        assertNotNull(d);
        assertEquals(d.getId(), decision.getId());
        assertEquals(d.getCreatedDate(), t);
    }

    @Test
    public void verifyConsentDecisionIsDeleted() throws Exception {
        final ConsentDecision decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        decision.setId(1);
        final Modification mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals(ResultCode.SUCCESS, DIRECTORY.getConnection().modify(USER_DN, mod).getResultCode());
        final ConsentDecision decision2 = BUILDER.build(SVC2, REG_SVC2, USER_CN, ATTR);
        decision2.setId(2);
        final Modification mod2 = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision2));
        assertEquals(ResultCode.SUCCESS, DIRECTORY.getConnection().modify(USER_DN, mod2).getResultCode());
        final ConsentDecision decision3 = BUILDER.build(SVC, REG_SVC, USER2_CN, ATTR);
        decision3.setId(3);
        final Modification mod3 = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision3));
        assertEquals(ResultCode.SUCCESS, DIRECTORY.getConnection().modify(USER2_DN, mod3).getResultCode());

        assertTrue(this.repository.deleteConsentDecision(decision2.getId(), USER_CN));

        final SearchResult r = DIRECTORY.getConnection().search(USER_DN, SearchScope.SUB, DEF_FILTER, ATTR_NAME);
        assertTrue(r.getEntryCount() > 0);
        assertEquals(r.getSearchEntry(USER_DN).getAttributeValues(ATTR_NAME).length, 1);
    }
}
