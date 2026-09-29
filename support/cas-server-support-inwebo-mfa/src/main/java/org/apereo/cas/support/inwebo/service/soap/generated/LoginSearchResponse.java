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
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="loginSearchReturn" type="{http://console.inwebo.com}LoginSearchResult"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = StringUtils.EMPTY, propOrder = "loginSearchReturn")
@XmlRootElement(name = "loginSearchResponse")
@Getter
@Setter
public class LoginSearchResponse {

    @XmlElement(required = true)
    protected LoginSearchResult loginSearchReturn;

}
