package org.apereo.cas.uma.web.controllers.resource;

import module java.base;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.val;
import org.pac4j.core.profile.UserProfile;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link UmaResourceRegistrationRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Data
public class UmaResourceRegistrationRequest implements Serializable {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Serial
    private static final long serialVersionUID = 3614209506339611242L;
    @JsonProperty("id")
    private long id;

    @JsonProperty
    private String uri;
    @JsonProperty
    private String type;
    @JsonProperty("icon_uri")
    private String iconUri;
    @JsonProperty
    private String name;

    @JsonProperty("resource_scopes")
    private Collection<String> scopes = new LinkedHashSet<>();

    /**
     * As resource set.
     *
     * @param profileResult the profile result
     * @return the resource set
     */
    @JsonIgnore
    public ResourceSet asResourceSet(final UserProfile profileResult) {
        val resourceSet = new ResourceSet();
        resourceSet.setIconUri(getIconUri());
        resourceSet.setId(getId());
        resourceSet.setName(getName());
        resourceSet.setScopes(new HashSet<>(getScopes()));
        resourceSet.setUri(getUri());
        resourceSet.setType(getType());
        resourceSet.setOwner(profileResult.getId());
        resourceSet.setClientId(OAuth20Utils.getClientIdFromAuthenticatedProfile(profileResult));
        return resourceSet;
    }

    /**
     * As json string.
     *
     * @return the string
     */
    @JsonIgnore
    public String toJson() {
        return MAPPER.writeValueAsString(this);
    }
}
