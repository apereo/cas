package org.jasig.cas.logging;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is {@link CasAppender}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Plugin(name="CasAppender", category="Core", elementType="appender", printObject=true)
public class CasAppender extends AbstractAppender {
    private static final long serialVersionUID = 3744758323628847477L;

    private static final Pattern TICKET_ID_PATTERN = Pattern.compile('(' + TicketGrantingTicket.PREFIX + '|'
            + ProxyGrantingTicket.PROXY_GRANTING_TICKET_IOU_PREFIX + '|' + ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX
            + ")(-)*(\\w)*(-)*(\\w)*");

    /**
     * Specifies the ending tail length of the ticket id that would still be visible in the output
     * for troubleshooting purposes.
     */
    private static final int VISIBLE_ID_TAIL_LENGTH = 10;

    private final Configuration config;
    private final AppenderRef appenderRef;


    /**
     * Instantiates a new Cas appender.
     *
     * @param name        the name
     * @param config      the config
     * @param appenderRef the appender ref
     */
    public CasAppender(final String name, final Configuration config, final AppenderRef appenderRef) {
        super(name, null, PatternLayout.createDefaultLayout());
        this.config = config;
        this.appenderRef = appenderRef;
    }
    

    @Override
    public void append(final LogEvent logEvent) {

        final String messageModified = manipulateLogMessage(logEvent.getMessage().getFormattedMessage());
        final Message message = new SimpleMessage(messageModified);
        final LogEvent newLogEvent = Log4jLogEvent.newBuilder()
                .setLevel(logEvent.getLevel())
                .setLoggerName(logEvent.getLoggerName())
                .setLoggerFqcn(logEvent.getLoggerFqcn())
                .setContextMap(logEvent.getContextMap())
                .setContextStack(logEvent.getContextStack())
                .setEndOfBatch(logEvent.isEndOfBatch())
                .setIncludeLocation(logEvent.isIncludeLocation())
                .setMarker(logEvent.getMarker())
                .setMessage(message)
                .setNanoTime(logEvent.getNanoTime())
                .setSource(logEvent.getSource())
                .setThreadName(logEvent.getThreadName())
                .setThrownProxy(logEvent.getThrownProxy())
                .setThrown(logEvent.getThrown())
                .setTimeMillis(logEvent.getTimeMillis()).build();
        
        final Appender appender = this.config.getAppender(this.appenderRef.getRef());
        appender.append(newLogEvent);
    }

    /**
     * Create appende cas appender.
     *
     * @param name        the name
     * @param appenderRef the appender ref
     * @param config      the config
     * @return the cas appender
     */
    @PluginFactory
    public static CasAppender build(@PluginAttribute("name") final String name, 
                                    @PluginElement("AppenderRef") AppenderRef appenderRef,
                                    @PluginConfiguration Configuration config) {
        return new CasAppender(name, config, appenderRef);
    }

    /**
     * Manipulate the log message. For now, removes ticket ids from the log.
     *
     * @param msg log message
     * @return message to log
     */
    private String manipulateLogMessage(final String msg) {
        return removeTicketId(msg);
    }

    /**
     * Remove ticket id from the log message.
     *
     * @param msg the message
     * @return the modified message with tgt id removed
     */
    private String removeTicketId(final String msg) {
        String modifiedMessage = msg;

        if (StringUtils.isNotBlank(msg)) {
            final Matcher matcher = TICKET_ID_PATTERN.matcher(msg);
            while (matcher.find()) {
                final String match = matcher.group();
                final String newId = matcher.group(1) + '-'
                        + StringUtils.repeat("*", match.length() - VISIBLE_ID_TAIL_LENGTH)
                        + StringUtils.right(match, VISIBLE_ID_TAIL_LENGTH);

                modifiedMessage = modifiedMessage.replaceAll(match, newId);
            }
        }
        return modifiedMessage;
    }


}
