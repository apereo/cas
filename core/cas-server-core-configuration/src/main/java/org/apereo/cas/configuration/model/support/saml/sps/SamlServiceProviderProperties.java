package org.apereo.cas.configuration.model.support.saml.sps;

import java.util.Arrays;

/**
 * This is {@link SamlServiceProviderProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlServiceProviderProperties {
    private Dropbox dropbox = new Dropbox();
    private Workday workday = new Workday();
    private SAManage saManage = new SAManage();
    private Salesforce salesforce = new Salesforce();
    private ServiceNow serviceNow = new ServiceNow();
    private Box box = new Box();
    private NetPartner netPartner = new NetPartner();
    private Webex webex = new Webex();
    private Office365 office365 = new Office365();
    private TestShib testShib = new TestShib();
    private InCommon inCommon = new InCommon();
    private Zoom zoom = new Zoom();
    private Evernote evernote = new Evernote();
    private Asana asana = new Asana();
    private Tableau tableau = new Tableau();
    private WebAdvisor webAdvisor = new WebAdvisor();

    public WebAdvisor getWebAdvisor() {
        return webAdvisor;
    }

    public void setWebAdvisor(final WebAdvisor webAdvisor) {
        this.webAdvisor = webAdvisor;
    }

    public Tableau getTableau() {
        return tableau;
    }

    public void setTableau(final Tableau tableau) {
        this.tableau = tableau;
    }

    public Asana getAsana() {
        return asana;
    }

    public void setAsana(final Asana asana) {
        this.asana = asana;
    }

    public Evernote getEvernote() {
        return evernote;
    }

    public void setEvernote(final Evernote evernote) {
        this.evernote = evernote;
    }

    public Zoom getZoom() {
        return zoom;
    }

    public void setZoom(final Zoom zoom) {
        this.zoom = zoom;
    }
    
    public InCommon getInCommon() {
        return inCommon;
    }

    public void setInCommon(final InCommon inCommon) {
        this.inCommon = inCommon;
    }

    public TestShib getTestShib() {
        return testShib;
    }

    public void setTestShib(final TestShib testShib) {
        this.testShib = testShib;
    }

    public Office365 getOffice365() {
        return office365;
    }

    public void setOffice365(final Office365 office365) {
        this.office365 = office365;
    }

    public Webex getWebex() {
        return webex;
    }

    public void setWebex(final Webex webex) {
        this.webex = webex;
    }

    public NetPartner getNetPartner() {
        return netPartner;
    }

    public void setNetPartner(final NetPartner netPartner) {
        this.netPartner = netPartner;
    }

    public ServiceNow getServiceNow() {
        return serviceNow;
    }

    public void setServiceNow(final ServiceNow serviceNow) {
        this.serviceNow = serviceNow;
    }

    public Box getBox() {
        return box;
    }

    public void setBox(final Box box) {
        this.box = box;
    }

    public Salesforce getSalesforce() {
        return salesforce;
    }

    public void setSalesforce(final Salesforce salesforce) {
        this.salesforce = salesforce;
    }

    public SAManage getSaManage() {
        return saManage;
    }

    public void setSaManage(final SAManage saManage) {
        this.saManage = saManage;
    }

    public Workday getWorkday() {
        return workday;
    }

    public void setWorkday(final Workday workday) {
        this.workday = workday;
    }

    public Dropbox getDropbox() {
        return dropbox;
    }

    public void setDropbox(final Dropbox dropbox) {
        this.dropbox = dropbox;
    }

    public static class Dropbox extends AbstractSamlSPProperties {
        public Dropbox() {
            setNameIdAttribute("mail");
        }
    }

    public static class Box extends AbstractSamlSPProperties {
        public Box() {
            setAttributes(Arrays.asList("email", "firstName", "lastName"));
        }
    }

    public static class SAManage extends AbstractSamlSPProperties {
        public SAManage() {
            setNameIdAttribute("mail");
        }
    }

    public static class Workday extends AbstractSamlSPProperties {
    }

    public static class Salesforce extends AbstractSamlSPProperties {
        public Salesforce() {
            setAttributes(Arrays.asList("mail", "eduPersonPrincipalName"));
        }
    }

    public static class ServiceNow extends AbstractSamlSPProperties {
        public ServiceNow() {
            setAttributes(Arrays.asList("eduPersonPrincipalName"));
        }
    }

    public static class NetPartner extends AbstractSamlSPProperties {
        public NetPartner() {
            setNameIdAttribute("studentId");
        }
    }
    
    public static class Office365 extends AbstractSamlSPProperties {
        public Office365() {
            setNameIdAttribute("scopedImmutableID");
            setAttributes(Arrays.asList("IDPEmail,ImmutableID"));
        }
    }
    
    public static class WebAdvisor extends AbstractSamlSPProperties {
        public WebAdvisor() {
            setAttributes(Arrays.asList("uid"));
        }
    }
    
    public static class Webex extends AbstractSamlSPProperties {
        public Webex() {
            setNameIdAttribute("email");
            setAttributes(Arrays.asList("firstName,lastName"));
        }
    }
    
    public static class Tableau extends AbstractSamlSPProperties {
        public Tableau() {
            setAttributes(Arrays.asList("username"));
        }
    }
    
    public static class TestShib extends AbstractSamlSPProperties {
        public TestShib() {
            //setMetadata("http://www.testshib.org/metadata/testshib-providers.xml");
            setAttributes(Arrays.asList("eduPersonPrincipalName"));
        }
    }

    public static class Zoom extends AbstractSamlSPProperties {
        public Zoom() {
            setNameIdAttribute("mail");
            setAttributes(Arrays.asList("mail,sn,givenName"));
        }
    }
    
    public static class InCommon extends AbstractSamlSPProperties {
        public InCommon() {
            //setMetadata("http://md.incommon.org/InCommon/InCommon-metadata.xml");
            //setSignatureLocation("/etc/cas/config/certs/inc-md-cert.pem");
            setAttributes(Arrays.asList("eduPersonPrincipalName"));
        }
    }
    
    public static class Evernote extends AbstractSamlSPProperties {
        public Evernote() {
            setNameIdAttribute("email");
            setNameIdFormat("emailAddress");
        }
    }
    
    public static class Asana extends AbstractSamlSPProperties {
        public Asana() {
            setNameIdAttribute("email");
            setNameIdFormat("emailAddress");
        }
    }
}
