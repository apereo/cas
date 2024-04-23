package org.apereo.cas.uma.web.controllers.permission;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * This is {@link UmaPermissionRegistrationRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Data
public class UmaPermissionRegistrationRequest implements Serializable {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Serial
    private static final long serialVersionUID = 3614209506339611242L;

    @JsonProperty("resource_id")
    private long resourceId;

    @JsonProperty("resource_scopes")
    private Collection<String> scopes = new LinkedHashSet<>();

    @JsonProperty
    private Map<String, Object> claims = new LinkedHashMap<>();

    /**
     * As json string.
     *
     * @return the string
     */
    @JsonIgnore
    public String toJson() {
        return FunctionUtils.doUnchecked(() -> MAPPER.writeValueAsString(this));
    }
}
