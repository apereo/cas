package org.apereo.cas.services;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;

/**
 * Contact assigned to a service definition.
 *
 * @author Travis Schmidt
 * @since 5.2
 */
@Embeddable
@Table(name = "RegisteredServiceImplContact")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@EqualsAndHashCode(of = {"name", "email"})
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DefaultRegisteredServiceContact implements RegisteredServiceContact {

    @Serial
    private static final long serialVersionUID = 1324660891900737066L;

    @Column(nullable = false)
    private String name;

    @Column
    private String email;

    @Column
    private String phone;

    @Column
    private String department;

    @Column
    private String type;

}
