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
    private static final Logger LOG = LoggerFactory.getLogger(FortressAuthenticationHandler.class);
    private static final String FORTRESS_SESSION_KEY = "fortressSession";
    private static final String JAXB_EXCEPTION_MESSAGE = "failed initializa jaxb context";
    private static final String TRACE_ATTEMPT_MESSAGE_TO_FORTRESS = "trying to delegate authentication for {} to fortress";
    private static final String TRACE_FORTRESS_SESSION_RESPONSE = "fortress session result : {}";
    private static final String TRACE_FORTRESS_AUTH_SUCCESS = "returning default handler";

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
            LOG.error(JAXB_EXCEPTION_MESSAGE, e);
        }
        LOG.trace("Fortress authentication handler registered");
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential usernamePasswordCredential,
                                                                 final String originalPassword) throws GeneralSecurityException, PreventedException {
        final String username = usernamePasswordCredential.getUsername();
        final String password = usernamePasswordCredential.getPassword();
        Session fortressSession = null;
        try {
            LOG.trace(TRACE_ATTEMPT_MESSAGE_TO_FORTRESS, new Object[]{username});
            fortressSession = accessManager.createSession(new User(username, password.toCharArray()), false);
            if (fortressSession != null) {
                final StringWriter writer = new StringWriter();
                marshaller.marshal(fortressSession, writer);
                final String fortressXmlSession = writer.toString();
                LOG.trace(TRACE_FORTRESS_SESSION_RESPONSE, fortressXmlSession);
                final Map<String, Object> attributes = new HashMap<>();
                attributes.put(FORTRESS_SESSION_KEY, fortressXmlSession);
                return createHandlerResult(usernamePasswordCredential,
                        principalFactory.createPrincipal(username, attributes), null);
            }
        } catch (final org.apache.directory.fortress.core.SecurityException e) {
            final String errorMessage = "Fortress authentication failed for [" + username + "]";
            LOG.trace(errorMessage, e);
            throw new GeneralSecurityException(errorMessage);
        } catch (final JAXBException e) {
            final String errorMessage = "cannot marshalling session with value : " + fortressSession == null ? "null"
                    : fortressSession.toString();
            LOG.warn(errorMessage);
            throw new GeneralSecurityException(e);
        }
        LOG.trace(TRACE_FORTRESS_AUTH_SUCCESS);
        return createHandlerResult(usernamePasswordCredential, principalFactory.createPrincipal(username), null);
    }

    public AccessMgr getAccessManager() {
        return accessManager;
    }

    public void setAccessManager(final AccessMgr accessManager) {
        this.accessManager = accessManager;
    }
}
