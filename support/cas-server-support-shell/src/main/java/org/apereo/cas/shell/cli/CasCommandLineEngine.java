package org.apereo.cas.shell.cli;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.shell.commands.FindPropertiesCommand;
import org.apereo.cas.shell.commands.GenerateCryptoKeysCommand;
import org.apereo.cas.shell.commands.GenerateJwtCommand;

/**
 * This is {@link CasCommandLineEngine}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CasCommandLineEngine {


    /**
     * Execute.
     *
     * @param args the args
     */
    public void execute(final String[] args) {
        final var parser = new CasCommandLineParser();
        final var line = parser.parse(args);
        if (args.length == 0 || parser.isHelp(line)) {
            parser.printHelp();
            return;
        }

        final var strict = parser.isStrictMatch(line);
        final var propertyPattern = parser.getProperty(line);

        if (parser.isGeneratingKey(line)) {
            final var cmd = new GenerateCryptoKeysCommand();
            cmd.generateKey(parser.getPropertyValue(line));
        } else if (parser.isGeneratingJwt(line)) {
            final var cmd = new GenerateJwtCommand();
            cmd.generate(parser.getSubject(line));
        } else {
            final var cmd = new FindPropertiesCommand();
            cmd.find(propertyPattern.pattern(), strict, parser.isSummary(line));
        }
    }
}
