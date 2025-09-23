package org.apereo.cas.nativex.features;

import org.graalvm.nativeimage.hosted.Feature;

/**
 * This is {@link BaseCasNativeImageFeature}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public abstract class BaseCasNativeImageFeature implements Feature {
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
