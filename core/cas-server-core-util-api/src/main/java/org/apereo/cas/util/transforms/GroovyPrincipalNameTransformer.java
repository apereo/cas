package org.apereo.cas.util.transforms;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.util.ScriptingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

/**
 * A transformer that delegates the transformation to a groovy script.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class GroovyPrincipalNameTransformer implements PrincipalNameTransformer {
    private static final long serialVersionUID = 5167914936775326709L;

    private final Resource script;

    @Override
    public String transform(final String formUserId) {
        return ScriptingUtils.executeGroovyScript(this.script,
            new Object[]{formUserId, LOGGER},
            String.class, true);
    }
}
