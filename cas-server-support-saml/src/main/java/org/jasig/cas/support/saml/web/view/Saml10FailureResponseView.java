package org.jasig.cas.support.saml.web.view;


import org.opensaml.saml.saml1.core.Response;
import org.opensaml.saml.saml1.core.StatusCode;

import java.util.Map;

/**
 * Represents a failed attempt at validating a ticket, responding via a SAML SOAP message.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
public final class Saml10FailureResponseView extends AbstractSaml10ResponseView {

    @Override
    protected void prepareResponse(final Response response, final Map<String, Object> model) {
        response.setStatus(this.samlObjectBuilder.newStatus(StatusCode.REQUEST_DENIED, (String) model.get("description")));
    }
}
