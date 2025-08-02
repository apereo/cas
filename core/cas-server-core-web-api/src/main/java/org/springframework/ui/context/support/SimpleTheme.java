package org.springframework.ui.context.support;

import org.springframework.ui.context.Theme;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;

/**
 * This is {@link SimpleTheme}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
@Getter
public class SimpleTheme implements Theme {

    private final String name;

    private final MessageSource messageSource;
}
