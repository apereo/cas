package org.apereo.cas.scim.v2;

import com.unboundid.scim2.common.types.Email;
import com.unboundid.scim2.common.types.Name;
import com.unboundid.scim2.common.types.PhoneNumber;
import com.unboundid.scim2.common.types.UserResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;

/**
 * This is {@link ScimV2PrincipalAttributeMapper}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class ScimV2PrincipalAttributeMapper {

    /**
     * Gets principal attribute value.
     *
     * @param p             the principal
     * @param attributeName the attribute name
     * @return the principal attribute value
     */
    public String getPrincipalAttributeValue(final Principal p, final String attributeName) {
        final var attributes = p.getAttributes();
        if (attributes.containsKey(attributeName)) {
            return CollectionUtils.toCollection(attributes.get(attributeName)).iterator().next().toString();
        }
        return null;
    }

    /**
     * Map.
     *
     * @param user       the user
     * @param p          the p
     * @param credential the credential
     */
    public void map(final UserResource user, final Principal p,
                    final Credential credential) {
        user.setUserName(p.getId());
        if (credential instanceof UsernamePasswordCredential) {
            user.setPassword(UsernamePasswordCredential.class.cast(credential).getPassword());
        }
        user.setActive(Boolean.TRUE);

        var attr = getPrincipalAttributeValue(p, "nickName");
        user.setNickName(attr);
        attr = getPrincipalAttributeValue(p, "displayName");
        user.setDisplayName(attr);

        final var name = new Name();
        attr = getPrincipalAttributeValue(p, "givenName");
        name.setGivenName(attr);
        attr = getPrincipalAttributeValue(p, "familyName");
        name.setFamilyName(attr);
        attr = getPrincipalAttributeValue(p, "middleName");
        name.setMiddleName(attr);

        user.setName(name);

        final var email = new Email();
        email.setPrimary(Boolean.TRUE);
        attr = getPrincipalAttributeValue(p, "mail");
        if (StringUtils.isBlank(attr)) {
            attr = getPrincipalAttributeValue(p, "email");
        }
        email.setValue(attr);
        user.setEmails(CollectionUtils.wrap(email));

        final var phone = new PhoneNumber();
        phone.setPrimary(Boolean.TRUE);
        attr = getPrincipalAttributeValue(p, "phone");
        if (StringUtils.isBlank(attr)) {
            attr = getPrincipalAttributeValue(p, "phoneNumber");
        }
        phone.setValue(attr);
        user.setPhoneNumbers(CollectionUtils.wrap(phone));
    }
}
