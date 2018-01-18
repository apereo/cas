package org.apereo.cas.authentication.metadata;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.util.CollectionUtils;
import java.util.HashSet;
import java.util.Set;

/**
 * Sets an authentication attribute containing the collection of authentication handlers (by name) that successfully
 * authenticated credential. The attribute name is given by {@link AuthenticationHandler#SUCCESSFUL_AUTHENTICATION_HANDLERS}.
 * This component provides a simple method to inject successful handlers into the CAS ticket validation
 * response to support level of assurance and MFA use cases.
 *
 * @author Marvin S. Addison
 * @author Alaa Nassef
 * @since 4.0.0
 */
@Slf4j
@ToString(callSuper = true)
public class SuccessfulHandlerMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        Set<String> successes = builder.getSuccesses().keySet();
        if (successes.isEmpty()) {
            successes = new HashSet(successes);
        }
        builder.mergeAttribute(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, CollectionUtils.wrap(successes));
    }

    @Override
    public boolean supports(final Credential credential) {
        return true;
    }
}
