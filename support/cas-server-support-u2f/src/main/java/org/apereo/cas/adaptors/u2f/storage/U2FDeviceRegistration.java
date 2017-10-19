package org.apereo.cas.adaptors.u2f.storage;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.LocalDate;

/**
 * This is {@link U2FDeviceRegistration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Entity
@Table(name = "U2FDeviceRegistration")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class U2FDeviceRegistration {
    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private String username;

    @Lob
    @Column(name = "record")
    private String record;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createdDate;

    public U2FDeviceRegistration() {
        setId(System.currentTimeMillis());
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(final String record) {
        this.record = record;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }
}
