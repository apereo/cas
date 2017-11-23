package org.apereo.cas.trusted.authentication.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

/**
 * This is {@link MultifactorAuthenticationTrustRecord}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Entity
@Table(name = "MultifactorAuthenticationTrustRecord")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultifactorAuthenticationTrustRecord implements Comparable<MultifactorAuthenticationTrustRecord> {
    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private String principal;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private String geography;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate recordDate;

    @Column(length = 500, updatable = true, insertable = true, nullable = false)
    private String recordKey;

    @Column(length = 500, updatable = true, insertable = true, nullable = false)
    private String name;

    public MultifactorAuthenticationTrustRecord() {
        this.id = System.currentTimeMillis();
    }

    public String getRecordKey() {
        return recordKey;
    }

    public void setRecordKey(final String id) {
        this.recordKey = id;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(final String principal) {
        this.principal = principal;
    }

    public String getGeography() {
        return geography;
    }

    public void setGeography(final String geography) {
        this.geography = geography;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(final LocalDate date) {
        this.recordDate = date;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final MultifactorAuthenticationTrustRecord rhs = (MultifactorAuthenticationTrustRecord) obj;
        return new EqualsBuilder()
                .append(this.principal, rhs.principal)
                .append(this.geography, rhs.geography)
                .append(this.recordDate, rhs.recordDate)
                .append(this.recordKey, rhs.recordKey)
                .append(this.name, rhs.name)
                .isEquals();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(principal)
                .append(geography)
                .append(recordDate)
                .append(recordKey)
                .append(name)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("principal", principal)
                .append("geography", geography)
                .append("recordDate", recordDate)
                .append("name", name)
                .toString();
    }

    /**
     * New instance of authentication trust record.
     *
     * @param principal the principal
     * @param geography the geography
     * @return the authentication trust record
     */
    public static MultifactorAuthenticationTrustRecord newInstance(final String principal, final String geography) {
        final MultifactorAuthenticationTrustRecord r = new MultifactorAuthenticationTrustRecord();
        r.setRecordDate(LocalDate.now());
        r.setPrincipal(principal);
        r.setGeography(geography);
        r.setName(principal.concat("-").concat(LocalDate.now().toString()).concat("-").concat(geography));
        return r;
    }

    @Override
    public int compareTo(final MultifactorAuthenticationTrustRecord o) {
        return this.recordDate.compareTo(o.getRecordDate());
    }
}
