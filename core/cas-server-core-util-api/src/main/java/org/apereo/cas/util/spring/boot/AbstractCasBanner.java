package org.apereo.cas.util.spring.boot;

import module java.base;
import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.SystemUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.core.env.Environment;

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
    public void printBanner(final @NonNull Environment environment, final Class<?> sourceClass, final @NonNull PrintStream out) {
        AsciiArtUtils.printAsciiArt(out, getTitle(), collectEnvironmentInfo(environment, sourceClass));
    }

    @Override
    public String getTitle() {
        return """
              __   ____  ____  ____  ____  __      ___   __   ____\s
             / _\\ (  _ \\(  __)(  _ \\(  __)/  \\    / __) / _\\ / ___)
            /    \\ ) __/ ) _)  )   / ) _)(  O )  ( (__ /    \\\\___ \\
            \\_/\\_/(__)  (____)(__\\_)(____)\\__/    \\___)\\_/\\_/(____/
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
