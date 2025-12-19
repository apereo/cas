package org.apereo.cas.configuration.model.support.aws;

import module java.base;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link GoogleCloudLogsProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-gcp-logging")
@Accessors(chain = true)
public class GoogleCloudLogsProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 1516821862609444134L;

    /**
     * In Google Cloud Logging, the log name is an identifier that specifies the
     * particular log to which log entries are written or from which they are retrieved.
     * It is an essential component when interacting with logs using the Google Cloud Logging API,
     * as it allows you to target specific logs for querying or analysis.
     * <p>
     * It typically has the following syntax: {@code projects/[PROJECT_ID]/logs/[LOG_ID]}
     */
    @RequiredProperty
    private String logName;

    /**
     * A Project ID is a unique identifier assigned to a specific project within your GCP environment.
     * The Project ID is globally unique across all GCP projects, meaning no two projects can have the same Project ID.
     * The Project ID is used in various API calls, configurations, and URLs to uniquely identify your CAS project.
     */
    @RequiredProperty
    private String projectId;

    /**
     * Map of resource labels to filter log entries.
     * Key is the resource label name, and value is the actual label itself.
     */
    private Map<String, String> labels = new LinkedHashMap<>();
}
