package org.apereo.cas.couchdb.gauth;

import org.apereo.cas.authentication.OneTimeToken;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * This is {@link CouchDbOneTimeToken}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
public class CouchDbOneTimeToken extends OneTimeToken {

    private static final long serialVersionUID = 2768980948846869252L;

    @JsonProperty("_id")
    private String cid;

    @JsonProperty("_rev")
    private String rev;

    @JsonCreator
    public CouchDbOneTimeToken(@JsonProperty("_id") final String cid, //NOPMD
        @JsonProperty("_rev") final String rev,
        @JsonProperty("id") final long id,
        @JsonProperty("token") @NonNull final Integer token,
        @JsonProperty("userId") @NonNull final String userId,
        @JsonProperty("issuedDateTime") final LocalDateTime issuedDateTime
    ) {
        super(token, userId);
        this.cid = cid;
        this.rev = rev;
        setId(id);
        setIssuedDateTime(issuedDateTime);
    }

    public CouchDbOneTimeToken(final OneTimeToken oneTimeToken) {
        setId(oneTimeToken.getId());
        setIssuedDateTime(oneTimeToken.getIssuedDateTime());
        setToken(oneTimeToken.getToken());
        setUserId(oneTimeToken.getUserId());
    }
}
