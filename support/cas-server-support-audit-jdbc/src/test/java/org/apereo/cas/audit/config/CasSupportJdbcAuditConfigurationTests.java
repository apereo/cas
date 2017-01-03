package org.apereo.cas.audit.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.audit.spi.DelegatingAuditTrailManager;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.AuditAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;
/**
 * This is {@link CasSupportJdbcAuditConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(SpringRunner.class)
@Transactional
@SpringBootTest(classes = {RefreshAutoConfiguration.class, 
        CasSupportJdbcAuditConfiguration.class,
        CasCoreAuditConfiguration.class,
        CasCoreServicesConfiguration.class})
public class CasSupportJdbcAuditConfigurationTests {
    
    @Autowired
    @Qualifier("auditTrailManager")
    private DelegatingAuditTrailManager auditTrailManager;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;
    
    @BeforeClass
    public static void setup() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.setLocalAddr("7.8.9.0");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
    }
    
    @Test
    public void verifyAudit() {
        this.servicesManager.save(RegisteredServiceTestUtils.getRegisteredService("auditservicetest"));
        assertEquals(this.auditTrailManager.get().size(), 1);
    }
}
