package org.apereo.cas.nativex.features;

import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.graalvm.nativeimage.hosted.Feature;

/**
 * This is {@link BaseCasNativeImageFeature}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public abstract class BaseCasNativeImageFeature implements Feature {
    /**
     * Gets option.
     *
     * @param name         the name
     * @param defaultValue the default value
     * @return the option
     */
    protected static boolean getBooleanOption(final String name, final boolean defaultValue) {
        val prop = System.getProperty(name);
        return BooleanUtils.toBoolean(StringUtils.defaultIfBlank(prop, Boolean.toString(defaultValue)));
    }

    /**
     * Log messages via System output stream.
     * 
     * @param message the message
     */
    protected static void log(final String message) {
        //CHECKSTYLE:OFF
        System.out.println(message);
        //CHECKSTYLE:ON
    }
}
