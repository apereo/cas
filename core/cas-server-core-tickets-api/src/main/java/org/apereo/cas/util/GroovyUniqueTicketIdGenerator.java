package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;

/**
 * This is {@link ProxyGrantingTicketIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class GroovyUniqueTicketIdGenerator implements UniqueTicketIdGenerator {
    private final ExecutableCompiledScript watchableScript;

    public GroovyUniqueTicketIdGenerator(final Resource groovyResource) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(groovyResource);
    }

    @Override
    public String getNewTicketId(final String prefix) throws Throwable {
        val args = new Object[]{prefix, LOGGER};
        return watchableScript.execute(args, String.class);
    }
}
