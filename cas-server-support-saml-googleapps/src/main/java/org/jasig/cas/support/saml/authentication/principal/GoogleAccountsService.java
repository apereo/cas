package org.jasig.cas.support.saml.authentication.principal;


import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.authentication.principal.ResponseBuilder;
import org.jasig.cas.authentication.principal.WebApplicationService;
/**
 * Implementation of a Service that supports Google Accounts (eventually a more
 * generic SAML2 support will come).
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class GoogleAccountsService extends AbstractWebApplicationService {

    private static final long serialVersionUID = 6678711809842282833L;

    private final String relayState;

    private final String requestId;

    /**
     * Instantiates a new google accounts service.
     *
     * @param id the id
     * @param relayState the relay state
     * @param requestId the request id
     * @param responseBuilder the response builder
     */
    protected GoogleAccountsService(final String id, final String relayState, final String requestId,
                                    final ResponseBuilder<WebApplicationService> responseBuilder) {
        super(id, id, null, responseBuilder);
        this.relayState = relayState;
        this.requestId = requestId;
    }

    /**
     * Return true if the service is already logged out.
     *
     * @return true if the service is already logged out.
     */
    @Override
    public boolean isLoggedOutAlready() {
        return true;
    }


    public String getRelayState() {
        return relayState;
    }

    public String getRequestId() {
        return requestId;
    }
}
