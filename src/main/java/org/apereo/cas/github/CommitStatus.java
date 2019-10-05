package org.apereo.cas.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * This is {@link CommitStatus}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
public class CommitStatus {
    private final String state;
    private final String url;
    private final String id;
    private final String context;
    private final String description;
    private final String targetUrl;

    @JsonCreator
    public CommitStatus(@JsonProperty("state") final String state,
                        @JsonProperty("url") final String url,
                        @JsonProperty("id") final String id,
                        @JsonProperty("context") final String context,
                        @JsonProperty("description") final String description,
                        @JsonProperty("target_url") final String targetUrl) {
        this.state = state;
        this.url = url;
        this.id = id;
        this.description = description;
        this.context = context;
        this.targetUrl = targetUrl;
    }

}
