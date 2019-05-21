package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Table;

/**
 * @author Travis Schmidt
 * @since 5.2
 */
@Embeddable
@Table(name = "RegisteredServiceImplContact")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"name", "email"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultRegisteredServiceContact implements RegisteredServiceContact {

    private static final long serialVersionUID = 1324660891900737066L;

    @Column(nullable = false)
    private String name;

    @Column
    private String email;

    @Column
    private String phone;

    @Column
    private String department;

}
