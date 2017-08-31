package org.apereo.cas.consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;
import java.time.LocalDateTime;
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
    private static final ConsentDecisionBuilder BUILDER = new ConsentDecisionBuilder(NoOpCipherExecutor.getInstance());
    
    private static final String ATTR_NAME = "description";
    private static final String USER_CN = "consentTest";
    private static final String USER_DN = "cn=consentTest,ou=people,dc=example,dc=org";
    private static final Service SVC = RegisteredServiceTestUtils.getService();
    private static final AbstractRegisteredService REG_SVC = RegisteredServiceTestUtils.getRegisteredService(SVC.getId());
    private static final Map<String, Object> ATTR = CollectionUtils.wrap("attribute", "value");
    
    @Autowired
    @Qualifier("consentRepository")
    private LdapConsentRepository repository;
    
    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer();
        DIRECTORY.populateEntries(new ClassPathResource("ldif/ldap-consent.ldif").getInputStream());
    }
    
    @After
    public void cleanDecisions() throws Exception {
        final LDAPConnection conn = DIRECTORY.getConnection();
        final SearchResult res = conn.search(USER_DN, SearchScope.SUB, "(objectClass=*)", ATTR_NAME);
        if (res.getEntryCount() != 0 && res.getSearchEntry(USER_DN).hasAttribute(ATTR_NAME)) {
            conn.modify(USER_DN, new Modification(ModificationType.DELETE, ATTR_NAME));
        }
    }
    
    @Test
    public void verifyConsentDecisionIsNotFound() throws Exception {
        final ConsentDecision d = this.repository.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication(USER_CN));
        assertNull(d);
    }
    
    @Test
    public void verifyConsentDecisionIsNotMistaken() throws Exception {        
        final ConsentDecision decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        final Modification mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals("success", DIRECTORY.getConnection().modify(USER_DN, mod).getResultCode().getName());
        
        final ConsentDecision d = this.repository.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication(USER_CN+"2"));
        assertNull(d);
        
        final ConsentDecision d2 = this.repository.findConsentDecision(RegisteredServiceTestUtils.getService2(),
                REG_SVC, CoreAuthenticationTestUtils.getAuthentication(USER_CN));
        assertNull(d2);
    }
    
    @Test
    public void verifyConsentDecisionIsFound() throws Exception {
        final ConsentDecision decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        final Modification mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals("success", DIRECTORY.getConnection().modify(USER_DN, mod).getResultCode().getName());
        
        final ConsentDecision d = this.repository.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication(USER_CN));
        assertNotNull(d);
        assertEquals(d.getPrincipal(), USER_CN);        
    }
    
    @Test
    public void verifyConsentDecisionIsStored() throws Exception {
        final ConsentDecision decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        
        this.repository.storeConsentDecision(decision);
        final SearchResult r = DIRECTORY.getConnection().search(USER_DN, SearchScope.SUB, "(objectClass=*)", ATTR_NAME);
        assertTrue(r.getEntryCount() > 0);
        final ConsentDecision d = MAPPER.readValue(r.getSearchEntry(USER_DN).getAttributeValue(ATTR_NAME), ConsentDecision.class);
        assertNotNull(d);
        assertEquals(d.getPrincipal(), USER_CN);
    }
    
    @Test
    public void verifyConsentDecisionIsUpdated() throws Exception {
        final ConsentDecision decision = BUILDER.build(SVC, REG_SVC, USER_CN, ATTR);
        final Modification mod = new Modification(ModificationType.ADD, ATTR_NAME, MAPPER.writeValueAsString(decision));
        assertEquals("success", DIRECTORY.getConnection().modify(USER_DN, mod).getResultCode().getName());
                
        final LocalDateTime t = LocalDateTime.now();
        assertNotEquals(t, decision.getDate());
        decision.setDate(t);
        this.repository.storeConsentDecision(decision);
        
        final SearchResult r2 = DIRECTORY.getConnection().search(USER_DN, SearchScope.SUB, "(objectClass=*)", ATTR_NAME);
        assertTrue(r2.getEntryCount() > 0);
        final ConsentDecision d = MAPPER.readValue(r2.getSearchEntry(USER_DN).getAttributeValue(ATTR_NAME), ConsentDecision.class);
        assertNotNull(d);
        assertEquals(d.getId(), decision.getId());
        assertEquals(d.getDate(), t);
    }    
}
