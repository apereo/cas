package org.apereo.cas.util.text;

import module java.base;
import org.apereo.cas.util.InetAddressUtils;
import org.apereo.cas.util.RegexUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link DefaultMessageSanitizer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class DefaultMessageSanitizer implements MessageSanitizer {
    private static final String PATTERN1 = "(psw|pwd|clientSecret|password|token|credential|secret|secretKey)\\s*=\\s*(['\"]*\\S+\\b['\"]*)";
    private static final String PATTERN2 = "('(password|clientSecret|token|secret|credential|secretKey)'\\s*->\\s*array<String>\\['([^']+)'\\])";
    
    private static final Pattern SENSITIVE_TEXT_PATTERN = RegexUtils.createPattern(PATTERN1 + '|' + PATTERN2);

    private static final Boolean CAS_TICKET_ID_SANITIZE_SKIP = Boolean.getBoolean("CAS_TICKET_ID_SANITIZE_SKIP");

    /**
     * Specifies the ending tail length of the ticket id that would still be visible in the output
     * for troubleshooting purposes.
     */
    private static final int VISIBLE_TAIL_LENGTH = 7;

    private static final int OBFUSCATION_LENGTH = 16;

    /**
     * The obfuscated text that would be the replacement for sensitive text.
     */
    public static final String OBFUSCATED_STRING = "*".repeat(OBFUSCATION_LENGTH);

    /**
     * Gets the default suffix used when the default ticket id generator is used so the proper
     * visible length is shown.
     */
    private static final int HOST_NAME_LENGTH = InetAddressUtils.getCasServerHostName().length();

    private final Pattern ticketIdPattern;

    @Override
    public String sanitize(final String msg) {
        var modifiedMessage = msg;
        if (StringUtils.isNotBlank(msg) && !CAS_TICKET_ID_SANITIZE_SKIP) {
            val matcher = ticketIdPattern.matcher(msg);
            while (matcher.find()) {
                val match = matcher.group();
                val group = matcher.group(1);
                if (StringUtils.isNotBlank(group)) {
                    val length = group.length();
                    var replaceLength = length - VISIBLE_TAIL_LENGTH - (HOST_NAME_LENGTH + 1);
                    if (replaceLength <= 0) {
                        replaceLength = length;
                    }
                    val newId = match.replace(group.substring(0, replaceLength), OBFUSCATED_STRING);
                    modifiedMessage = modifiedMessage.replace(match, newId);
                }
            }
        }

        val matcher = SENSITIVE_TEXT_PATTERN.matcher(msg);
        while (matcher.find()) {
            var group = matcher.group(2);
            if (StringUtils.isNotBlank(group)) {
                modifiedMessage = modifiedMessage.replace(group, OBFUSCATED_STRING);
            }
            group = matcher.group(5);
            if (StringUtils.isNotBlank(group)) {
                modifiedMessage = modifiedMessage.replace(group, OBFUSCATED_STRING);
            }
        }
        return modifiedMessage;
    }
}
