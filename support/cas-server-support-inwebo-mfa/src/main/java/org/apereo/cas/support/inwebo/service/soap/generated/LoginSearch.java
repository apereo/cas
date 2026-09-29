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
 *         &lt;element name="userid" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="serviceid" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="loginname" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="exactmatch" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="offset" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="nmax" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="sort" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
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
@XmlType(name = StringUtils.EMPTY, propOrder = {
    "userid",
    "serviceid",
    "loginname",
    "exactmatch",
    "offset",
    "nmax",
    "sort"
})
@XmlRootElement(name = "loginSearch")
@Getter
@Setter
public class LoginSearch {

    protected long userid;

    protected long serviceid;

    @XmlElement(required = true)
    protected String loginname;

    protected long exactmatch;

    protected long offset;

    protected long nmax;

    protected long sort;

}
