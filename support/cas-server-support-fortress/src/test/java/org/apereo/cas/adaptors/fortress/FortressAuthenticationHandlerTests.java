package org.apereo.cas.adaptors.fortress;

import org.apache.directory.fortress.core.AccessMgr;
import org.apache.directory.fortress.core.GlobalErrIds;
import org.apache.directory.fortress.core.PasswordException;
import org.apache.directory.fortress.core.model.Session;
import org.apache.directory.fortress.core.model.User;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.HandlerResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.FailedLoginException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.UUID;

/**
 * This is {@link FortressAuthenticationHandler}.
 *
 * @author yudhi.k.surtan
 * @since 5.2.0
 */
public class FortressAuthenticationHandlerTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(FortressAuthenticationHandlerTests.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private AccessMgr accessManager;

    @InjectMocks
    private FortressAuthenticationHandler fortressAuthenticationHandler;

    @Before
    public void initializeTest() {
        MockitoAnnotations.initMocks(this);
        fortressAuthenticationHandler.setAccessManager(accessManager);
    }

    @Test
    public void verifyUnauthorizedUserLoginIncorrect() throws Exception {
        Mockito.when(accessManager.createSession(Mockito.any(User.class), Mockito.anyBoolean()))
                .thenThrow(new PasswordException(GlobalErrIds.USER_PW_INVLD, "error message"));
        this.thrown.expect(FailedLoginException.class);
        fortressAuthenticationHandler.authenticateUsernamePasswordInternal(
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), null);
    }

    @Test
    public void verifyAuthenticateSuccessfully() throws Exception {
        final UUID sessionId = UUID.randomUUID();
        final Session session = new Session(new User(CoreAuthenticationTestUtils.CONST_USERNAME), sessionId.toString());
        session.setAuthenticated(true);
        Mockito.when(accessManager.createSession(Mockito.any(User.class), Mockito.anyBoolean())).thenReturn(session);
        try {
            final HandlerResult handlerResult = fortressAuthenticationHandler.authenticateUsernamePasswordInternal(
                    CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), null);
            Assert.assertEquals(handlerResult.getPrincipal().getId(), CoreAuthenticationTestUtils.CONST_USERNAME);
            final JAXBContext jaxbContext = JAXBContext.newInstance(Session.class);
            final Marshaller marshaller = jaxbContext.createMarshaller();
            final StringWriter writer = new StringWriter();
            marshaller.marshal(session, writer);
            Assert.assertEquals(writer.toString(), handlerResult.getPrincipal().getAttributes().get(FortressAuthenticationHandler.FORTRESS_SESSION_KEY));
        } catch (final Exception e) {
            LOGGER.error("test failed", e);
        }
    }
}
