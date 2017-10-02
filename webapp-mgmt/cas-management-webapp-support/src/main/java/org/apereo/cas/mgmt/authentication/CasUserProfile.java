package org.apereo.cas.mgmt.authentication;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;

import java.util.Collection;

/**
 * This is {@link CasUserProfile}.
 *
 * @author Misagh Moayyed
 * @author Travis Schmidt
 * @since 5.2.0
 */
public class CasUserProfile extends CommonProfile {
    private static final long serialVersionUID = -6308325782274816263L;
    private final boolean admin;
    
    public CasUserProfile(final UserProfile up, final Collection<String> adminRoles) {
        build(up.getId(), up.getAttributes());
        setClientName(up.getClientName());
        setLinkedId(up.getLinkedId());
        setRemembered(up.isRemembered());
        addRoles(up.getRoles());
        addPermissions(up.getPermissions());
        
        this.admin = adminRoles.stream().anyMatch(r -> getRoles().contains(r));
    }

    public boolean isAdministrator() {
        return this.admin;
    }

    public String getDepartment() {
        return findFirstMatchingAttribute("department|ou");
    }

    public String getPhone() {
        return findFirstMatchingAttribute("phone|phoneNumber|telephoneNumber|primaryPhone|primaryPhoneNumber");
    }

    private String findFirstMatchingAttribute(final String pattern) {
        return getAttributes().entrySet()
                .stream()
                .filter(entry -> entry.getKey().matches(pattern))
                .map(e -> e.getValue().toString())
                .findFirst()
                .orElse(null);
    }
}
