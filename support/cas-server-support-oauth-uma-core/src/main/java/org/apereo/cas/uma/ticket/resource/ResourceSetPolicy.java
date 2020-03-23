package org.apereo.cas.uma.ticket.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import java.io.Serializable;
import java.util.HashSet;

/**
 * This is {@link ResourceSetPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Embeddable
@Table(name = "UMA_ResourceSetPolicy")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class ResourceSetPolicy implements Serializable {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules();
    
    private static final long serialVersionUID = 1664113523427391736L;

    @org.springframework.data.annotation.Id
    @Id
    private long id;

    @Lob
    @Column(length = Integer.MAX_VALUE)
    private HashSet<ResourceSetPolicyPermission> permissions = new HashSet<>(0);

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
