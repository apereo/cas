package org.apereo.cas.authentication.metadata;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.util.StringUtils;

/**
 * This is {@link AuthenticationContextAttributeMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@RequiredArgsConstructor
public class AuthenticationContextAttributeMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {
    private final String authenticationContextAttribute;

    private final AuthenticationHandler authenticationHandler;

    private final String authenticationContextAttributeValue;

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        if (builder.hasAttribute(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE,
            obj -> obj.toString().equals(authenticationHandler.getName()))) {
            StringUtils.commaDelimitedListToSet(authenticationContextAttribute).forEach(attribute ->
                builder.mergeAttribute(attribute, authenticationContextAttributeValue));
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return this.authenticationHandler.supports(credential);
    }
}
