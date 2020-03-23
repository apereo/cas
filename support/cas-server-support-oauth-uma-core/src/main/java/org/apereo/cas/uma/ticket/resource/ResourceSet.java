package org.apereo.cas.uma.ticket.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.data.annotation.Id;
import org.springframework.http.HttpStatus;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.io.Serializable;
import java.util.HashSet;

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
    private static final long serialVersionUID = -5529923878827427102L;

    @Id
    @Transient
    private long id;

    @Column
    private String name;

    @Column
    private String uri;

    @Column
    private String type;

    @Lob
    @Column(length = Integer.MAX_VALUE)
    private HashSet<String> scopes = new HashSet<>(0);

    @Column
    private String iconUri;

    @Column
    private String owner;

    @Column
    private String clientId;

    @Lob
    @Column(length = Integer.MAX_VALUE)
    private HashSet<ResourceSetPolicy> policies = new HashSet<>(0);

    /**
     * Validate.
     *
     * @param profile the profile
     */
    @JsonIgnore
    public void validate(final CommonProfile profile) {
        if (StringUtils.isBlank(getClientId())) {
            throw new InvalidResourceSetException(HttpStatus.BAD_REQUEST.value(), "Authentication request does contain a client id");
        }

        if (getScopes().isEmpty()) {
            throw new InvalidResourceSetException(HttpStatus.BAD_REQUEST.value(), "Resource set registration is missing scopes");
        }

        if (!getOwner().equals(profile.getId())) {
            throw new InvalidResourceSetException(HttpStatus.FORBIDDEN.value(), "Resource-set owner does not match the authenticated profile");
        }
    }
}
