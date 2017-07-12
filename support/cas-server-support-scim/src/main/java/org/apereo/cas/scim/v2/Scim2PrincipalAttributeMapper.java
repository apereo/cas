package org.apereo.cas.scim.v2;

import com.unboundid.scim2.common.types.Email;
import com.unboundid.scim2.common.types.Name;
import com.unboundid.scim2.common.types.PhoneNumber;
import com.unboundid.scim2.common.types.UserResource;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;

/**
 * This is {@link Scim2PrincipalAttributeMapper}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class Scim2PrincipalAttributeMapper {

    /**
     * Gets principal attribute value.
     *
     * @param p             the principal
     * @param attributeName the attribute name
     * @return the principal attribute value
     */
    public String getPrincipalAttributeValue(final Principal p, final String attributeName) {
        if (p.getAttributes().containsKey(attributeName)) {
            return CollectionUtils.toCollection(p.getAttributes().get(attributeName)).iterator().next().toString();
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
                    final UsernamePasswordCredential credential) {
        user.setUserName(p.getId());
        user.setPassword(credential.getPassword());
        user.setActive(Boolean.TRUE);

        String attr = getPrincipalAttributeValue(p, "nickName");
        user.setNickName(attr);
        attr = getPrincipalAttributeValue(p, "displayName");
        user.setDisplayName(attr);
        
        final Name name = new Name();
        attr = getPrincipalAttributeValue(p, "givenName");
        name.setGivenName(attr);
        attr = getPrincipalAttributeValue(p, "familyName");
        name.setFamilyName(attr);
        attr = getPrincipalAttributeValue(p, "middleName");
        name.setMiddleName(attr);

        user.setName(name);

        final Email email = new Email();
        email.setPrimary(Boolean.TRUE);
        attr = getPrincipalAttributeValue(p, "mail");
        if (StringUtils.isBlank(attr)) {
            attr = getPrincipalAttributeValue(p, "email");
        }
        email.setValue(attr);
        user.setEmails(CollectionUtils.wrap(email));

        final PhoneNumber phone = new PhoneNumber();
        phone.setPrimary(Boolean.TRUE);
        attr = getPrincipalAttributeValue(p, "phone");
        if (StringUtils.isBlank(attr)) {
            attr = getPrincipalAttributeValue(p, "phoneNumber");
        }
        phone.setValue(attr);
        user.setPhoneNumbers(CollectionUtils.wrap(phone));
    }
}
