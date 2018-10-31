package org.apereo.cas.uma.ticket.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.http.HttpStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
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
@Table(name = "UMA_ResourceSet")
@EqualsAndHashCode(of = "id")
@Entity
@NoArgsConstructor
public class ResourceSet implements Serializable {
    private static final long serialVersionUID = -5529923878827427102L;

    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @Column
    private String name;

    @Column
    private String uri;

    @Column
    private String type;

    @Lob
    @Column(length = Integer.MAX_VALUE)
    private HashSet<String> scopes = new HashSet<>();

    @Column
    private String iconUri;

    @Column
    private String owner;

    @Column
    private String clientId;

    @Lob
    @Column(length = Integer.MAX_VALUE)
    private HashSet<ResourceSetPolicy> policies = new HashSet<>();

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
