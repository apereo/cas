package org.apereo.cas.scim.v1;

import com.unboundid.scim.data.Entry;
import com.unboundid.scim.data.Name;
import com.unboundid.scim.data.UserResource;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;

/**
 * This is {@link Scim1PrincipalAttributeMapper}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class Scim1PrincipalAttributeMapper {

    /**
     * Gets principal attribute value.
     *
     * @param p             the p
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

        user.setNickName(getPrincipalAttributeValue(p, "nickName"));
        user.setDisplayName(getPrincipalAttributeValue(p, "displayName"));

        final Name name = new Name(getPrincipalAttributeValue(p, "formattedName"),
                getPrincipalAttributeValue(p, "familyName"),
                getPrincipalAttributeValue(p, "middleName"),
                getPrincipalAttributeValue(p, "givenName"),
                getPrincipalAttributeValue(p, "honorificPrefix"),
                getPrincipalAttributeValue(p, "honorificSuffix"));
        user.setName(name);

        Entry entry = new Entry(getPrincipalAttributeValue(p, "mail"), "primary");
        user.setEmails(CollectionUtils.wrap(entry));

        entry = new Entry(getPrincipalAttributeValue(p, "phoneNumber"), "primary");
        user.setPhoneNumbers(CollectionUtils.wrap(entry));
    }
}
