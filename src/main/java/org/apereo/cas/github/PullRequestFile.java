package org.apereo.cas.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(of = {"sha", "filename", "status"}, includeFieldNames = false)
public class PullRequestFile {
    private final String sha;
    private final String filename;
    private final String status;

    @JsonCreator
    public PullRequestFile(@JsonProperty("sha") final String sha,
                           @JsonProperty("filename") final String filename,
                           @JsonProperty("status") final String status) {
        this.sha = sha;
        this.filename = filename;
        this.status = status;
    }
}
