package org.apereo.cas.services;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.ScriptingUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * This is {@link ScriptedRegisteredServiceUsernameProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class ScriptedRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    private static final long serialVersionUID = -678554831202936052L;

    private String script;

    @Override
    protected String resolveUsernameInternal(final Principal principal, final Service service, final RegisteredService registeredService) {
        try {
            LOGGER.debug("Found groovy script to execute");
            final Object result = ScriptingUtils.executeGroovyScriptEngine(this.script, new Object[]{principal.getAttributes(), principal.getId(), LOGGER}, Object.class);
            if (result != null) {
                LOGGER.debug("Found username [{}] from script [{}]", result, this.script);
                return result.toString();
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.warn("Script [{}] returned no value for username attribute. Fallback to default [{}]", this.script, principal.getId());
        return principal.getId();
    }

}
