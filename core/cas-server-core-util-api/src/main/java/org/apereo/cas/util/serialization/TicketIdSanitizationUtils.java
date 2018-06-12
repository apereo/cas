package org.apereo.cas.util.serialization;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.util.InetAddressUtils;

import java.util.regex.Pattern;

/**
 * This is {@link TicketIdSanitizationUtils} which attempts to remove
 * sensitive ticket ids from a given String.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@UtilityClass
public class TicketIdSanitizationUtils {
    private static final Pattern TICKET_ID_PATTERN = Pattern.compile("(?:(?:" + TicketGrantingTicket.PREFIX + '|'
        + ProxyGrantingTicket.PROXY_GRANTING_TICKET_IOU_PREFIX + '|' + ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX
        + ")-\\d+-)([\\w.-]+)");

    /**
     * Specifies the ending tail length of the ticket id that would still be visible in the output
     * for troubleshooting purposes.
     */
    private static final int VISIBLE_TAIL_LENGTH = 10;

    /**
     * Gets the default suffix used when the default ticket id generator is used so the poroper
     * visible length is shown.
     */
    private static final int HOST_NAME_LENGTH = InetAddressUtils.getCasServerHostName().length();


    /**
     * Remove ticket id from the message.
     *
     * @param msg the message
     * @return the modified message with tgt id removed
     */
    public static String sanitize(final String msg) {
        var modifiedMessage = msg;
        if (StringUtils.isNotBlank(msg) && !Boolean.getBoolean("CAS_TICKET_ID_SANITIZE_SKIP")) {
            final var matcher = TICKET_ID_PATTERN.matcher(msg);
            while (matcher.find()) {
                final var match = matcher.group();
                final var group = matcher.group(1);
                final var length = group.length();
                var replaceLength = length - VISIBLE_TAIL_LENGTH - (HOST_NAME_LENGTH + 1);
                if (replaceLength <= 0) {
                    replaceLength = length;
                }
                final var newId = match.replace(group.substring(0, replaceLength), StringUtils.repeat("*", replaceLength));
                modifiedMessage = modifiedMessage.replaceAll(match, newId);
            }
        }
        return modifiedMessage;
    }
}
