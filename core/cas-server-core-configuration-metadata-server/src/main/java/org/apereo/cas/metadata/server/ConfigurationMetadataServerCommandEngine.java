package org.apereo.cas.metadata.server;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link ConfigurationMetadataServerCommandEngine}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ConfigurationMetadataServerCommandEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMetadataServerCommandEngine.class);
    
    /**
     * Execute.
     *
     * @param args   the args
     */
    public void execute(final String[] args) {
        final ConfigurationMetadataServerCommandLineParser parser = new ConfigurationMetadataServerCommandLineParser();
        final CommandLine line = parser.parse(args);
        if (args.length == 0 || parser.isHelp(line)) {
            parser.printHelp();
            return;
        }
    }
}
