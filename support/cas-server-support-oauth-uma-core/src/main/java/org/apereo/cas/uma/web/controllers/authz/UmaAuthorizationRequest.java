package org.apereo.cas.uma.web.controllers.authz;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
    @SneakyThrows
    public String toJson() {
        return MAPPER.writeValueAsString(this);
    }
}
