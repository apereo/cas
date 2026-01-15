package org.apereo.cas.shell.commands.services;

import module java.base;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.apereo.cas.shell.commands.CasShellCommand;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;

/**
 * This is {@link AnonymousUsernameAttributeProviderCommand}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class AnonymousUsernameAttributeProviderCommand implements CasShellCommand {
    /**
     * Generate username.
     *
     * @param username the username
     * @param service  the service
     * @param salt     the salt
     * @return the string
     */
    @Command(group = "Registered Services", name = "generate-anonymous-user", description = "Generate an anonymous (persistent) username identifier")
    public String generateUsername(
        @Option(
            longName = "username",
            description = "Authenticated username"
        )
        final String username,

        @Option(
            longName = "service",
            description = "Service application URL for which CAS may generate the identifier"
        )
        final String service,

        @Option(
            longName = "salt",
            description = "Salt used to generate and encode the anonymous identifier"
        )
        final String salt) {
        val generator = new ShibbolethCompatiblePersistentIdGenerator(salt);
        val id = generator.generate(username, service);
        LOGGER.info("Generated identifier:\n[{}]", id);
        return id;
    }
}
