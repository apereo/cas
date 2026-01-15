package org.apereo.cas.web.support;
import module java.base;

/**
 * This is {@link ThrottledSubmissionReceiver}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface ThrottledSubmissionReceiver<T extends ThrottledSubmission> {

    /**
     * Receive.
     *
     * @param submission the submission
     * @throws Exception the exception
     */
    void receive(T submission) throws Exception;
}
