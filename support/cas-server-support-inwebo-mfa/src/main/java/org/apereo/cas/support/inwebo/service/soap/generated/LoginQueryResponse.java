package org.apereo.cas.support.inwebo.service.soap.generated;

import module java.base;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * The generated SOAP class.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = StringUtils.EMPTY, propOrder = "loginQueryReturn")
@XmlRootElement(name = "loginQueryResponse")
@Getter
@Setter
public class LoginQueryResponse {

    @XmlElement(required = true)
    protected LoginQueryResult loginQueryReturn;

}
