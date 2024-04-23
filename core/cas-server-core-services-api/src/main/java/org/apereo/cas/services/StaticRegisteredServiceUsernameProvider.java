package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * Returns a static value for the username attribute.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class StaticRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    @Serial
    private static final long serialVersionUID = -3546719400741715137L;

    @ExpressionLanguageCapable
    private String value;

    @Override
    public String resolveUsernameInternal(final RegisteredServiceUsernameProviderContext context) {
        return SpringExpressionLanguageValueResolver.getInstance().resolve(this.value);
    }
}
