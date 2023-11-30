package org.apereo.cas.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.ZonedDateTime;

@Getter
@Setter
@ToString(of = {"id", "event", "actor"}, includeFieldNames = false)
public class TimelineEntry {
    private long id;
    private User actor;
    private User committer;
    private User author;
    private String event;
    private Label label;
    private String sha;
    private Commit tree;

    @JsonProperty("created_at")
    private ZonedDateTime createdAt;

    public boolean isLabeled() {
        return "labeled".equalsIgnoreCase(event);
    }

    public boolean isRenamed() {
        return "renamed".equalsIgnoreCase(event);
    }

    public boolean isCommented() {
        return "commented".equalsIgnoreCase(event);
    }
}
