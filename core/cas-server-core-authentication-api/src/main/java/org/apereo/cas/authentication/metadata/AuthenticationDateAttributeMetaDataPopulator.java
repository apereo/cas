package org.apereo.cas.authentication.metadata;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link AuthenticationDateAttributeMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString(callSuper = true)
@RequiredArgsConstructor
public class AuthenticationDateAttributeMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {
    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        builder.mergeAttribute(AuthenticationManager.AUTHENTICATION_DATE_ATTRIBUTE, ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null;
    }
}
