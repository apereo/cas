package org.apereo.cas.util;

import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

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
    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovyUniqueTicketIdGenerator(final Resource groovyResource) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
    }

    @Override
    public String getNewTicketId(final String prefix) {
        val args = new Object[]{prefix, LOGGER};
        return watchableScript.execute(args, String.class);
    }
}
