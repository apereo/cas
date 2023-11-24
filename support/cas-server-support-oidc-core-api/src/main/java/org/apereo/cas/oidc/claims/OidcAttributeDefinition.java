package org.apereo.cas.oidc.claims;

import org.apereo.cas.authentication.attribute.DefaultAttributeDefinition;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import java.io.Serial;
import java.util.List;

/**
 * This is {@link OidcAttributeDefinition}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class OidcAttributeDefinition extends DefaultAttributeDefinition {
    @Serial
    private static final long serialVersionUID = -144152663366303322L;

    private boolean singleValue;

    private boolean structured;

    private String trustFramework;

    /**
     * To attribute value.
     *
     * @param values the values
     * @return the object
     */
    @JsonIgnore
    public Object toAttributeValue(final List values) {
        return singleValue && values.size() == 1 ? values.getFirst() : values;
    }
}
