package org.apereo.cas.authentication;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import jakarta.annotation.Nonnull;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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

    /**
     * Table name used to hold otp properties.
     */
    public static final String TABLE_NAME_OTP_PROPERTIES = "otp_properties";

    @Serial
    private static final long serialVersionUID = -8289105320642735252L;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Id
    @Transient
    @JsonProperty("id")
    private long id;

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
    private List<Number> scratchCodes = new ArrayList<>();

    @ElementCollection(targetClass = String.class)
    @CollectionTable(name = TABLE_NAME_OTP_PROPERTIES, joinColumns = @JoinColumn(name = "id"))
    @Column(nullable = false, columnDefinition = "VARCHAR(1024)")
    @Builder.Default
    private List<String> properties = new ArrayList<>();

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

    @Column
    @JsonProperty("source")
    private String source;

    @Column
    @JsonProperty("tenant")
    private String tenant;

    @Override
    public int compareTo(@Nonnull final OneTimeTokenAccount tokenAccount) {
        return Comparator
            .comparing((OneTimeTokenAccount account) -> account.getScratchCodes().toArray(),
                Comparator.comparing(Arrays::toString))
            .thenComparingInt(OneTimeTokenAccount::getValidationCode)
            .thenComparing(OneTimeTokenAccount::getSecretKey)
            .thenComparing(OneTimeTokenAccount::getUsername)
            .thenComparing(OneTimeTokenAccount::getName)
            .thenComparing(acct -> Objects.requireNonNullElse(acct.getLastUsedDateTime(), StringUtils.EMPTY))
            .thenComparing(acct -> StringUtils.defaultString(acct.getSource()))
            .thenComparing(acct -> StringUtils.defaultString(acct.getTenant()))
            .compare(this, tokenAccount);
    }

    @Override
    public final OneTimeTokenAccount clone() {
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

    /**
     * Assign id if undefined.
     *
     * @return the registered service
     */
    @CanIgnoreReturnValue
    public OneTimeTokenAccount assignIdIfNecessary() {
        if (getId() <= 0) {
            setId(System.currentTimeMillis());
        }
        return this;
    }
}
