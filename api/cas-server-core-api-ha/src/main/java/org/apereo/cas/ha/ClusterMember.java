package org.apereo.cas.ha;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link ClusterMember}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@SuperBuilder
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
@AllArgsConstructor
@With
public class ClusterMember implements Serializable {
    @Serial
    private static final long serialVersionUID = 1659099897056632608L;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String owner;
    private String id;
    private String address;
    private long responseTime;
    private boolean status;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String description;

    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> attributes = new LinkedHashMap<>();
}
