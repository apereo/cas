package org.apereo.cas.authentication;

import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

/**
 * This is {@link GroovyAuthenticationPreProcessor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@Slf4j
public class GroovyAuthenticationPreProcessor implements AuthenticationPreProcessor, DisposableBean {
    private final transient WatchableGroovyScriptResource watchableScript;
    private int order;

    public GroovyAuthenticationPreProcessor(final Resource groovyResource) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
    }

    @Override
    public boolean process(final AuthenticationTransaction transaction) throws AuthenticationException {
        val args = new Object[]{transaction, LOGGER};
        return watchableScript.execute(args, Boolean.class);
    }

    @Override
    public boolean supports(final Credential credential) {
        val args = new Object[]{credential, LOGGER};
        return watchableScript.execute("supports", Boolean.class, args);
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
