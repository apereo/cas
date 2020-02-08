package org.apereo.cas.authentication.attribute;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.CompareToBuilder;

/**
 * This is {@link DefaultAttributeDefinition}.
 *
 * @author Misagh Moayyed
 * @author Travis Schmidt
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@EqualsAndHashCode(of = "key")
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class DefaultAttributeDefinition implements AttributeDefinition {
    private static final long serialVersionUID = 6898745248727445565L;

    private String key;

    private String name;

    private boolean scoped;

    private String attribute;

    private String patternFormat;

    private String script;

    @Override
    public int compareTo(final AttributeDefinition o) {
        return new CompareToBuilder()
            .append(getKey(), o.getKey())
            .build();
    }
}
