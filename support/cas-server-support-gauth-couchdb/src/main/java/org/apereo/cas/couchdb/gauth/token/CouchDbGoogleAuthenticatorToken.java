package org.apereo.cas.couchdb.gauth.token;

import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * This is {@link CouchDbGoogleAuthenticatorToken}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
public class CouchDbGoogleAuthenticatorToken extends GoogleAuthenticatorToken {

    private static final long serialVersionUID = 2768980948846869252L;

    @JsonProperty("_id")
    private String cid;

    @JsonProperty("_rev")
    private String rev;

    @JsonCreator
    public CouchDbGoogleAuthenticatorToken(@JsonProperty("_id") final String cid,
                                           @JsonProperty("_rev") final String rev,
                                           @JsonProperty("id") final long id,
                                           @JsonProperty("token") final @NonNull Integer token,
                                           @JsonProperty("userId") final @NonNull String userId,
                                           @JsonProperty("issuedDateTime") final LocalDateTime issuedDateTime
    ) {
        super(token, userId);
        this.cid = cid;
        this.rev = rev;
        setId(id);
        setIssuedDateTime(issuedDateTime);
    }

    public CouchDbGoogleAuthenticatorToken(final OneTimeToken oneTimeToken) {
        setId(oneTimeToken.getId());
        setIssuedDateTime(oneTimeToken.getIssuedDateTime());
        setToken(oneTimeToken.getToken());
        setUserId(oneTimeToken.getUserId());
    }
}
