package org.apereo.cas.oidc.claims;

import org.apereo.cas.support.oauth.profile.OAuth20AttributeDefinition;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import java.io.Serial;

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
public class OidcAttributeDefinition extends OAuth20AttributeDefinition {
    @Serial
    private static final long serialVersionUID = -144152663366303322L;

    private boolean structured;

    private String trustFramework;
}
