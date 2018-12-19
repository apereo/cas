package org.apereo.cas.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(of = {"fullName", "url"}, includeFieldNames = false)
public class Repository {
    private final String id;
    private final String name;
    private final String fullName;
    private final boolean fork;
    private final String url;
    private final String description;
    private final String mergeUrl;

    @JsonCreator
    public Repository(@JsonProperty("id") final String id,
                      @JsonProperty("name") final String name,
                      @JsonProperty("full_name") final String fullName,
                      @JsonProperty("fork") final boolean fork,
                      @JsonProperty("description") final String description,
                      @JsonProperty("merges_url") final String mergeUrl,
                      @JsonProperty("url") final String url) {
        this.id = id;
        this.name = name;
        this.fullName = fullName;
        this.fork = fork;
        this.description = description;
        this.url = url;
        this.mergeUrl = mergeUrl;
    }
}
