package org.apereo.cas.scim.v2;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;

import de.captaingoldfish.scim.sdk.common.resources.User;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.resources.complex.Name;
import de.captaingoldfish.scim.sdk.common.resources.multicomplex.Email;
import de.captaingoldfish.scim.sdk.common.resources.multicomplex.PhoneNumber;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * This is {@link DefaultScimV2PrincipalAttributeMapper}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultScimV2PrincipalAttributeMapper implements ScimV2PrincipalAttributeMapper {
    @Override
    public void map(final User user, final Principal principal, final Credential credential) {
        user.setUserName(principal.getId());
        if (credential instanceof final UsernamePasswordCredential instance) {
            user.setPassword(instance.toPassword());
        }
        user.setActive(Boolean.TRUE);
        user.setNickName(getPrincipalAttributeValue(principal, "nickName"));
        user.setDisplayName(getPrincipalAttributeValue(principal, "displayName"));

        val name = new Name();
        name.setGivenName(getPrincipalAttributeValue(principal, "givenName"));
        name.setFamilyName(getPrincipalAttributeValue(principal, "familyName"));
        name.setMiddleName(getPrincipalAttributeValue(principal, "middleName"));

        user.setName(name);

        val email = new Email();
        email.setPrimary(Boolean.TRUE);
        email.setValue(getPrincipalAttributeValue(principal, "email"));
        user.setEmails(CollectionUtils.wrap(email));

        val phone = new PhoneNumber();
        phone.setPrimary(Boolean.TRUE);
        phone.setValue(getPrincipalAttributeValue(principal, "phoneNumber"));
        user.setPhoneNumbers(CollectionUtils.wrap(phone));

        user.setExternalId(getPrincipalAttributeValue(principal, "externalId", principal.getId()));
        user.setGroups(null);

        if (user.getMeta().isEmpty()) {
            val meta = new Meta();
            meta.setCreated(LocalDateTime.now(Clock.systemUTC()));
            meta.setResourceType(getPrincipalAttributeValue(principal, "resourceType", "Unknown"));
            user.setMeta(meta);
        }
    }

    /**
     * Gets principal attribute value.
     *
     * @param principal     the principal
     * @param attributeName the attribute name
     * @param defaultValue  the default value
     * @return the principal attribute value
     */
    protected String getPrincipalAttributeValue(final Principal principal,
                                                final String attributeName,
                                                final String defaultValue) {
        return StringUtils.defaultIfBlank(getPrincipalAttributeValue(principal, attributeName), defaultValue);
    }

    /**
     * Gets principal attribute value.
     *
     * @param principal     the principal
     * @param attributeName the attribute name
     * @return the principal attribute value
     */
    protected String getPrincipalAttributeValue(final Principal principal,
                                                final String attributeName) {
        val attributes = principal.getAttributes();
        if (attributes.containsKey(attributeName)) {
            return CollectionUtils.toCollection(attributes.get(attributeName)).iterator().next().toString();
        }
        return null;
    }
}
