package org.apereo.cas.shell.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.RegexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import java.util.regex.Pattern;

/**
 * This is {@link CasCommandLineParser}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasCommandLineParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CasCommandLineParser.class);
    private static final int WIDTH = 120;

    private final CommandLineParser parser;
    private final Options options;

    public CasCommandLineParser() {
        options = new Options();
        options.addOption(CommandLineOptions.OPTION_PROPERTY);
        options.addOption(CommandLineOptions.OPTION_HELP);
        options.addOption(CommandLineOptions.OPTION_SUMMARY);
        options.addOption(CommandLineOptions.OPTION_STRICT_MATCH);
        options.addOption(CommandLineOptions.OPTION_SKIP_BANNER);
        options.addOption(CommandLineOptions.OPTION_SHELL);
        options.addOption(CommandLineOptions.OPTION_GENERATE_KEY);
        options.addOption(CommandLineOptions.OPTION_GENERATE_JWT);
        options.addOption(CommandLineOptions.OPTION_SUBJECT);

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
                LOGGER.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Print help information.
     */
    public void printHelp() {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(WIDTH, "java -jar [cas-server-support-shell-X-Y-Z.jar]",
                "\nCAS Command-line Shell\n", options,
                "\nThe CAS command-line shell provides the ability to query the CAS server "
                        + "for help on available settings/modules and various other utility functions."
                        + "The shell engine is presented as both a CLI utility and an interactive shell."
                        + "\n\nExample use cases include: \n"
                        + "1) Information on a property, such as description, defaults, hints and deprecation.\n"
                        + "2) Generating signing/encryption keys for relevant CAS configuration.\n"
                        + "3) Validating JSON/YAML service definitions for fun and profit.\n"
                        + "4) Retrieving list of available settings for a given module/group.\n"
                        + "5) etc.\n",
                true);
    }

    /**
     * Gets property.
     *
     * @param line the line
     * @return the property
     */
    public Pattern getProperty(final CommandLine line) {
        return RegexUtils.createPattern(getOptionValue(line, CommandLineOptions.OPTION_PROPERTY, ".+"));
    }

    /**
     * Gets subject.
     *
     * @param line the line
     * @return the subject
     */
    public String getSubject(final CommandLine line) {
        return getOptionValue(line, CommandLineOptions.OPTION_SUBJECT, null);
    }

    /**
     * Gets property.
     *
     * @param line the line
     * @return the property
     */
    public String getPropertyValue(final CommandLine line) {
        return getOptionValue(line, CommandLineOptions.OPTION_PROPERTY, StringUtils.EMPTY);
    }

    /**
     * Is help boolean.
     *
     * @param line the line
     * @return the boolean
     */
    public boolean isHelp(final CommandLine line) {
        return line == null || hasOption(line, CommandLineOptions.OPTION_HELP);
    }

    /**
     * Is key gen.
     *
     * @param line the line
     * @return the boolean
     */
    public boolean isGeneratingKey(final CommandLine line) {
        return hasOption(line, CommandLineOptions.OPTION_GENERATE_KEY);
    }

    /**
     * Is jwt gen.
     *
     * @param line the line
     * @return the boolean
     */
    public boolean isGeneratingJwt(final CommandLine line) {
        return hasOption(line, CommandLineOptions.OPTION_GENERATE_JWT);
    }


    /**
     * Is summary boolean.
     *
     * @param line the line
     * @return the boolean
     */
    public boolean isSummary(final CommandLine line) {
        return hasOption(line, CommandLineOptions.OPTION_SUMMARY);
    }

    /**
     * Is strict match boolean.
     *
     * @param line the line
     * @return the boolean
     */
    public boolean isStrictMatch(final CommandLine line) {
        return hasOption(line, CommandLineOptions.OPTION_STRICT_MATCH);
    }

    /**
     * Is skipping banner boolean.
     *
     * @param line the line
     * @return the boolean
     */
    public boolean isSkippingBanner(final CommandLine line) {
        return hasOption(line, CommandLineOptions.OPTION_SKIP_BANNER);
    }


    /**
     * Is skipping banner boolean.
     *
     * @param env the env
     * @return the boolean
     */
    public static boolean isSkippingBanner(final Environment env) {
        return env.containsProperty(CommandLineOptions.OPTION_SKIP_BANNER.getOpt());
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

    /**
     * Gets option value.
     *
     * @param line         the line
     * @param opt          the opt
     * @param defaultValue the default value
     * @return the option value
     */
    public boolean getOptionValue(final CommandLine line, final Option opt, final boolean defaultValue) {
        return line.hasOption(opt.getOpt()) ? Boolean.parseBoolean(line.getOptionValue(opt.getOpt())) : defaultValue;
    }

    /**
     * Has option boolean.
     *
     * @param line the line
     * @param opt  the opt
     * @return the boolean
     */
    public boolean hasOption(final CommandLine line, final Option opt) {
        return line.hasOption(opt.getOpt());
    }

    public Options getOptions() {
        return this.options;
    }

    /**
     * Gets banner mode.
     *
     * @param args the args
     * @return the banner mode
     */
    public static Banner.Mode getBannerMode(final String[] args) {
        final CasCommandLineParser parser = new CasCommandLineParser();
        final CommandLine line = parser.parse(args);
        return (line != null && parser.isSkippingBanner(line) || isShell(args)) ? Banner.Mode.OFF : Banner.Mode.CONSOLE;
    }

    /**
     * Is shell boolean.
     *
     * @param args the args
     * @return the boolean
     */
    public static boolean isShell(final String[] args) {
        final CasCommandLineParser parser = new CasCommandLineParser();
        final CommandLine line = parser.parse(args);
        return line != null && parser.hasOption(line, CommandLineOptions.OPTION_SHELL);
    }

    /**
     * To system.
     *
     * @param args the args
     */
    public static void convertToSystemProperties(final String[] args) {
        final CasCommandLineParser parser = new CasCommandLineParser();
        final CommandLine line = parser.parse(args);
        parser.getOptions().getOptions().forEach(o -> {
            if (parser.hasOption(line, o)) {
                final String optionValue = parser.getOptionValue(line, o, null);
                System.setProperty(o.getOpt(), StringUtils.defaultIfBlank(optionValue, StringUtils.EMPTY));
            }
        });
    }
}
