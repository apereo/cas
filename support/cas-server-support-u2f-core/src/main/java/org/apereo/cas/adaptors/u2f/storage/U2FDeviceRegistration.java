package org.apereo.cas.adaptors.u2f.storage;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * This is {@link U2FDeviceRegistration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@MappedSuperclass
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@AllArgsConstructor
public class U2FDeviceRegistration implements Serializable {
    private static final long serialVersionUID = 7258490070277856614L;

    @Id
    @Transient
    private long id = -1;

    @Column(nullable = false)
    private String username;

    @Lob
    @Column(name = "record", length = Integer.MAX_VALUE)
    private String record;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createdDate;

    public U2FDeviceRegistration() {
        setId(System.nanoTime());
    }
}
