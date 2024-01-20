package org.apereo.cas.util.spring.boot;

import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.SystemUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Formatter;
import java.util.ServiceLoader;

/**
 * This is {@link AbstractCasBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractCasBanner implements CasBanner {

    private static final int SEPARATOR_REPEAT_COUNT = 60;

    private static final String SEPARATOR_CHAR = "-";

    protected static final String LINE_SEPARATOR = String.join(StringUtils.EMPTY,
        Collections.nCopies(SEPARATOR_REPEAT_COUNT, SEPARATOR_CHAR));

    @Override
    public void printBanner(final Environment environment, final Class<?> sourceClass, final PrintStream out) {
        AsciiArtUtils.printAsciiArt(out, getTitle(), collectEnvironmentInfo(environment, sourceClass));
    }

    @Override
    public String getTitle() {
        return """
                ____  ____    ___  ____     ___   ___          __   ____  _____
               /    T|    \\  /  _]|    \\   /  _] /   \\        /  ] /    T/ ___/
              Y  o  ||  o  )/  [_ |  D  ) /  [_ Y     Y      /  / Y  o  (   \\_\s
              |     ||   _/Y    _]|    / Y    _]|  O  |     /  /  |     |\\__  T
              |  _  ||  |  |   [_ |    \\ |   [_ |     |    /   \\_ |  _  |/  \\ |
              |  |  ||  |  |     T|  .  Y|     Tl     !    \\     ||  |  |\\    |
              l__j__jl__j  l_____jl__j\\_jl_____j \\___/      \\____jl__j__j \\___j
            """;
    }

    /**
     * Collect environment info with
     * details on the java and os deployment
     * versions.
     *
     * @param environment the environment
     * @param sourceClass the source class
     * @return environment info
     */
    private String collectEnvironmentInfo(final Environment environment, final Class<?> sourceClass) {
        val properties = System.getProperties();
        if (properties.containsKey("CAS_BANNER_SKIP")) {
            try (val formatter = new Formatter()) {
                formatter.format("CAS Version: %s%n", CasVersion.getVersion());
                return formatter.toString();
            }
        }

        try (val formatter = new Formatter()) {
            val sysInfo = SystemUtils.getSystemInfo();
            sysInfo.forEach((key, v) -> {
                if (key.startsWith(SEPARATOR_CHAR)) {
                    formatter.format("%s%n", LINE_SEPARATOR);
                } else {
                    formatter.format("%s: %s%n", key, v);
                }
            });
            formatter.format("%s%n", LINE_SEPARATOR);
            injectEnvironmentInfo(formatter, environment, sourceClass);
            ServiceLoader.load(BannerContributor.class)
                .stream()
                .forEach(clz -> clz.get().contribute(formatter, environment));
            formatter.format("%s%n", LINE_SEPARATOR);
            return formatter.toString();
        }
    }
}
