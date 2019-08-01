package org.apereo.cas.adaptors.u2f.storage;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * This is {@link U2FDeviceRegistration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Entity
@Table(name = "U2FDeviceRegistration")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@AllArgsConstructor
public class U2FDeviceRegistration implements Serializable {
    private static final long serialVersionUID = 7258490070277856614L;

    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;

    @Column(nullable = false)
    private String username;

    @Lob
    @Column(name = "record", length = 4000)
    private String record;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createdDate;

    public U2FDeviceRegistration() {
        setId(System.nanoTime());
    }
}
