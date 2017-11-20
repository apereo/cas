package org.apereo.cas.util.serialization;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is {@link TicketIdSanitizationUtils} which attempts to remove
 * sensitive ticket ids from a given String.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class TicketIdSanitizationUtils {
    private static final Pattern TICKET_ID_PATTERN = Pattern.compile('(' + TicketGrantingTicket.PREFIX + '|'
            + ProxyGrantingTicket.PROXY_GRANTING_TICKET_IOU_PREFIX + '|' + ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX
            + ")(-)+(\\w)+(-)+(\\w)+");

    /**
     * Specifies the ending tail length of the ticket id that would still be visible in the output
     * for troubleshooting purposes.
     */
    private static final int VISIBLE_TAIL_LENGTH = 10;
    
    private TicketIdSanitizationUtils() {}

    /**
     * Remove ticket id from the message.
     *
     * @param msg the message
     * @return the modified message with tgt id removed
     */
    public static String sanitize(final String msg) {
        String modifiedMessage = msg;
        if (StringUtils.isNotBlank(msg) && !Boolean.getBoolean("CAS_TICKET_ID_SANITIZE_SKIP")) {
            final Matcher matcher = TICKET_ID_PATTERN.matcher(msg);
            while (matcher.find()) {
                final String match = matcher.group();
                final String newId = matcher.group(1) + '-'
                        + StringUtils.repeat("*", match.length() - VISIBLE_TAIL_LENGTH)
                        + StringUtils.right(match, VISIBLE_TAIL_LENGTH);
                modifiedMessage = modifiedMessage.replaceAll(match, newId);
            }
        }
        return modifiedMessage;
    }
}
