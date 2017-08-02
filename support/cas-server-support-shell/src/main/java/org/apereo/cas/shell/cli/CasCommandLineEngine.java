package org.apereo.cas.shell.cli;

import org.apache.commons.cli.CommandLine;
import org.apereo.cas.shell.commands.FindCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * This is {@link CasCommandLineEngine}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasCommandLineEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCommandLineEngine.class);
    
    
    /**
     * Execute.
     *
     * @param args the args
     */
    public void execute(final String[] args) {
        final CasCommandLineParser parser = new CasCommandLineParser();
        final CommandLine line = parser.parse(args);
        if (args.length == 0 || parser.isHelp(line)) {
            parser.printHelp();
            return;
        }

        final boolean strict = parser.isStrictMatch(line);
        final Pattern groupPattern = parser.getGroup(line);
        final Pattern propertyPattern = parser.getProperty(line);
        
        final FindCommand cmd = new FindCommand();
        cmd.find(strict, parser.isSummary(line), groupPattern, propertyPattern);

    }
}
