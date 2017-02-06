package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.authentication.principal.Principal;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link AbstractRegisteredService}.
 *
 * @author Marvin S. Addison
 * @since 3.4.12
 */
public class AbstractRegisteredServiceTests {

    private static final long ID = 1000;
    private static final String DESCRIPTION = "test";
    private static final String SERVICEID = "serviceId";
    private static final String THEME = "theme";
    private static final String NAME = "name";
    private static final boolean ENABLED = false;
    private static final boolean ALLOWED_TO_PROXY = false;
    private static final boolean SSO_ENABLED = false;
    
    private final AbstractRegisteredService r = new AbstractRegisteredService() {
        private static final long serialVersionUID = 1L;

        @Override
        public void setServiceId(final String id) {
            serviceId = id;
        }

        @Override
        protected AbstractRegisteredService newInstance() {
            return this;
        }

        @Override
        public boolean matches(final Service service) {
            return true;
        }

        @Override
        public boolean matches(final String serviceId) {
            return true;
        }
    };

    @Test
    public void verifyAllowToProxyIsFalseByDefault() {
        final RegexRegisteredService regexRegisteredService = new RegexRegisteredService();
        assertFalse(regexRegisteredService.getProxyPolicy().isAllowedToProxy());
        final RegexRegisteredService service = new RegexRegisteredService();
        assertFalse(service.getProxyPolicy().isAllowedToProxy());
    }

    @Test
    public void verifySettersAndGetters() {
        prepareService();

        assertEquals(ALLOWED_TO_PROXY, this.r.getProxyPolicy().isAllowedToProxy());
        assertEquals(DESCRIPTION, this.r.getDescription());
        assertEquals(ENABLED, this.r.getAccessStrategy()
                .isServiceAccessAllowed());
        assertEquals(ID, this.r.getId());
        assertEquals(NAME, this.r.getName());
        assertEquals(SERVICEID, this.r.getServiceId());
        assertEquals(SSO_ENABLED, this.r.getAccessStrategy()
                .isServiceAccessAllowedForSso());
        assertEquals(THEME, this.r.getTheme());

        assertNotNull(this.r);
        assertFalse(this.r.equals(new Object()));
        assertEquals(this.r, this.r);
    }

    @Test
    public void verifyEquals() throws Exception {
        assertTrue(r.equals(r.clone()));
        assertNotNull(new RegexRegisteredService());
        assertFalse(new RegexRegisteredService().equals(new Object()));
    }
    
    private void prepareService() {
        this.r.setUsernameAttributeProvider(
                new AnonymousRegisteredServiceUsernameAttributeProvider(
                        new ShibbolethCompatiblePersistentIdGenerator("casrox")));
        this.r.setDescription(DESCRIPTION);
        this.r.setId(ID);
        this.r.setName(NAME);
        this.r.setServiceId(SERVICEID);
        this.r.setTheme(THEME);
        this.r.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(ENABLED, SSO_ENABLED));
    }
    
    @Test
    public void verifyServiceAttributeFilterAllAttributes() {
        prepareService();
        this.r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        final Principal p = mock(Principal.class);
        
        final Map<String, Object> map = new HashMap<>();
        map.put("attr1", "value1");
        map.put("attr2", "value2");
        map.put("attr3", Arrays.asList("v3", "v4"));
        
        when(p.getAttributes()).thenReturn(map);
        when(p.getId()).thenReturn("principalId");
        
        final Map<String, Object> attr = this.r.getAttributeReleasePolicy()
                .getAttributes(p, RegisteredServiceTestUtils.getRegisteredService("test"));
        assertEquals(attr.size(), map.size());
    }
    
    @Test
    public void verifyServiceAttributeFilterAllowedAttributes() {
        prepareService();
        final ReturnAllowedAttributeReleasePolicy policy = new ReturnAllowedAttributeReleasePolicy();
        policy.setAllowedAttributes(Arrays.asList("attr1", "attr3"));
        this.r.setAttributeReleasePolicy(policy);
        final Principal p = mock(Principal.class);
        
        final Map<String, Object> map = new HashMap<>();
        map.put("attr1", "value1");
        map.put("attr2", "value2");
        map.put("attr3", Arrays.asList("v3", "v4"));
        
        when(p.getAttributes()).thenReturn(map);
        when(p.getId()).thenReturn("principalId");
        
        final Map<String, Object> attr = this.r.getAttributeReleasePolicy().getAttributes(p,
                RegisteredServiceTestUtils.getRegisteredService("test"));
        assertEquals(attr.size(), 2);
        assertTrue(attr.containsKey("attr1"));
        assertTrue(attr.containsKey("attr3"));
    }
    
    @Test
    public void verifyServiceAttributeFilterMappedAttributes() {
        prepareService();
        final ReturnMappedAttributeReleasePolicy policy = new ReturnMappedAttributeReleasePolicy();
        final Map<String, String> mappedAttr = new HashMap<>();
        mappedAttr.put("attr1", "newAttr1");
        
        policy.setAllowedAttributes(mappedAttr);
                
        this.r.setAttributeReleasePolicy(policy);
        final Principal p = mock(Principal.class);
        
        final Map<String, Object> map = new HashMap<>();
        map.put("attr1", "value1");
        map.put("attr2", "value2");
        map.put("attr3", Arrays.asList("v3", "v4"));
        
        when(p.getAttributes()).thenReturn(map);
        when(p.getId()).thenReturn("principalId");
        
        final Map<String, Object> attr = this.r.getAttributeReleasePolicy().getAttributes(p,
                RegisteredServiceTestUtils.getRegisteredService("test"));
        assertEquals(attr.size(), 1);
        assertTrue(attr.containsKey("newAttr1"));
    }

    @Test
    public void verifyServiceEquality() {
        final RegisteredService svc1 = RegisteredServiceTestUtils.getRegisteredService(SERVICEID);
        final RegisteredService svc2 = RegisteredServiceTestUtils.getRegisteredService(SERVICEID);
        assertEquals(svc1, svc2);
    }

    @Test
    public void verifyServiceCopy() throws Exception {
        final RegisteredService svc1 = RegisteredServiceTestUtils.getRegisteredService(SERVICEID);
        final RegisteredService svc2 = svc1.clone();
        assertEquals(svc1, svc2);
    }

    @Test
    public void verifyServiceWithInvalidIdStillHasTheSameIdAfterCallingMatches() throws Exception {
        final String invalidId = "***";
        final AbstractRegisteredService service = RegisteredServiceTestUtils.getRegisteredService(invalidId);

        service.matches("notRelevant");

        assertEquals(service.getServiceId(), invalidId);
    }
}
