package org.apereo.cas.uma.web.controllers.permission;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;

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
    private static final int MAP_SIZE = 8;
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules();

    private static final long serialVersionUID = 3614209506339611242L;

    @JsonProperty("resource_id")
    private long resourceId;

    @JsonProperty("resource_scopes")
    private Collection<String> scopes = new LinkedHashSet<>(MAP_SIZE);

    @JsonProperty
    private Map<String, Object> claims = new LinkedHashMap<>(MAP_SIZE);

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
