package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationFailedException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionCatalog;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasWebflowExceptionCatalogTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("WebflowConfig")
@TestPropertySource(properties = "cas.authn.errors.exceptions=org.apereo.cas.ticket.InvalidTicketException")
class DefaultCasWebflowExceptionCatalogTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowExceptionCatalog.BEAN_NAME)
    private CasWebflowExceptionCatalog casWebflowExceptionCatalog;

    @Test
    void verifyAuthnExceptionTranslation() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        val exception = new AuthenticationException(new MultifactorAuthenticationFailedException(new RuntimeException("Failed")));
        val results = casWebflowExceptionCatalog.translateException(requestContext, exception);
        assertEquals(MultifactorAuthenticationFailedException.class.getSimpleName(), results);
    }

    @Test
    void verifyTicketExceptionTranslation() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        val exception = new InvalidTicketException(UUID.randomUUID().toString());
        val results = casWebflowExceptionCatalog.translateException(requestContext, exception);
        assertEquals(InvalidTicketException.class.getSimpleName(), results);
    }

    @Test
    void verifyEmptyAuthnExceptionTranslation() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        val exception = new AuthenticationException("Failed");
        val results = casWebflowExceptionCatalog.translateException(requestContext, exception);
        assertEquals(CasWebflowExceptionCatalog.UNKNOWN, results);
    }

    @Test
    void verifyUnknownExceptionTranslation() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        val exception = new RuntimeException("Failed");
        val results = casWebflowExceptionCatalog.translateException(requestContext, exception);
        assertEquals(CasWebflowExceptionCatalog.UNKNOWN, results);
    }
}

