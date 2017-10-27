package org.apereo.cas.shell.cli;

import org.apache.commons.cli.CommandLine;
import org.apereo.cas.shell.commands.FindPropertiesCommand;
import org.apereo.cas.shell.commands.GenerateCryptoKeysCommand;
import org.apereo.cas.shell.commands.GenerateJwtCommand;
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
        final Pattern propertyPattern = parser.getProperty(line);

        if (parser.isGeneratingKey(line)) {
            final GenerateCryptoKeysCommand cmd = new GenerateCryptoKeysCommand();
            cmd.generateKey(parser.getPropertyValue(line));
        } else if (parser.isGeneratingJwt(line)) {
            final GenerateJwtCommand cmd = new GenerateJwtCommand();
            cmd.generate(parser.getSubject(line));
        } else {
            final FindPropertiesCommand cmd = new FindPropertiesCommand();
            cmd.find(propertyPattern.pattern(), strict, parser.isSummary(line));
        }
    }
}
