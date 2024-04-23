package org.apereo.cas.authentication;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jooq.lambda.Unchecked;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link OneTimeTokenAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@MappedSuperclass
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString(exclude = {"secretKey", "scratchCodes"})
@Getter
@Setter
@EqualsAndHashCode
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class OneTimeTokenAccount implements Serializable, Comparable<OneTimeTokenAccount>, Cloneable {
    /**
     * Table name used to hold otp scratch codes.
     */
    public static final String TABLE_NAME_SCRATCH_CODES = "scratch_codes";

    @Serial
    private static final long serialVersionUID = -8289105320642735252L;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Id
    @Transient
    @JsonProperty("id")
    @Builder.Default
    private long id = System.currentTimeMillis();

    @Column(nullable = false, length = 2048)
    @JsonProperty("secretKey")
    private String secretKey;

    @Column(nullable = false)
    @JsonProperty("validationCode")
    private int validationCode;

    @ElementCollection(targetClass = BigInteger.class)
    @CollectionTable(name = TABLE_NAME_SCRATCH_CODES, joinColumns = @JoinColumn(name = "id"))
    @Column(nullable = false, columnDefinition = "numeric", precision = 255, scale = 0)
    @Builder.Default
    private List<Number> scratchCodes = new ArrayList<>(0);

    @Column(nullable = false)
    @JsonProperty("username")
    private String username;

    @Column(nullable = false)
    @JsonProperty("name")
    private String name;

    @Column
    @JsonProperty("registrationDate")
    @Builder.Default
    private ZonedDateTime registrationDate = ZonedDateTime.now(ZoneOffset.UTC);

    @Column
    @JsonProperty("lastUsedDateTime")
    private String lastUsedDateTime;

    @Override
    public int compareTo(final OneTimeTokenAccount o) {
        return new CompareToBuilder()
            .append(this.scratchCodes.toArray(), o.getScratchCodes().toArray())
            .append(this.validationCode, o.getValidationCode())
            .append(this.secretKey, o.getSecretKey())
            .append(this.username, o.getUsername())
            .append(this.name, o.getName())
            .append(this.lastUsedDateTime, o.getLastUsedDateTime())
            .build()
            .intValue();
    }

    @Override
    public OneTimeTokenAccount clone() {
        return Unchecked.supplier(() -> (OneTimeTokenAccount) super.clone()).get();
    }

    /**
     * Convert this record into JSON.
     *
     * @return the string
     */
    @JsonIgnore
    public String toJson() {
        return FunctionUtils.doUnchecked(() -> MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(this));
    }
}
