package org.apereo.cas.uma.web.controllers.authz;

import module java.base;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link UmaAuthorizationRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Data
@Accessors(chain = true)
public class UmaAuthorizationRequest implements Serializable {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Serial
    private static final long serialVersionUID = -5359723510084259980L;

    @JsonProperty
    private String ticket;

    @JsonProperty
    private String rpt;

    @JsonProperty("grant_type")
    private String grantType;

    @JsonProperty("claim_token")
    private String claimToken;

    @JsonProperty("claim_token_format")
    private String claimTokenFormat;

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
