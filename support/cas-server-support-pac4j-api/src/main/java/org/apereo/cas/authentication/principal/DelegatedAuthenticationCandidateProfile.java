package org.apereo.cas.authentication.principal;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link DelegatedAuthenticationCandidateProfile}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SuperBuilder
@Getter
@ToString(of = {"id", "linkedId"})
public class DelegatedAuthenticationCandidateProfile implements Serializable {
    @Serial
    private static final long serialVersionUID = 6377783357802049769L;

    @NonNull
    private final String key;

    @NonNull
    private final String id;

    @NonNull
    private final String linkedId;

    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * To user profile based on given client name.
     *
     * @param clientName the client name
     * @return the user profile
     */
    public UserProfile toUserProfile(final String clientName) {
        val userProfile = new CommonProfile();
        userProfile.setId(this.id);
        userProfile.setLinkedId(this.linkedId);
        userProfile.addAttributes(this.attributes);
        userProfile.setClientName(clientName);
        return userProfile;
    }
}
