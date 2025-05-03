package org.apereo.cas.uma.ticket.resource;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link ResourceSetPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Table(name = "UMA_ResourceSetPolicy")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@Accessors(chain = true)
public class ResourceSetPolicy implements Serializable {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();
    
    @Serial
    private static final long serialVersionUID = 1664113523427391736L;

    @org.springframework.data.annotation.Id
    @Id
    private long id;

    @Column(columnDefinition = "json")
    @Type(JsonType.class)
    private Set<ResourceSetPolicyPermission> permissions = new HashSet<>();

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
