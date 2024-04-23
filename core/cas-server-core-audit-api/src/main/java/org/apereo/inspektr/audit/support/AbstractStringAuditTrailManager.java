package org.apereo.inspektr.audit.support;

import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract AuditTrailManager that turns the AuditActionContext into a printable String.
 *
 * @author Scott Battaglia
 * @since 1.0.1
 */
@Setter
@Getter
public abstract class AbstractStringAuditTrailManager implements AuditTrailManager {
    /**
     * what format should the audit log entry use.
     */
    private AuditFormats auditFormat = AuditFormats.DEFAULT;

    /**
     * Use multi-line output by default.
     **/
    private boolean useSingleLine;

    /**
     * Separator for single line log entries.
     */
    private String entrySeparator = ",";

    private List<AuditableFields> auditableFields = new ArrayList<>();

    protected String toString(final AuditActionContext auditActionContext) {
        if (auditFormat == AuditFormats.JSON) {
            val builder = new StringBuilder();
            try {
                var writer = useSingleLine
                    ? MAPPER.writer(new MinimalPrettyPrinter())
                    : MAPPER.writerWithDefaultPrettyPrinter();
                builder.append(writer.writeValueAsString(getMappedAuditActionContext(auditActionContext)));
                
                if (!useSingleLine) {
                    builder.append('\n');
                }
            } catch (final Exception e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            return builder.toString();
        }
        return getAuditLineString(auditActionContext);
    }

    protected String getAuditLineString(final AuditActionContext auditActionContext) {
        val builder = new StringBuilder();

        if (!useSingleLine) {
            builder.append("Audit trail record BEGIN\n");
            builder.append("=============================================================\n");
        }

        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.WHEN)) {
            if (useSingleLine) {
                builder.append(auditActionContext.getWhenActionWasPerformed());
                builder.append(getEntrySeparator());
            } else {
                builder.append("WHEN: ");
                builder.append(auditActionContext.getWhenActionWasPerformed());
                builder.append('\n');
            }
        }

        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.WHO)) {
            if (useSingleLine) {
                builder.append(auditActionContext.getPrincipal());
                builder.append(getEntrySeparator());
            } else {
                builder.append("WHO: ");
                builder.append(auditActionContext.getPrincipal());
                builder.append('\n');
            }
        }

        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.WHAT)) {
            if (useSingleLine) {
                builder.append(auditActionContext.getResourceOperatedUpon());
                builder.append(getEntrySeparator());
            } else {
                builder.append("WHAT: ");
                builder.append(auditActionContext.getResourceOperatedUpon());
                builder.append('\n');
            }
        }

        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.ACTION)) {
            if (useSingleLine) {
                builder.append(auditActionContext.getActionPerformed());
                builder.append(getEntrySeparator());
            } else {
                builder.append("ACTION: ");
                builder.append(auditActionContext.getActionPerformed());
                builder.append('\n');
            }
        }

        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.APPLICATION)) {
            if (useSingleLine) {
                builder.append(auditActionContext.getApplicationCode());
                builder.append(getEntrySeparator());
            } else {
                builder.append("APPLICATION: ");
                builder.append(auditActionContext.getApplicationCode());
                builder.append('\n');
            }
        }

        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.USER_AGENT)) {
            if (useSingleLine) {
                builder.append(auditActionContext.getClientInfo().getUserAgent());
                builder.append(getEntrySeparator());
            } else {
                builder.append("USER-AGENT: ");
                builder.append(auditActionContext.getClientInfo().getUserAgent());
                builder.append('\n');
            }
        }

        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.CLIENT_IP)) {
            if (useSingleLine) {
                builder.append(auditActionContext.getClientInfo().getClientIpAddress());
                builder.append(getEntrySeparator());
            } else {
                builder.append("CLIENT IP ADDRESS: ");
                builder.append(auditActionContext.getClientInfo().getClientIpAddress());
                builder.append('\n');
            }
        }

        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.SERVER_IP)) {
            if (useSingleLine) {
                builder.append(auditActionContext.getClientInfo().getServerIpAddress());
                builder.append(getEntrySeparator());
            } else {
                builder.append("SERVER IP ADDRESS: ");
                builder.append(auditActionContext.getClientInfo().getServerIpAddress());
                builder.append('\n');
            }
        }

        if (!useSingleLine) {
            builder.append("=============================================================");
            builder.append("\n\n");
        }

        return builder.toString();
    }

    private static Object readFieldValue(final String value) {
        try {
            return MAPPER.readValue(value, Map.class);
        } catch (final Exception e) {
            return value;
        }
    }

    protected Map<String, ?> getMappedAuditActionContext(final AuditActionContext auditActionContext) {
        var map = new LinkedHashMap<String, Object>();
        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.WHO)) {
            map.put("who", readFieldValue(auditActionContext.getPrincipal()));
        }
        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.WHAT)) {
            map.put("what", readFieldValue(auditActionContext.getResourceOperatedUpon()));
        }

        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.ACTION)) {
            map.put("action", readFieldValue(auditActionContext.getActionPerformed()));
        }
        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.APPLICATION)) {
            map.put("application", readFieldValue(auditActionContext.getApplicationCode()));
        }
        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.WHEN)) {
            map.put("when", readFieldValue(auditActionContext.getWhenActionWasPerformed().toString()));
        }
        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.CLIENT_IP)) {
            map.put("clientIpAddress", readFieldValue(auditActionContext.getClientInfo().getClientIpAddress()));
        }
        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.SERVER_IP)) {
            map.put("serverIpAddress", readFieldValue(auditActionContext.getClientInfo().getServerIpAddress()));
        }
        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.USER_AGENT)) {
            map.put("userAgent", readFieldValue(auditActionContext.getClientInfo().getUserAgent()));
        }
        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.HEADERS)) {
            map.put("headers", auditActionContext.getClientInfo().getHeaders());
        }
        if (auditableFields.isEmpty() || auditableFields.contains(AuditableFields.GEO_LOCATION)) {
            map.put("geoLocation", readFieldValue(auditActionContext.getClientInfo().getGeoLocation()));
        }
        return map;
    }
}
