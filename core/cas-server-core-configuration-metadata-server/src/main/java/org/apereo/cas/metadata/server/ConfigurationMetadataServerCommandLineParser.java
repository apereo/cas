package org.apereo.cas.metadata.server;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apereo.cas.util.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link ConfigurationMetadataServerCommandLineParser}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ConfigurationMetadataServerCommandLineParser {
    private enum QueryCommands {
        GROUPS,
        PROPERTIES
    }

    /**
     * Command line option that indicates a property.
     */
    public static final Option OPTION_COMMAND = OptionBuilder
            .withLongOpt("command")
            .hasArg()
            .withType(QueryCommands.class)
            .withDescription("Command to execute via the query engine. Accepted commands are: ["
                    + Arrays.stream(QueryCommands.values()).map(s -> s.toString().toLowerCase()).collect(Collectors.joining(",")) + "]")
            .create("c");

    /**
     * Command line option that indicates a property.
     */
    public static final Option OPTION_PROPERTY = OptionBuilder
            .withLongOpt("property")
            .hasArg()
            .withDescription("A regular expression filter to indicate the property name (i.e. cas.server.name).")
            .create("p");

    /**
     * Command line option that indicates a group.
     */
    public static final Option OPTION_GROUP = OptionBuilder
            .hasArg()
            .withLongOpt("group")
            .withDescription("A regular expression filter to indicate the group name (i.e. cas.authn).")
            .create("g");


    /**
     * Command line option that indicates help information should be displayed.
     */
    public static final Option OPTION_HELP = OptionBuilder
            .withDescription("Print help and usage information.")
            .withLongOpt("help")
            .create("h");

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMetadataServerCommandLineParser.class);
    private static final int WIDTH = 120;

    private final CommandLineParser parser;
    private final Options options;

    public ConfigurationMetadataServerCommandLineParser() {
        OPTION_COMMAND.setRequired(true);

        options = new Options();
        options.addOption(OPTION_GROUP);
        options.addOption(OPTION_PROPERTY);
        options.addOption(OPTION_HELP);
        options.addOption(OPTION_COMMAND);
        parser = new DefaultParser();
    }

    /**
     * Parse command line.
     *
     * @param args the args
     * @return the command line
     */
    public CommandLine parse(final String[] args) {
        try {
            return parser.parse(options, args);
        } catch (final Exception e) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Print help information.
     */
    public void printHelp() {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(WIDTH, "java -jar [cas-server-core-configuration-metadata-server-X-Y-Z.jar]",
                "\nCAS Configuration Metadata Server\n", options,
                "\nThe CAS Configuration Metadata Server provides the ability to query the CAS server "
                        + "for help on available settings and modules. The query engine "
                        + "is presented as a CLI utility that is able to accept parameters on groups/settings "
                        + "and report back results.\n\nExample use cases include: \n"
                        + "1) Information on a property, such as description, defaults, hints and deprecation.\n"
                        + "2) Retrieving list of available settings for a given module/group.\n",
                true);
    }

    /**
     * Gets property.
     *
     * @param line the line
     * @return the property
     */
    public Pattern getProperty(final CommandLine line) {
        return RegexUtils.createPattern(getOptionValue(line, OPTION_PROPERTY, ".+"));
    }

    /**
     * Get group or module.
     *
     * @param line the line
     * @return the string
     */
    public Pattern getGroup(final CommandLine line) {
        return RegexUtils.createPattern(getOptionValue(line, OPTION_GROUP, ".+"));
    }

    /**
     * Is help boolean.
     *
     * @param line the line
     * @return the boolean
     */
    public boolean isHelp(final CommandLine line) {
        return line == null || line.getArgList().isEmpty() || line.hasOption(OPTION_HELP.getOpt());
    }

    /**
     * Gets option value.
     *
     * @param line         the line
     * @param opt          the opt
     * @param defaultValue the default value
     * @return the option value
     */
    public String getOptionValue(final CommandLine line, final Option opt, final String defaultValue) {
        return line.hasOption(opt.getOpt()) ? line.getOptionValue(opt.getOpt()) : defaultValue;
    }
}
