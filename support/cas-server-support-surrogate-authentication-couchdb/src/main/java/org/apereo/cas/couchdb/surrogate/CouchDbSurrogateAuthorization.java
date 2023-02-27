package org.apereo.cas.couchdb.surrogate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.ektorp.support.OpenCouchDbDocument;

import java.io.Serial;

/**
 * This is {@link CouchDbSurrogateAuthorization}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 * @deprecated Since 7
 */
@Getter
@Setter
@Deprecated(since = "7.0.0")
public class CouchDbSurrogateAuthorization extends OpenCouchDbDocument {

    @Serial
    private static final long serialVersionUID = 2579922772241446709L;

    /**
     * Surrogate authorized user.
     */
    private String surrogate;

    /**
     * Principal this surrogate is authorized to authenticate as.
     */
    private String principal;

    @JsonCreator
    public CouchDbSurrogateAuthorization(@JsonProperty("surrogate") final String surrogate, @JsonProperty("principal") final String principal) {
        this.surrogate = surrogate;
        this.principal = principal;
    }
}
