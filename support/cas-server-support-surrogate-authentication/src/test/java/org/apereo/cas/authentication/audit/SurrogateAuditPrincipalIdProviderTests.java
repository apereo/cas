package org.apereo.cas.authentication.audit;

import module java.base;
import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.SurrogateAuthenticationException;
import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationServiceTests;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SurrogateAuditPrincipalIdProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Audits")
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
class SurrogateAuditPrincipalIdProviderTests {
    @Autowired
    @Qualifier("surrogateAuditPrincipalIdProvider")
    private AuditPrincipalIdProvider surrogateAuditPrincipalIdProvider;

    @Test
    void verifyAction() {
        assertEquals(Credential.UNKNOWN_ID, surrogateAuditPrincipalIdProvider.getPrincipalIdFrom(null, null, null, null));

        val auth = CoreAuthenticationTestUtils.getAuthentication(
            CoreAuthenticationTestUtils.getPrincipal(),
            CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, List.of("true"),
                SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL, List.of("principal"),
                SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER, List.of("surrogateUser")));
        assertTrue(surrogateAuditPrincipalIdProvider.supports(mock(JoinPoint.class), auth,
            new Object(), new SurrogateAuthenticationException("error")));
        assertNotNull(surrogateAuditPrincipalIdProvider.getPrincipalIdFrom(mock(JoinPoint.class), auth,
            new Object(), new SurrogateAuthenticationException("error")));
    }

    @Test
    void verifyNoSurrogateAction() {
        assertTrue(surrogateAuditPrincipalIdProvider.getOrder() > 0);
        val auth = CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal());
        assertEquals("test",
            surrogateAuditPrincipalIdProvider.getPrincipalIdFrom(mock(JoinPoint.class),
                auth, new Object(), new SurrogateAuthenticationException("error")));
    }
}
