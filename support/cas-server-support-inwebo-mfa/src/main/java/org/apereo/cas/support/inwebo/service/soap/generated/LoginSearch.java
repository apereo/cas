// CHECKSTYLE:OFF
package org.apereo.cas.support.inwebo.service.soap.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
@XmlType(name = "", propOrder = {
    "userid",
    "serviceid",
    "loginname",
    "exactmatch",
    "offset",
    "nmax",
    "sort"
})
@XmlRootElement(name = "loginSearch")
public class LoginSearch {

    protected long userid;
    protected long serviceid;
    @XmlElement(required = true)
    protected String loginname;
    protected long exactmatch;
    protected long offset;
    protected long nmax;
    protected long sort;

    /**
     * Obtient la valeur de la propriété userid.
     *
     * @return the user id
     */
    public long getUserid() {
        return userid;
    }

    /**
     * Définit la valeur de la propriété userid.
     *
     * @param value the user id
     */
    public void setUserid(long value) {
        this.userid = value;
    }

    /**
     * Obtient la valeur de la propriété serviceid.
     *
     * @return the service id
     */
    public long getServiceid() {
        return serviceid;
    }

    /**
     * Définit la valeur de la propriété serviceid.
     *
     * @param value the service id
     */
    public void setServiceid(long value) {
        this.serviceid = value;
    }

    /**
     * Obtient la valeur de la propriété loginname.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLoginname() {
        return loginname;
    }

    /**
     * Définit la valeur de la propriété loginname.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLoginname(String value) {
        this.loginname = value;
    }

    /**
     * Obtient la valeur de la propriété exactmatch.
     *
     * @return whether we want an exact match
     */
    public long getExactmatch() {
        return exactmatch;
    }

    /**
     * Définit la valeur de la propriété exactmatch.
     *
     * @param value the exact match
     */
    public void setExactmatch(long value) {
        this.exactmatch = value;
    }

    /**
     * Obtient la valeur de la propriété offset.
     *
     * @return the offset
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Définit la valeur de la propriété offset.
     *
     * @param value the offset
     */
    public void setOffset(long value) {
        this.offset = value;
    }

    /**
     * Obtient la valeur de la propriété nmax.
     *
     * @return the number max of results
     */
    public long getNmax() {
        return nmax;
    }

    /**
     * Définit la valeur de la propriété nmax.
     *
     * @param value the max
     */
    public void setNmax(long value) {
        this.nmax = value;
    }

    /**
     * Obtient la valeur de la propriété sort.
     *
     * @return the sort
     */
    public long getSort() {
        return sort;
    }

    /**
     * Définit la valeur de la propriété sort.
     *
     * @param value the sort
     */
    public void setSort(long value) {
        this.sort = value;
    }

}
