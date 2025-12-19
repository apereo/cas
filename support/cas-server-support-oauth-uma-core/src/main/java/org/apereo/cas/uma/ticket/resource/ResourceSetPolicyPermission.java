package org.apereo.cas.uma.ticket.resource;

import module java.base;
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

/**
 * This is {@link ResourceSetPolicyPermission}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Table(name = "UMA_ResourceSetPolicyPermission")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@Accessors(chain = true)
public class ResourceSetPolicyPermission implements Serializable {
    @Serial
    private static final long serialVersionUID = 1664113523427391736L;

    @org.springframework.data.annotation.Id
    @Id
    private long id;

    @Column
    private String subject;

    @Column(columnDefinition = "json")
    @Type(JsonType.class)
    private Set<String> scopes = new HashSet<>();

    @Column(columnDefinition = "json")
    @Type(JsonType.class)
    private Map<String, Object> claims = new LinkedHashMap<>();
}
