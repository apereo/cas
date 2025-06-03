package org.apereo.cas.heimdall;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import java.io.Serial;

/**
 * This is {@link AuthorizationResponse}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Data
@NoArgsConstructor
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class AuthorizationResponse extends BaseHeimdallEntity {

    @Serial
    private static final long serialVersionUID = -1703161195540021350L;

    @Builder.Default
    private HttpStatusCode status = HttpStatusCode.valueOf(HttpStatus.UNAUTHORIZED.value());

    private String message;

    public boolean getDecision() {
        return status.is2xxSuccessful();
    }
    
    /**
     * Ok authorization response.
     *
     * @return the authorization response
     */
    public static AuthorizationResponse ok() {
        return new AuthorizationResponse().setStatus(HttpStatus.OK);
    }

    /**
     * Unauthorized authorization response.
     *
     * @param message the message
     * @return the authorization response
     */
    public static AuthorizationResponse unauthorized(final String message) {
        return new AuthorizationResponse().setStatus(HttpStatus.FORBIDDEN).setMessage(message);
    }

    /**
     * Not found authorization response.
     *
     * @param message the message
     * @return the authorization response
     */
    public static AuthorizationResponse notFound(final String message) {
        return new AuthorizationResponse().setStatus(HttpStatus.NOT_FOUND).setMessage(message);
    }
    
}
