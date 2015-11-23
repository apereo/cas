package org.jasig.cas.mock;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.authentication.principal.Service;

import java.util.Map;

/**
 * Simple mock implementation of a service principal.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class MockService implements Service {

    private static final long serialVersionUID = 117438127028057173L;
    private boolean loggedOut;
    private final String id;

    public MockService(final String id) {
        this.id = id;
    }

    public String getArtifactId() {
        return null;
    }

    public Response getResponse(final String ticketId) {
        return null;
    }

    public boolean logOutOfService(final String sessionIdentifier) {
        this.loggedOut = true;
        return false;
    }

    public boolean isLoggedOut() {
        return this.loggedOut;
    }

    @Override
    public void setPrincipal(final Principal principal) {}

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean matches(final Service service) {
        return true;
    }

}
