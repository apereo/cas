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
    /**
     * Command line option that indicates a shell should be launched.
     */
    public static final Option OPTION_SHELL = Option.builder("sh")
            .longOpt("shell")
            .desc("Launch into a CAS interactive shell to execute commands. Activating this option will disable "
                    + "basic CLI capabilities and allows you to interactively query the CAS configuration metadata server.")
            .build();

    /**
     * Command line option that indicates a property.
     */
    public static final Option OPTION_PROPERTY = Option.builder("p")
            .longOpt("property")
            .hasArg()
            .desc("A regular expression filter to indicate the property name (i.e. cas.server.name).")
            .build();

    /**
     * Command line option that indicates a group.
     */
    public static final Option OPTION_GROUP = Option.builder("g")
            .hasArg()
            .longOpt("group")
            .desc("A regular expression filter to indicate the group name (i.e. cas.authn).")
            .build();

    /**
     * Command line option that indicates a summary.
     */
    public static final Option OPTION_SUMMARY = Option.builder("su")
            .longOpt("summary")
            .desc("Display a compact version of the query results; Summarize output.")
            .build();

    /**
     * Command line option that indicates a strict-mode matching.
     */
    public static final Option OPTION_STRICT_MATCH = Option.builder("sm")
            .longOpt("strict-match")
            .desc("Control whether pattern matching should be done in strict mode which means "
                    + "the matching engine tries to match the entire region for the query.")
            .build();

    /**
     * Command line option that indicates banner should be skipped.
     */
    public static final Option OPTION_SKIP_BANNER = Option.builder("skb")
            .desc("Skip printing the CAS banner.")
            .longOpt("skip-banner")
            .build();

    /**
     * Command line option that indicates help information should be displayed.
     */
    public static final Option OPTION_HELP = Option.builder("h")
            .desc("Print help and usage information.")
            .longOpt("help")
            .build();

    private static final Logger LOGGER = LoggerFactory.getLogger(CasCommandLineParser.class);
    private static final int WIDTH = 120;

    private final CommandLineParser parser;
    private final Options options;

    public CasCommandLineParser() {
        options = new Options();
        options.addOption(OPTION_GROUP);
        options.addOption(OPTION_PROPERTY);
        options.addOption(OPTION_HELP);
        options.addOption(OPTION_SUMMARY);
        options.addOption(OPTION_STRICT_MATCH);
        options.addOption(OPTION_SKIP_BANNER);
        options.addOption(OPTION_SHELL);
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
                        + "4) etc.\n",
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
        return line == null || hasOption(line, OPTION_HELP);
    }

    /**
     * Is summary boolean.
     *
     * @param line the line
     * @return the boolean
     */
    public boolean isSummary(final CommandLine line) {
        return hasOption(line, OPTION_SUMMARY);
    }

    /**
     * Is strict match boolean.
     *
     * @param line the line
     * @return the boolean
     */
    public boolean isStrictMatch(final CommandLine line) {
        return hasOption(line, OPTION_STRICT_MATCH);
    }

    /**
     * Is skipping banner boolean.
     *
     * @param line the line
     * @return the boolean
     */
    public boolean isSkippingBanner(final CommandLine line) {
        return hasOption(line, OPTION_SKIP_BANNER);
    }


    /**
     * Is skipping banner boolean.
     *
     * @param env the env
     * @return the boolean
     */
    public static boolean isSkippingBanner(final Environment env) {
        return env.containsProperty(OPTION_SKIP_BANNER.getOpt());
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
        return line.hasOption(opt.getOpt()) ? Boolean.valueOf(line.getOptionValue(opt.getOpt())) : defaultValue;
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
        return line != null && parser.hasOption(line, OPTION_SHELL);
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
