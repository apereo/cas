package org.apereo.cas.adaptors.fortress;

import org.apache.directory.fortress.core.AccessMgr;
import org.apache.directory.fortress.core.GlobalErrIds;
import org.apache.directory.fortress.core.PasswordException;
import org.apache.directory.fortress.core.SecurityException;
import org.apache.directory.fortress.core.model.Session;
import org.apache.directory.fortress.core.model.User;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.FailedLoginException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.util.UUID;

/**
 * This is {@link FortressAuthenticationHandler}.
 *
 * @author yudhi.k.surtan
 * @since 5.2.0
 */

public class FortressAuthenticationHandlerTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(FortressAuthenticationHandlerTests.class);
    private static final String FORTRESS_SESSION_KEY = "fortressSession";

    @Mock
    private AccessMgr accessManager;

    @InjectMocks
    private FortressAuthenticationHandler fortressAuthenticationHandler;

    @Before
    public void initializeTest() {
        MockitoAnnotations.initMocks(this);
        fortressAuthenticationHandler.setAccessManager(accessManager);
    }

    @Test(expected = FailedLoginException.class)
    public void testForUnauthorizeUserLoginIncorrect() throws SecurityException, GeneralSecurityException, PreventedException {
        Mockito.when(accessManager.createSession(Mockito.any(User.class), Mockito.anyBoolean())).thenThrow(new PasswordException(GlobalErrIds.USER_PW_INVLD, "error message"));
        try {
            fortressAuthenticationHandler.authenticateUsernamePasswordInternal(new UsernamePasswordCredential("username", "password"), null);
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void testAuthenticateSuccessfully() throws SecurityException, GeneralSecurityException, PreventedException {
        UUID sessionId = UUID.randomUUID();
        Session session = new Session(new User("username"), sessionId.toString());
        session.setAuthenticated(true);
        Mockito.when(accessManager.createSession(Mockito.any(User.class), Mockito.anyBoolean())).thenReturn(session);
        try {
            HandlerResult handlerResult = fortressAuthenticationHandler.authenticateUsernamePasswordInternal(new UsernamePasswordCredential("username", "password"), null);
            Assert.assertEquals(handlerResult.getPrincipal().getId(),"username");
            final JAXBContext jaxbContext = JAXBContext.newInstance(Session.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            final StringWriter writer = new StringWriter();
            marshaller.marshal(session, writer);
            Assert.assertEquals(writer.toString(), handlerResult.getPrincipal().getAttributes().get(FORTRESS_SESSION_KEY));
        } catch (Exception e) {
            LOGGER.error("test failed", e);
        }
    }
}
