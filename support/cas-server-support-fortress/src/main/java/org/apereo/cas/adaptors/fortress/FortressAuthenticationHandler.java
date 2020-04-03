package org.apereo.cas.adaptors.fortress;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.directory.fortress.core.AccessMgr;
import org.apache.directory.fortress.core.model.Session;
import org.apache.directory.fortress.core.model.User;

import javax.security.auth.login.FailedLoginException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;

/**
 * Fortress authentication handler, this class will delegate the authentication to call fortress rest authentication.
 *
 * @author yudhi.k.surtan
 * @since 5.2.0.
 */
@Slf4j
@Setter
public class FortressAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    /**
     * Fortress key to look up session as an attribute.
     */
    public static final String FORTRESS_SESSION_KEY = "fortressSession";

    private AccessMgr accessManager;
    private Marshaller marshaller;

    public FortressAuthenticationHandler(final AccessMgr accessManager, final String name, final ServicesManager servicesManager,
                                         final PrincipalFactory principalFactory, final Integer order) {
        super(name, servicesManager, principalFactory, order);
        this.accessManager = accessManager;
        try {
            val jaxbContext = JAXBContext.newInstance(Session.class);
            this.marshaller = jaxbContext.createMarshaller();
        } catch (final Exception e) {
            LOGGER.error("Failed initialize fortress context", e);
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential c,
                                                                                        final String originalPassword)
        throws GeneralSecurityException, PreventedException {
        val username = c.getUsername();
        val password = c.getPassword();
        try {
            LOGGER.debug("Trying to delegate authentication for [{}] to fortress", username);
            val user = new User(username, password);
            val fortressSession = accessManager.createSession(user, false);
            if (fortressSession != null && fortressSession.isAuthenticated()) {
                val writer = new StringWriter();
                marshaller.marshal(fortressSession, writer);
                val fortressXmlSession = writer.toString();
                LOGGER.debug("Fortress session result: [{}]", fortressXmlSession);
                val attributes = new HashMap<String, List<Object>>();
                attributes.put(FORTRESS_SESSION_KEY, CollectionUtils.wrapList(fortressXmlSession));
                return createHandlerResult(c, principalFactory.createPrincipal(username, attributes));
            }
            LOGGER.warn("Could not establish a fortress session or session cannot authenticate");
        } catch (final org.apache.directory.fortress.core.SecurityException e) {
            val errorMessage = String.format("Fortress authentication failed for [%s]", username);
            LOGGER.error(errorMessage, e);
            throw new FailedLoginException(errorMessage);
        } catch (final JAXBException e) {
            LOGGER.warn("Cannot marshal fortress session with value", e);
            throw new PreventedException(e);
        }
        throw new FailedLoginException(String.format("[%s] could not authenticate with fortress", username));
    }
}
