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
    /**
     * Fortress key to look up session as an attribute.
     */
    public static final String FORTRESS_SESSION_KEY = "fortressSession";

    private static final Logger LOGGER = LoggerFactory.getLogger(FortressAuthenticationHandler.class);

    private AccessMgr accessManager;
    private Marshaller marshaller;

    public FortressAuthenticationHandler(final AccessMgr accessManager,
                                         final String name,
                                         final ServicesManager servicesManager,
                                         final PrincipalFactory principalFactory,
                                         final Integer order) {
        super(name, servicesManager, principalFactory, order);
        this.accessManager = accessManager;
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(Session.class);
            this.marshaller = jaxbContext.createMarshaller();
        } catch (final Exception e) {
            LOGGER.error("Failed initialize fortress context", e);
        }
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential usernamePasswordCredential,
                                                                 final String originalPassword) throws GeneralSecurityException, PreventedException {
        final String username = usernamePasswordCredential.getUsername();
        final String password = usernamePasswordCredential.getPassword();
        Session fortressSession = null;
        try {
            LOGGER.debug("Trying to delegate authentication for [{}] to fortress", new Object[]{username});
            final User user = new User(username, password);
            fortressSession = accessManager.createSession(user, false);
            if (fortressSession != null && fortressSession.isAuthenticated()) {
                final StringWriter writer = new StringWriter();
                marshaller.marshal(fortressSession, writer);
                final String fortressXmlSession = writer.toString();
                LOGGER.debug("Fortress session result: [{}]", fortressXmlSession);
                final Map<String, Object> attributes = new HashMap<>();
                attributes.put(FORTRESS_SESSION_KEY, fortressXmlSession);
                return createHandlerResult(usernamePasswordCredential,
                        principalFactory.createPrincipal(username, attributes), null);
            } else {
                LOGGER.warn("Could not establish a fortress session or session cannot authenticate");
            }
        } catch (final org.apache.directory.fortress.core.SecurityException e) {
            final String errorMessage = String.format("Fortress authentication failed for [%s]", username);
            LOGGER.error(errorMessage, e);
            throw new FailedLoginException(errorMessage);
        } catch (final JAXBException e) {
            final String errorMessage = String.format("Cannot marshal fortress session with value: %s", fortressSession);
            LOGGER.warn(errorMessage);
            throw new PreventedException(e);
        }
        throw new FailedLoginException(String.format("[%s] could not authenticate with fortress", username));
    }

    void setAccessManager(final AccessMgr accessManager) {
        this.accessManager = accessManager;
    }
}
