package org.apereo.cas.support.openid.authentication.principal;

import org.apereo.cas.authentication.principal.AbstractWebApplicationService;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Scott Battaglia
 * @deprecated 6.2
 * @since 3.1
 */
@Entity
@DiscriminatorValue("openid")
@Getter
@NoArgsConstructor
@Setter
@Deprecated(since = "6.2.0")
@EqualsAndHashCode(callSuper = true)
public class OpenIdService extends AbstractWebApplicationService {

    private static final long serialVersionUID = 5776500133123291301L;

    @Column(nullable = false)
    private String identity;

    @JsonCreator
    protected OpenIdService(@JsonProperty("id") final String id, @JsonProperty("originalUrl") final String originalUrl,
                            @JsonProperty("artifactId") final String artifactId, @JsonProperty("identity") final String identity) {
        super(id, originalUrl, artifactId);
        this.identity = identity;
    }

}
