package org.apereo.cas.authentication.metadata;

import module java.base;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * This is {@link CredentialCustomFieldsAttributeMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ToString(callSuper = true)
@RequiredArgsConstructor
public class CredentialCustomFieldsAttributeMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {
    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        transaction
            .getCredentials()
            .stream()
            .filter(this::supports)
            .map(UsernamePasswordCredential.class::cast)
            .forEach(upc -> upc.getCustomFields().forEach((key, value) -> builder.mergeAttribute(key, CollectionUtils.toCollection(value))));
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }
}
