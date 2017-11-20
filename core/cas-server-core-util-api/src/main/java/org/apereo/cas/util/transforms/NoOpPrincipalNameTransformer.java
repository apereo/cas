package org.apereo.cas.util.transforms;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;


/**
 * A transformer that does no transformations and returns the principal name as it was provided.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class NoOpPrincipalNameTransformer implements PrincipalNameTransformer {

    private static final long serialVersionUID = 1067914936775326709L;
    
    @Override
    public String transform(final String formUserId) {
        return formUserId.trim();
    }
}
