package org.apereo.cas.util.transforms;

import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.util.ScriptingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;


/**
 * A transformer that delegates the transformation to a groovy script.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class GroovyPrincipalNameTransformer implements PrincipalNameTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyPrincipalNameTransformer.class);

    private static final long serialVersionUID = 5167914936775326709L;

    private Resource script;

    public GroovyPrincipalNameTransformer(final Resource script) {
        this.script = script;
    }

    @Override
    public String transform(final String formUserId) {
        return ScriptingUtils.executeGroovyScript(this.script,
                new Object[]{formUserId, LOGGER},
                String.class);
    }
}
