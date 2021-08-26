// CHECKSTYLE:OFF
package org.apereo.cas.support.inwebo.service.soap.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * The generated SOAP class.
 * 
 * <pre>
 * &lt;complexType name="LoginSearchResult"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="err" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="n" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded"/&gt;
 *         &lt;element name="login" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *         &lt;element name="code" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded"/&gt;
 *         &lt;element name="role" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded"/&gt;
 *         &lt;element name="firstname" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *         &lt;element name="mail" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *         &lt;element name="phone" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *         &lt;element name="extrafields" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *         &lt;element name="createdby" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded"/&gt;
 *         &lt;element name="activation_status" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded"/&gt;
 *         &lt;element name="count" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
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
@XmlType(name = "LoginSearchResult", propOrder = {
    "err",
    "n",
    "id",
    "login",
    "code",
    "status",
    "role",
    "firstname",
    "name",
    "mail",
    "phone",
    "extrafields",
    "createdby",
    "activationStatus",
    "count"
})
public class LoginSearchResult {

    @XmlElement(required = true, nillable = true)
    protected String err;
    protected int n;
    @XmlElement(required = true, nillable = true)
    protected List<Long> id;
    @XmlElement(required = true, nillable = true)
    protected List<String> login;
    @XmlElement(required = true, nillable = true)
    protected List<String> code;
    @XmlElement(required = true, nillable = true)
    protected List<Long> status;
    @XmlElement(required = true, nillable = true)
    protected List<Long> role;
    @XmlElement(required = true, nillable = true)
    protected List<String> firstname;
    @XmlElement(required = true, nillable = true)
    protected List<String> name;
    @XmlElement(required = true, nillable = true)
    protected List<String> mail;
    @XmlElement(required = true, nillable = true)
    protected List<String> phone;
    @XmlElement(required = true, nillable = true)
    protected List<String> extrafields;
    @XmlElement(required = true, nillable = true)
    protected List<Long> createdby;
    @XmlElement(name = "activation_status", required = true, nillable = true)
    protected List<Long> activationStatus;
    protected long count;

    /**
     * Obtient la valeur de la propriété err.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErr() {
        return err;
    }

    /**
     * Définit la valeur de la propriété err.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErr(String value) {
        this.err = value;
    }

    /**
     * Obtient la valeur de la propriété n.
     *
     * @return the n property
     */
    public int getN() {
        return n;
    }

    /**
     * Définit la valeur de la propriété n.
     *
     * @param value the n property
     */
    public void setN(int value) {
        this.n = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the id property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * @return the ids
     */
    public List<Long> getId() {
        if (id == null) {
            id = new ArrayList<Long>();
        }
        return this.id;
    }

    /**
     * Gets the value of the login property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the login property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLogin().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * @return the logins
     */
    public List<String> getLogin() {
        if (login == null) {
            login = new ArrayList<String>();
        }
        return this.login;
    }

    /**
     * Gets the value of the code property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the code property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * @return the codes
     */
    public List<String> getCode() {
        if (code == null) {
            code = new ArrayList<String>();
        }
        return this.code;
    }

    /**
     * Gets the value of the status property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the status property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStatus().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * @return the statuses
     */
    public List<Long> getStatus() {
        if (status == null) {
            status = new ArrayList<Long>();
        }
        return this.status;
    }

    /**
     * Gets the value of the role property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the role property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRole().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * @return the roles
     */
    public List<Long> getRole() {
        if (role == null) {
            role = new ArrayList<Long>();
        }
        return this.role;
    }

    /**
     * Gets the value of the firstname property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the firstname property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFirstname().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * @return the first names
     */
    public List<String> getFirstname() {
        if (firstname == null) {
            firstname = new ArrayList<String>();
        }
        return this.firstname;
    }

    /**
     * Gets the value of the name property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the name property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * @return the names
     */
    public List<String> getName() {
        if (name == null) {
            name = new ArrayList<String>();
        }
        return this.name;
    }

    /**
     * Gets the value of the mail property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mail property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * @return the emails
     */
    public List<String> getMail() {
        if (mail == null) {
            mail = new ArrayList<String>();
        }
        return this.mail;
    }

    /**
     * Gets the value of the phone property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the phone property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPhone().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * @return the phones
     */
    public List<String> getPhone() {
        if (phone == null) {
            phone = new ArrayList<String>();
        }
        return this.phone;
    }

    /**
     * Gets the value of the extrafields property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extrafields property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtrafields().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * @return the extra fields
     */
    public List<String> getExtrafields() {
        if (extrafields == null) {
            extrafields = new ArrayList<String>();
        }
        return this.extrafields;
    }

    /**
     * Gets the value of the createdby property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the createdby property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCreatedby().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * @return the created by properties
     */
    public List<Long> getCreatedby() {
        if (createdby == null) {
            createdby = new ArrayList<Long>();
        }
        return this.createdby;
    }

    /**
     * Gets the value of the activationStatus property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the activationStatus property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActivationStatus().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * @return the activation statuses
     */
    public List<Long> getActivationStatus() {
        if (activationStatus == null) {
            activationStatus = new ArrayList<Long>();
        }
        return this.activationStatus;
    }

    /**
     * Obtient la valeur de la propriété count.
     *
     * @return the count
     */
    public long getCount() {
        return count;
    }

    /**
     * Définit la valeur de la propriété count.
     *
     * @param value the count
     */
    public void setCount(long value) {
        this.count = value;
    }

}
