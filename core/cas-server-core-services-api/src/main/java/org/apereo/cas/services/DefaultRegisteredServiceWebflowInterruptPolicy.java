package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RegularExpressionCapable;
import org.apereo.cas.configuration.support.TriStateBoolean;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link DefaultRegisteredServiceWebflowInterruptPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DefaultRegisteredServiceWebflowInterruptPolicy implements RegisteredServiceWebflowInterruptPolicy {
    @Serial
    private static final long serialVersionUID = -9011530431859480167L;

    private boolean enabled = true;

    private TriStateBoolean forceExecution = TriStateBoolean.UNDEFINED;

    @ExpressionLanguageCapable
    @RegularExpressionCapable
    private String attributeName;

    @ExpressionLanguageCapable
    @RegularExpressionCapable
    private String attributeValue;

    @ExpressionLanguageCapable
    private String groovyScript;
}
