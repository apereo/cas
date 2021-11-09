package org.apereo.cas.logging;

import org.apereo.cas.util.logging.LoggingInitialization;

import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.lookup.MainMapLookup;

import java.util.Arrays;

/**
 * This is {@link Log4jInitialization}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@NoArgsConstructor
public class Log4jInitialization implements LoggingInitialization {
    @Override
    public void setMainArguments(final String[] mainArguments) {
        val args = Arrays.stream(mainArguments)
            .filter(arg -> arg.startsWith("--logging.level"))
            .map(arg -> StringUtils.replace(arg, "=", " "))
            .toArray(String[]::new);
        MainMapLookup.setMainArguments(args);
    }
}
