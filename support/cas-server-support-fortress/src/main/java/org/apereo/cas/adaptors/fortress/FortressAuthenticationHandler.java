package org.apereo.cas.adaptors.fortress;

import org.apache.directory.fortress.core.AccessMgr;
import org.apache.directory.fortress.core.model.Session;
import org.apache.directory.fortress.core.model.User;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.security.auth.login.FailedLoginException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * Fortress authentication handler, this class will delegate the authentication to call fortress rest authentication.
 *
 * @author yudhi.k.surtan
 * @since 5.2.0.
 */
public class FortressAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FortressAuthenticationHandler.class);
    private static final String FORTRESS_SESSION_KEY = "fortressSession";
    private static final String JAXB_EXCEPTION_MESSAGE = "failed initialize jaxb context";
    private static final String TRACE_ATTEMPT_MESSAGE_TO_FORTRESS = "trying to delegate authentication for {} to fortress";
    private static final String TRACE_FORTRESS_SESSION_RESPONSE = "fortress session result : {}";

    private static final String ERROR_MESSAGE_FOR_FORTRESS_AUTHENTICATION_FAILURE = "Fortress authentication failed for [%s]";
    private static final String ERROR_MESSAGE_FOR_JAXB_EXCEPTION = "Cannot marshalling fortress session with value : %s";

    @Autowired
    @Qualifier("fortressAccessManager")
    private AccessMgr accessManager;

    private Marshaller marshaller;

    public FortressAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                         final PrincipalFactory principalFactory, final Integer order) {
        super(name, servicesManager, principalFactory, order);
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(Session.class);
            marshaller = jaxbContext.createMarshaller();
        } catch (final JAXBException e) {
            LOGGER.error(JAXB_EXCEPTION_MESSAGE, e);
        }
        LOGGER.trace("Fortress authentication handler registered");
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential usernamePasswordCredential,
                                                                 final String originalPassword) throws GeneralSecurityException, PreventedException {
        final String username = usernamePasswordCredential.getUsername();
        final String password = usernamePasswordCredential.getPassword();
        Session fortressSession = null;
        try {
            LOGGER.trace(TRACE_ATTEMPT_MESSAGE_TO_FORTRESS, new Object[]{username});
            fortressSession = accessManager.createSession(new User(username, password.toCharArray()), false);
            if (fortressSession != null && fortressSession.isAuthenticated()) {
                final StringWriter writer = new StringWriter();
                marshaller.marshal(fortressSession, writer);
                final String fortressXmlSession = writer.toString();
                LOGGER.trace(TRACE_FORTRESS_SESSION_RESPONSE, fortressXmlSession);
                final Map<String, Object> attributes = new HashMap<>();
                attributes.put(FORTRESS_SESSION_KEY, fortressXmlSession);
                return createHandlerResult(usernamePasswordCredential,
                        principalFactory.createPrincipal(username, attributes), null);
            }
        } catch (final org.apache.directory.fortress.core.SecurityException e) {
            final String errorMessage = String.format(ERROR_MESSAGE_FOR_FORTRESS_AUTHENTICATION_FAILURE, username);
            LOGGER.trace(errorMessage, e);
            throw new FailedLoginException(errorMessage);
        } catch (final JAXBException e) {
            final String errorMessage = String.format(ERROR_MESSAGE_FOR_JAXB_EXCEPTION, fortressSession.toString());
            LOGGER.warn(errorMessage);
            throw new PreventedException(e);
        }
        throw new FailedLoginException(String.format("[%s] could not authenticate with fortress", username));
    }

    public AccessMgr getAccessManager() {
        return accessManager;
    }

    public void setAccessManager(final AccessMgr accessManager) {
        this.accessManager = accessManager;
    }
}
