package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.authentication.attribute.AttributeDefinitionResolutionContext;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinition;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.val;

import java.io.Serial;
import java.util.List;

/**
 * This is {@link SamlIdPAttributeDefinition}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SamlIdPAttributeDefinition extends DefaultAttributeDefinition {
    @Serial
    private static final long serialVersionUID = -144152003366303322L;

    private String friendlyName;

    private String urn;

    private boolean persistent;

    private String salt;

    @Override
    public List<Object> resolveAttributeValues(final AttributeDefinitionResolutionContext context) {
        if (isPersistent()) {
            val generator = new ShibbolethCompatiblePersistentIdGenerator(this.salt);
            return CollectionUtils.wrapList(generator.generate(context.getPrincipal(), context.getService()));
        }
        return super.resolveAttributeValues(context);
    }
}
