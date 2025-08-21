package org.apereo.cas.adaptors.trusted.authentication.handler.support;

import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Andrew Petro
 * @since 3.0.0
 */
@Tag("AuthenticationHandler")
class PrincipalBearingCredentialsAuthenticationHandlerTests {

    private final PrincipalBearingCredentialsAuthenticationHandler handler =
        new PrincipalBearingCredentialsAuthenticationHandler(StringUtils.EMPTY,
            PrincipalFactoryUtils.newPrincipalFactory(), null);

    /**
     * When the credentials bear a Principal, succeed the authentication.
     */
    @Test
    void verifyNonNullPrincipal() throws Throwable {
        val credentials = new PrincipalBearingCredential(PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("scott"));
        assertNotNull(this.handler.authenticate(credentials, mock(Service.class)));
    }

    @Test
    void verifySupports() throws Throwable {
        val credentials = new PrincipalBearingCredential(PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("scott"));
        assertTrue(this.handler.supports(credentials));
        assertFalse(this.handler.supports(new UsernamePasswordCredential()));
    }
}
