package org.jasig.cas.authentication.handler;

import org.springframework.stereotype.Component;

/**
 * Simple implementation that actually does NO transformation.
 *
 * @author Scott Battaglia

 * @since 3.3.6
 */
@Component("noOpPrincipalNameTransformer")
public final class NoOpPrincipalNameTransformer implements PrincipalNameTransformer {

    @Override
    public String transform(final String formUserId) {
        return formUserId;
    }
}
