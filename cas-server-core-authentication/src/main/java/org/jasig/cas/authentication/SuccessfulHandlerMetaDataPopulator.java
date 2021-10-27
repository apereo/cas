package org.jasig.cas.authentication;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Sets an authentication attribute containing the collection of authentication handlers (by name) that successfully
 * authenticated credential. The attribute name is given by {@link #SUCCESSFUL_AUTHENTICATION_HANDLERS}.
 * This component provides a simple method to inject successful handlers into the CAS ticket validation
 * response to support level of assurance and MFA use cases.
 *
 * @author Marvin S. Addison
 * @author Alaa Nassef
 * @since 4.0.0
 */

@Component("successfulHandlerMetaDataPopulator")
public class SuccessfulHandlerMetaDataPopulator implements AuthenticationMetaDataPopulator {
    /** Attribute name containing collection of handler names that successfully authenticated credential. */
    public static final String SUCCESSFUL_AUTHENTICATION_HANDLERS = "successfulAuthenticationHandlers";

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        Set<String> successes = builder.getSuccesses().keySet();
        if (successes != null) {
            successes = new HashSet(successes);
        }
        
        builder.addAttribute(SUCCESSFUL_AUTHENTICATION_HANDLERS, successes);
    }

    @Override
    public boolean supports(final Credential credential) {
        return true;
    }
}
