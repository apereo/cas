package org.apereo.cas.shell.commands.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * This is {@link AnonymousUsernameAttributeProviderCommand}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@ShellCommandGroup("Registered Services")
@ShellComponent
@Slf4j
public class AnonymousUsernameAttributeProviderCommand {
    /**
     * Generate username.
     *
     * @param username the username
     * @param service  the service
     * @param salt     the salt
     */
    @ShellMethod(key = "generate-anonymous-user", value = "Generate an anonymous (persistent) username identifier")
    public void generateUsername(
        @ShellOption(value = {"username"},
            help = "Authenticated username") final String username,
        @ShellOption(value = {"service"},
            help = "Service application URL for which CAS may generate the identifier") final String service,
        @ShellOption(value = {"salt"},
            help = "Salt used to generate and encode the anonymous identifier") final String salt) {
        final var generator = new ShibbolethCompatiblePersistentIdGenerator(salt);
        final var id = generator.generate(username, service);
        LOGGER.info("Generated identifier:\n[{}]", id);
    }
}
