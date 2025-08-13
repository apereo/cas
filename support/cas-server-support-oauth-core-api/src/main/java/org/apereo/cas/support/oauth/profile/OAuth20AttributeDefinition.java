package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.authentication.attribute.DefaultAttributeDefinition;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import java.io.Serial;

/**
 * This is {@link OAuth20AttributeDefinition}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class OAuth20AttributeDefinition extends DefaultAttributeDefinition {
    @Serial
    private static final long serialVersionUID = -122152663366303322L;
}
