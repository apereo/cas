package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link ScriptedRegisteredServiceUsernameProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 * @deprecated Since 6.2
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Deprecated(since = "6.2.0")
public class ScriptedRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    private static final long serialVersionUID = -678554831202936052L;

    private String script;

    @Override
    protected String resolveUsernameInternal(final Principal principal, final Service service, final RegisteredService registeredService) {
        try {
            LOGGER.debug("Found groovy script to execute");
            var args = new Object[]{principal.getAttributes(), principal.getId(), LOGGER};
            val result = ScriptingUtils.executeScriptEngine(SpringExpressionLanguageValueResolver.getInstance().resolve(this.script), args, Object.class);
            if (result != null) {
                LOGGER.debug("Found username [{}] from script [{}]", result, this.script);
                return result.toString();
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        LOGGER.warn("Script [{}] returned no value for username attribute. Fallback to default [{}]", this.script, principal.getId());
        return principal.getId();
    }

}
