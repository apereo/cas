/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apereo.cas.feedback;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * {@link EnableConfigurationProperties Configuration properties} for configuring the
 * monitoring of issues that require user feedback.
 *
 * @author Andy Wilkinson
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "issuebot.feedback")
final class FeedbackProperties {

    /**
     * Name of the label that is applied when feedback is required.
     */
    private String requiredLabel;

    /**
     * Name of the label that is applied when feedback has been provided.
     */
    private String providedLabel;

    /**
     * Name of the label that is applied when the feedback reminder comment has been made.
     */
    private String reminderLabel;

    /**
     * The text of the comment that is added as a reminder that feedback is required.
     */
    private String reminderComment;

    /**
     * The text of the comment that is added when an issue is clsed as feedback has not
     * been provided.
     */
    private String closeComment;

}
