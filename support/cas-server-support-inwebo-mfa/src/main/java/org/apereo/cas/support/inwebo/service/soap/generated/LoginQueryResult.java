// CHECKSTYLE:OFF
package org.apereo.cas.support.inwebo.service.soap.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

/**
 * The generated SOAP class.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LoginQueryResult", propOrder = {
    "err",
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
    "lastauthdate",
    "nca",
    "caid",
    "castate",
    "caname",
    "cault",
    "caalias",
    "nma",
    "maid",
    "mastate",
    "maname",
    "maalias",
    "mapushenabled",
    "nmac",
    "macid",
    "macstate",
    "macname",
    "macalias",
    "macpushenabled",
    "nva",
    "vaid",
    "vastate",
    "vaname",
    "vaalias",
    "longcode"
})
public class LoginQueryResult {

    @XmlElement(required = true, nillable = true)
    protected String err;

    @XmlElement(required = true, nillable = true)
    protected String login;

    @XmlElement(required = true, nillable = true)
    protected String code;

    protected long status;

    protected long role;

    @XmlElement(required = true, nillable = true)
    protected String firstname;

    @XmlElement(required = true, nillable = true)
    protected String name;

    @XmlElement(required = true, nillable = true)
    protected String mail;

    @XmlElement(required = true, nillable = true)
    protected String phone;

    @XmlElement(required = true, nillable = true)
    protected String extrafields;

    protected long createdby;

    protected long lastauthdate;

    protected long nca;

    @XmlElement(required = true, nillable = true)
    protected List<Long> caid;

    @XmlElement(required = true, nillable = true)
    protected List<Long> castate;

    @XmlElement(required = true, nillable = true)
    protected List<String> caname;

    @XmlElement(required = true, nillable = true)
    protected List<Long> cault;

    @XmlElement(required = true, nillable = true)
    protected List<String> caalias;

    protected long nma;

    @XmlElement(required = true, nillable = true)
    protected List<Long> maid;

    @XmlElement(required = true, nillable = true)
    protected List<Long> mastate;

    @XmlElement(required = true, nillable = true)
    protected List<String> maname;

    @XmlElement(required = true, nillable = true)
    protected List<String> maalias;

    @XmlElement(required = true, nillable = true)
    protected List<Long> mapushenabled;

    protected long nmac;

    @XmlElement(required = true, nillable = true)
    protected List<Long> macid;

    @XmlElement(required = true, nillable = true)
    protected List<Long> macstate;

    @XmlElement(required = true, nillable = true)
    protected List<String> macname;

    @XmlElement(required = true, nillable = true)
    protected List<String> macalias;

    @XmlElement(required = true, nillable = true)
    protected List<Long> macpushenabled;

    protected long nva;

    @XmlElement(required = true, nillable = true)
    protected List<Long> vaid;

    @XmlElement(required = true, nillable = true)
    protected List<Long> vastate;

    @XmlElement(required = true, nillable = true)
    protected List<String> vaname;

    @XmlElement(required = true, nillable = true)
    protected List<String> vaalias;

    @XmlElement(required = true, nillable = true)
    protected String longcode;

    /**
     * Obtient la valeur de la propriété err.
     *
     * @return possible object is
     * {@link String }
     */
    public String getErr() {
        return err;
    }

    /**
     * Définit la valeur de la propriété err.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setErr(final String value) {
        this.err = value;
    }

    /**
     * Obtient la valeur de la propriété login.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLogin() {
        return login;
    }

    /**
     * Définit la valeur de la propriété login.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLogin(final String value) {
        this.login = value;
    }

    /**
     * Obtient la valeur de la propriété code.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCode() {
        return code;
    }

    /**
     * Définit la valeur de la propriété code.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCode(final String value) {
        this.code = value;
    }

    /**
     * Obtient la valeur de la propriété status.
     */
    public long getStatus() {
        return status;
    }

    /**
     * Définit la valeur de la propriété status.
     */
    public void setStatus(final long value) {
        this.status = value;
    }

    /**
     * Obtient la valeur de la propriété role.
     */
    public long getRole() {
        return role;
    }

    /**
     * Définit la valeur de la propriété role.
     */
    public void setRole(final long value) {
        this.role = value;
    }

    /**
     * Obtient la valeur de la propriété firstname.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFirstname() {
        return firstname;
    }

    /**
     * Définit la valeur de la propriété firstname.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFirstname(final String value) {
        this.firstname = value;
    }

    /**
     * Obtient la valeur de la propriété name.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Définit la valeur de la propriété name.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Obtient la valeur de la propriété mail.
     *
     * @return possible object is
     * {@link String }
     */
    public String getMail() {
        return mail;
    }

    /**
     * Définit la valeur de la propriété mail.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMail(final String value) {
        this.mail = value;
    }

    /**
     * Obtient la valeur de la propriété phone.
     *
     * @return possible object is
     * {@link String }
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Définit la valeur de la propriété phone.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPhone(final String value) {
        this.phone = value;
    }

    /**
     * Obtient la valeur de la propriété extrafields.
     *
     * @return possible object is
     * {@link String }
     */
    public String getExtrafields() {
        return extrafields;
    }

    /**
     * Définit la valeur de la propriété extrafields.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setExtrafields(final String value) {
        this.extrafields = value;
    }

    /**
     * Obtient la valeur de la propriété createdby.
     */
    public long getCreatedby() {
        return createdby;
    }

    /**
     * Définit la valeur de la propriété createdby.
     */
    public void setCreatedby(final long value) {
        this.createdby = value;
    }

    /**
     * Obtient la valeur de la propriété lastauthdate.
     */
    public long getLastauthdate() {
        return lastauthdate;
    }

    /**
     * Définit la valeur de la propriété lastauthdate.
     */
    public void setLastauthdate(final long value) {
        this.lastauthdate = value;
    }

    /**
     * Obtient la valeur de la propriété nca.
     */
    public long getNca() {
        return nca;
    }

    /**
     * Définit la valeur de la propriété nca.
     */
    public void setNca(final long value) {
        this.nca = value;
    }

    /**
     * Gets the value of the caid property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the caid property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCaid().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getCaid() {
        if (caid == null) {
            caid = new ArrayList<>();
        }
        return this.caid;
    }

    /**
     * Gets the value of the castate property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the castate property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCastate().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getCastate() {
        if (castate == null) {
            castate = new ArrayList<>();
        }
        return this.castate;
    }

    /**
     * Gets the value of the caname property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the caname property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCaname().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getCaname() {
        if (caname == null) {
            caname = new ArrayList<>();
        }
        return this.caname;
    }

    /**
     * Gets the value of the cault property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the cault property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCault().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getCault() {
        if (cault == null) {
            cault = new ArrayList<>();
        }
        return this.cault;
    }

    /**
     * Gets the value of the caalias property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the caalias property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCaalias().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getCaalias() {
        if (caalias == null) {
            caalias = new ArrayList<>();
        }
        return this.caalias;
    }

    /**
     * Obtient la valeur de la propriété nma.
     */
    public long getNma() {
        return nma;
    }

    /**
     * Définit la valeur de la propriété nma.
     */
    public void setNma(final long value) {
        this.nma = value;
    }

    /**
     * Gets the value of the maid property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the maid property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMaid().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getMaid() {
        if (maid == null) {
            maid = new ArrayList<>();
        }
        return this.maid;
    }

    /**
     * Gets the value of the mastate property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the mastate property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMastate().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getMastate() {
        if (mastate == null) {
            mastate = new ArrayList<>();
        }
        return this.mastate;
    }

    /**
     * Gets the value of the maname property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the maname property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getManame().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getManame() {
        if (maname == null) {
            maname = new ArrayList<>();
        }
        return this.maname;
    }

    /**
     * Gets the value of the maalias property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the maalias property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMaalias().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getMaalias() {
        if (maalias == null) {
            maalias = new ArrayList<>();
        }
        return this.maalias;
    }

    /**
     * Gets the value of the mapushenabled property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the mapushenabled property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapushenabled().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getMapushenabled() {
        if (mapushenabled == null) {
            mapushenabled = new ArrayList<>();
        }
        return this.mapushenabled;
    }

    /**
     * Obtient la valeur de la propriété nmac.
     */
    public long getNmac() {
        return nmac;
    }

    /**
     * Définit la valeur de la propriété nmac.
     */
    public void setNmac(final long value) {
        this.nmac = value;
    }

    /**
     * Gets the value of the macid property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the macid property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMacid().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getMacid() {
        if (macid == null) {
            macid = new ArrayList<>();
        }
        return this.macid;
    }

    /**
     * Gets the value of the macstate property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the macstate property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMacstate().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getMacstate() {
        if (macstate == null) {
            macstate = new ArrayList<>();
        }
        return this.macstate;
    }

    /**
     * Gets the value of the macname property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the macname property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMacname().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getMacname() {
        if (macname == null) {
            macname = new ArrayList<>();
        }
        return this.macname;
    }

    /**
     * Gets the value of the macalias property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the macalias property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMacalias().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getMacalias() {
        if (macalias == null) {
            macalias = new ArrayList<>();
        }
        return this.macalias;
    }

    /**
     * Gets the value of the macpushenabled property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the macpushenabled property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMacpushenabled().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getMacpushenabled() {
        if (macpushenabled == null) {
            macpushenabled = new ArrayList<>();
        }
        return this.macpushenabled;
    }

    /**
     * Obtient la valeur de la propriété nva.
     */
    public long getNva() {
        return nva;
    }

    /**
     * Définit la valeur de la propriété nva.
     */
    public void setNva(final long value) {
        this.nva = value;
    }

    /**
     * Gets the value of the vaid property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the vaid property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVaid().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getVaid() {
        if (vaid == null) {
            vaid = new ArrayList<>();
        }
        return this.vaid;
    }

    /**
     * Gets the value of the vastate property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the vastate property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVastate().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     */
    public List<Long> getVastate() {
        if (vastate == null) {
            vastate = new ArrayList<>();
        }
        return this.vastate;
    }

    /**
     * Gets the value of the vaname property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the vaname property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVaname().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getVaname() {
        if (vaname == null) {
            vaname = new ArrayList<>();
        }
        return this.vaname;
    }

    /**
     * Gets the value of the vaalias property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a {@code set} method for the vaalias property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVaalias().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     */
    public List<String> getVaalias() {
        if (vaalias == null) {
            vaalias = new ArrayList<>();
        }
        return this.vaalias;
    }

    /**
     * Obtient la valeur de la propriété longcode.
     *
     * @return possible object is
     * {@link String }
     */
    public String getLongcode() {
        return longcode;
    }

    /**
     * Définit la valeur de la propriété longcode.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setLongcode(final String value) {
        this.longcode = value;
    }

}
