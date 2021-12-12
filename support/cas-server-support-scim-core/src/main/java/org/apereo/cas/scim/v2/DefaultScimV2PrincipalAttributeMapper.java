package org.apereo.cas.scim.v2;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;

import com.unboundid.scim2.common.types.Email;
import com.unboundid.scim2.common.types.Meta;
import com.unboundid.scim2.common.types.Name;
import com.unboundid.scim2.common.types.PhoneNumber;
import com.unboundid.scim2.common.types.UserResource;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * This is {@link DefaultScimV2PrincipalAttributeMapper}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultScimV2PrincipalAttributeMapper implements ScimV2PrincipalAttributeMapper {
    @Override
    public void map(final UserResource user, final Principal principal, final Credential credential) {
        user.setUserName(principal.getId());
        if (credential instanceof UsernamePasswordCredential) {
            user.setPassword(UsernamePasswordCredential.class.cast(credential).getPassword());
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

        if (user.getMeta() == null) {
            val meta = new Meta();
            meta.setCreated(Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)));
            meta.setResourceType(user.getUserType());
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
        return StringUtils.defaultString(getPrincipalAttributeValue(principal, attributeName), defaultValue);
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
