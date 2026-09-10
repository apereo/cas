package org.apereo.cas.uma.ticket.resource;

import module java.base;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.pac4j.core.profile.UserProfile;
import org.springframework.data.annotation.Id;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

/**
 * This is {@link ResourceSet}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@MappedSuperclass
public class ResourceSet implements Serializable {
    @Serial
    private static final long serialVersionUID = -5529923878827427102L;

    @Id
    @Transient
    @JsonSerialize(using = ToStringSerializer.class)
    private long id;

    @Column
    private String name;

    @Column
    private String uri;

    @Column
    private String type;

    @Column(columnDefinition = "json")
    @Type(JsonType.class)
    private Set<String> scopes = new HashSet<>();

    @Column
    private String iconUri;

    @Column
    private String owner;

    @Column
    private String clientId;
    
    @Column(columnDefinition = "json")
    @Type(JsonType.class)
    private Set<ResourceSetPolicy> policies = new HashSet<>();

    /**
     * Validate.
     *
     * @param profile the profile
     */
    @JsonIgnore
    public void validate(final UserProfile profile) {
        if (StringUtils.isBlank(getClientId())) {
            throw new InvalidResourceSetException(HttpStatus.BAD_REQUEST.value(), "Authentication request does not contain a client id");
        }

        if (getScopes().isEmpty()) {
            throw new InvalidResourceSetException(HttpStatus.BAD_REQUEST.value(), "Resource set registration is missing scopes");
        }

        if (!getOwner().equals(profile.getId())) {
            throw new InvalidResourceSetException(HttpStatus.FORBIDDEN.value(), "Resource-set owner does not match the authenticated profile");
        }

        val authenticatedClientId = OAuth20Utils.getClientIdFromAuthenticatedProfile(profile);
        if (!Objects.equals(getClientId(), authenticatedClientId)) {
            throw new InvalidResourceSetException(HttpStatus.FORBIDDEN.value(), "Resource-set client does not match the authenticated client");
        }
    }
}
