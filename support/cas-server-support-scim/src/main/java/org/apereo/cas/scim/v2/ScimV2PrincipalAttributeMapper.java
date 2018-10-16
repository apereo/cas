package org.apereo.cas.scim.v2;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;

import com.unboundid.scim2.common.types.Email;
import com.unboundid.scim2.common.types.Name;
import com.unboundid.scim2.common.types.PhoneNumber;
import com.unboundid.scim2.common.types.UserResource;
import lombok.val;

/**
 * This is {@link ScimV2PrincipalAttributeMapper}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ScimV2PrincipalAttributeMapper {

    /**
     * Gets principal attribute value.
     *
     * @param p             the principal
     * @param attributeName the attribute name
     * @return the principal attribute value
     */
    public String getPrincipalAttributeValue(final Principal p, final String attributeName) {
        val attributes = p.getAttributes();
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

        user.setNickName(getPrincipalAttributeValue(p, "nickName"));
        user.setDisplayName(getPrincipalAttributeValue(p, "displayName"));

        val name = new Name();
        name.setGivenName(getPrincipalAttributeValue(p, "givenName"));
        name.setFamilyName(getPrincipalAttributeValue(p, "familyName"));
        name.setMiddleName(getPrincipalAttributeValue(p, "middleName"));

        user.setName(name);

        val email = new Email();
        email.setPrimary(Boolean.TRUE);
        email.setValue(getPrincipalAttributeValue(p, "email"));
        user.setEmails(CollectionUtils.wrap(email));

        val phone = new PhoneNumber();
        phone.setPrimary(Boolean.TRUE);
        phone.setValue(getPrincipalAttributeValue(p, "phoneNumber"));
        user.setPhoneNumbers(CollectionUtils.wrap(phone));
    }
}
