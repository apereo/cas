package org.apereo.inspektr.audit.support;

import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import tools.jackson.core.util.MinimalPrettyPrinter;
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
                    ? MAPPER.writer().with(new MinimalPrettyPrinter())
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

    protected void buildSingleAuditLineForField(final AuditableFields field, final Object value, final StringBuilder builder) {
        if (isFieldAuditable(field)) {
            if (useSingleLine) {
                builder.append(value);
                builder.append(getEntrySeparator());
            } else {
                builder.append("%s: ".formatted(field.name()));
                builder.append(value);
                builder.append('\n');
            }
        }
    }
    
    protected String getAuditLineString(final AuditActionContext auditActionContext) {
        val builder = new StringBuilder();

        if (!useSingleLine) {
            builder.append("Audit trail record BEGIN\n");
            builder.append("=============================================================\n");
        }

        val clientInfo = auditActionContext.getClientInfo();
        buildSingleAuditLineForField(AuditableFields.WHEN, auditActionContext.getWhenActionWasPerformed(), builder);
        buildSingleAuditLineForField(AuditableFields.WHO, auditActionContext.getPrincipal(), builder);
        buildSingleAuditLineForField(AuditableFields.WHAT, auditActionContext.getResourceOperatedUpon(), builder);
        buildSingleAuditLineForField(AuditableFields.ACTION, auditActionContext.getActionPerformed(), builder);
        buildSingleAuditLineForField(AuditableFields.APPLICATION, auditActionContext.getApplicationCode(), builder);
        buildSingleAuditLineForField(AuditableFields.USER_AGENT, clientInfo.getUserAgent(), builder);
        buildSingleAuditLineForField(AuditableFields.CLIENT_IP, clientInfo.getClientIpAddress(), builder);
        buildSingleAuditLineForField(AuditableFields.SERVER_IP, clientInfo.getServerIpAddress(), builder);
        buildSingleAuditLineForField(AuditableFields.DEVICE_FINGERPRINT, clientInfo.getDeviceFingerprint(), builder);
        buildSingleAuditLineForField(AuditableFields.HEADERS, clientInfo.getHeaders(), builder);
        buildSingleAuditLineForField(AuditableFields.TENANT, clientInfo.getTenant(), builder);

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
        if (isFieldAuditable(AuditableFields.WHO)) {
            map.put("who", readFieldValue(auditActionContext.getPrincipal()));
        }
        if (isFieldAuditable(AuditableFields.WHAT)) {
            map.put("what", readFieldValue(auditActionContext.getResourceOperatedUpon()));
        }
        if (isFieldAuditable(AuditableFields.ACTION)) {
            map.put("action", readFieldValue(auditActionContext.getActionPerformed()));
        }
        if (isFieldAuditable(AuditableFields.APPLICATION)) {
            map.put("application", readFieldValue(auditActionContext.getApplicationCode()));
        }
        if (isFieldAuditable(AuditableFields.WHEN)) {
            map.put("when", readFieldValue(auditActionContext.getWhenActionWasPerformed().toString()));
        }
        
        val clientInfo = auditActionContext.getClientInfo();
        if (isFieldAuditable(AuditableFields.CLIENT_IP)) {
            map.put("clientIpAddress", readFieldValue(clientInfo.getClientIpAddress()));
        }
        if (isFieldAuditable(AuditableFields.SERVER_IP)) {
            map.put("serverIpAddress", readFieldValue(clientInfo.getServerIpAddress()));
        }
        if (isFieldAuditable(AuditableFields.USER_AGENT)) {
            map.put("userAgent", readFieldValue(clientInfo.getUserAgent()));
        }
        if (isFieldAuditable(AuditableFields.HEADERS)) {
            map.put("headers", clientInfo.getHeaders());
        }
        if (isFieldAuditable(AuditableFields.GEO_LOCATION)) {
            map.put("geoLocation", readFieldValue(clientInfo.getGeoLocation()));
        }
        if (isFieldAuditable(AuditableFields.DEVICE_FINGERPRINT)) {
            map.put("deviceFingerprint", readFieldValue(clientInfo.getDeviceFingerprint()));
        }
        if (isFieldAuditable(AuditableFields.TENANT)) {
            map.put("tenant", readFieldValue(clientInfo.getTenant()));
        }
        return map;
    }

    private boolean isFieldAuditable(final AuditableFields field) {
        return auditableFields.isEmpty() || auditableFields.contains(field);
    }
}
