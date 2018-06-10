package org.apereo.cas.shell.commands;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Service;

/**
 * This is {@link AnonymousUsernameAttributeProviderCommand}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Service
@Slf4j
public class AnonymousUsernameAttributeProviderCommand implements CommandMarker {
    /**
     * Generate username.
     *
     * @param username the username
     * @param service  the service
     * @param salt     the salt
     */
    @CliCommand(value = "generate-anonymous-user", help = "Generate an anonymous (persistent) username identifier")
    public void generateUsername(
        @CliOption(key = {"username"},
            mandatory = true,
            help = "Authenticated username",
            optionContext = "Authenticated username") final String username,
        @CliOption(key = {"service"},
            mandatory = true,
            help = "Service application URL to generate the identifier for",
            optionContext = "Service application URL to generate the identifier for") final String service,
        @CliOption(key = {"salt"},
            mandatory = true,
            help = "Salt used to generate and encode the anonymous identifier",
            optionContext = "Salt used to generate and encode the anonymous identifier") final String salt) {

        final ShibbolethCompatiblePersistentIdGenerator generator = new ShibbolethCompatiblePersistentIdGenerator(salt);
        final String id = generator.generate(username, service);
        LOGGER.info("Generated identifier:\n[{}]", id);
    }
}
