package org.apereo.cas.adaptors.fortress;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.PreventedException;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.directory.fortress.core.AccessMgr;
import org.apache.directory.fortress.core.GlobalErrIds;
import org.apache.directory.fortress.core.PasswordException;
import org.apache.directory.fortress.core.model.Session;
import org.apache.directory.fortress.core.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.security.auth.login.FailedLoginException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringWriter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link FortressAuthenticationHandler}.
 *
 * @author yudhi.k.surtan
 * @since 5.2.0
 */
@Tag("Simple")
public class FortressAuthenticationHandlerTests {
    @Mock
    private AccessMgr accessManager;

    @InjectMocks
    private FortressAuthenticationHandler fortressAuthenticationHandler;

    @BeforeEach
    public void initializeTest() {
        MockitoAnnotations.initMocks(this);
        fortressAuthenticationHandler.setAccessManager(accessManager);
    }

    @Test
    public void verifyUnauthorizedUserLoginIncorrect() throws Exception {
        when(accessManager.createSession(ArgumentMatchers.any(User.class), ArgumentMatchers.anyBoolean()))
            .thenThrow(new PasswordException(GlobalErrIds.USER_PW_INVLD, "error message"));
        assertThrows(FailedLoginException.class,
            () -> fortressAuthenticationHandler.authenticateUsernamePasswordInternal(
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), null));
    }

    @Test
    @SneakyThrows
    public void verifyAuthenticateSuccessfully() {
        val sessionId = UUID.randomUUID();
        val session = new Session(new User(CoreAuthenticationTestUtils.CONST_USERNAME), sessionId.toString());
        session.setAuthenticated(true);
        when(accessManager.createSession(ArgumentMatchers.any(User.class), ArgumentMatchers.anyBoolean())).thenReturn(session);
        val handlerResult = fortressAuthenticationHandler.authenticateUsernamePasswordInternal(
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), null);
        assertEquals(CoreAuthenticationTestUtils.CONST_USERNAME,
            handlerResult.getPrincipal().getId());
        val jaxbContext = JAXBContext.newInstance(Session.class);
        val marshaller = jaxbContext.createMarshaller();
        val writer = new StringWriter();
        marshaller.marshal(session, writer);
        assertEquals(writer.toString(), handlerResult.getPrincipal()
            .getAttributes().get(FortressAuthenticationHandler.FORTRESS_SESSION_KEY).get(0));
    }

    @Test
    @SneakyThrows
    public void verifyUnauthSession() {
        val sessionId = UUID.randomUUID();
        val session = new Session(new User(CoreAuthenticationTestUtils.CONST_USERNAME), sessionId.toString());
        session.setAuthenticated(false);
        when(accessManager.createSession(ArgumentMatchers.any(User.class), ArgumentMatchers.anyBoolean())).thenReturn(session);
        assertThrows(FailedLoginException.class, () -> fortressAuthenticationHandler.authenticateUsernamePasswordInternal(
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), null));
    }

    @Test
    @SneakyThrows
    public void verifyFailToMarshalSession() {
        when(accessManager.createSession(ArgumentMatchers.any(User.class), ArgumentMatchers.anyBoolean()))
            .thenAnswer(invocationOnMock -> {
                throw new JAXBException("error");
            });
        assertThrows(PreventedException.class, () -> fortressAuthenticationHandler.authenticateUsernamePasswordInternal(
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(), null));
    }
}
