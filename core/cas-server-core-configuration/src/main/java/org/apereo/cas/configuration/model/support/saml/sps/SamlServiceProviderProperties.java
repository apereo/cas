package org.apereo.cas.configuration.model.support.saml.sps;

import java.util.Arrays;

/**
 * This is {@link SamlServiceProviderProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlServiceProviderProperties {
    private static final String EMAIL = "email";
    private static final String PRINCIPAL_NAME = "eduPersonPrincipalName";

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
    private Gartner gartner = new Gartner();
    private Tableau tableau = new Tableau();
    private WebAdvisor webAdvisor = new WebAdvisor();
    private OpenAthens openAthens = new OpenAthens();
    private ArcGIS arcGIS = new ArcGIS();
    private BenefitFocus benefitFocus = new BenefitFocus();
    private AdobeCloud adobeCloud = new AdobeCloud();
    private AcademicWorks academicWorks = new AcademicWorks();
    private EasyIep easyIep = new EasyIep();
    private InfiniteCampus infiniteCampus = new InfiniteCampus();
    private SecuringTheHuman sansSth = new SecuringTheHuman();
    private Slack slack = new Slack();
    private Zendesk zendesk = new Zendesk();

    public Gartner getGartner() {
        return gartner;
    }

    public void setGartner(final Gartner gartner) {
        this.gartner = gartner;
    }

    public Zendesk getZendesk() {
        return zendesk;
    }

    public void setZendesk(final Zendesk zendesk) {
        this.zendesk = zendesk;
    }

    public Slack getSlack() {
        return slack;
    }

    public void setSlack(final Slack slack) {
        this.slack = slack;
    }

    public SecuringTheHuman getSansSth() {
        return sansSth;
    }

    public void setSansSth(final SecuringTheHuman sansSth) {
        this.sansSth = sansSth;
    }

    public InfiniteCampus getInfiniteCampus() {
        return infiniteCampus;
    }

    public void setInfiniteCampus(final InfiniteCampus infiniteCampus) {
        this.infiniteCampus = infiniteCampus;
    }

    public EasyIep getEasyIep() {
        return easyIep;
    }

    public void setEasyIep(final EasyIep easyIep) {
        this.easyIep = easyIep;
    }

    public AcademicWorks getAcademicWorks() {
        return academicWorks;
    }

    public void setAcademicWorks(final AcademicWorks academicWorks) {
        this.academicWorks = academicWorks;
    }

    public AdobeCloud getAdobeCloud() {
        return adobeCloud;
    }

    public void setAdobeCloud(final AdobeCloud adobeCloud) {
        this.adobeCloud = adobeCloud;
    }

    public ArcGIS getArcGIS() {
        return arcGIS;
    }

    public void setArcGIS(final ArcGIS arcGIS) {
        this.arcGIS = arcGIS;
    }

    public OpenAthens getOpenAthens() {
        return openAthens;
    }

    public void setOpenAthens(final OpenAthens openAthens) {
        this.openAthens = openAthens;
    }

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

    public BenefitFocus getBenefitFocus() {
        return benefitFocus;
    }

    public void setBenefitFocus(final BenefitFocus benefitFocus) {
        this.benefitFocus = benefitFocus;
    }

    public static class Dropbox extends AbstractSamlSPProperties {
        public Dropbox() {
            setNameIdAttribute("mail");
        }
    }

    public static class Box extends AbstractSamlSPProperties {
        public Box() {
            setAttributes(Arrays.asList(EMAIL, "firstName", "lastName"));
        }
    }

    public static class SAManage extends AbstractSamlSPProperties {
        public SAManage() {
            setNameIdAttribute("mail");
        }
    }

    public static class Workday extends AbstractSamlSPProperties {
        public Workday() {
            setSignAssertions(true);
            setSignResponses(true);
        }
    }

    public static class Salesforce extends AbstractSamlSPProperties {
        public Salesforce() {
            setAttributes(Arrays.asList("mail", PRINCIPAL_NAME));
        }
    }

    public static class ServiceNow extends AbstractSamlSPProperties {
        public ServiceNow() {
            setAttributes(Arrays.asList(PRINCIPAL_NAME));
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
            setSignResponses(false);
            setSignAssertions(true);
        }
    }

    public static class WebAdvisor extends AbstractSamlSPProperties {
        public WebAdvisor() {
            setAttributes(Arrays.asList("uid"));
        }
    }

    public static class Webex extends AbstractSamlSPProperties {
        public Webex() {
            setNameIdAttribute(EMAIL);
            setAttributes(Arrays.asList("firstName,lastName"));
            setSignResponses(false);
            setSignAssertions(true);
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
            setAttributes(Arrays.asList(PRINCIPAL_NAME));
        }
    }

    public static class Zoom extends AbstractSamlSPProperties {
        public Zoom() {
            setNameIdAttribute("mail");
            setAttributes(Arrays.asList("mail,sn,givenName"));
        }
    }

    public static class ArcGIS extends AbstractSamlSPProperties {
        public ArcGIS() {
            setNameIdAttribute("arcNameId");
            setAttributes(Arrays.asList("mail,givenName,arcNameId"));
        }
    }

    public static class InCommon extends AbstractSamlSPProperties {
        public InCommon() {
            //setMetadata("http://md.incommon.org/InCommon/InCommon-metadata.xml");
            //setSignatureLocation("/etc/cas/config/certs/inc-md-cert.pem");
            setAttributes(Arrays.asList(PRINCIPAL_NAME));
        }
    }

    public static class Evernote extends AbstractSamlSPProperties {
        public Evernote() {
            setNameIdAttribute(EMAIL);
            setNameIdFormat("emailAddress");
        }
    }

    public static class Asana extends AbstractSamlSPProperties {
        public Asana() {
            setNameIdAttribute(EMAIL);
            setNameIdFormat("emailAddress");
        }
    }

    public static class OpenAthens extends AbstractSamlSPProperties {
        public OpenAthens() {
            //setMetadata("https://login.openathens.net/saml/2/metadata-sp");
            setAttributes(Arrays.asList(PRINCIPAL_NAME, EMAIL));
        }
    }

    public static class BenefitFocus extends AbstractSamlSPProperties {
        public BenefitFocus() {
            setNameIdAttribute("benefitFocusUniqueId");
        }
    }

    public static class AdobeCloud extends AbstractSamlSPProperties {
        public AdobeCloud() {
            setAttributes(Arrays.asList("firstName", "lastName", EMAIL));
        }
    }

    public static class AcademicWorks extends AbstractSamlSPProperties {
        public AcademicWorks() {
            setAttributes(Arrays.asList("displayName", EMAIL));
        }
    }

    public static class EasyIep extends AbstractSamlSPProperties {
        public EasyIep() {
            setAttributes(Arrays.asList("employeeId"));
        }
    }

    public static class InfiniteCampus extends AbstractSamlSPProperties {
        public InfiniteCampus() {
            setAttributes(Arrays.asList("employeeId"));
        }
    }

    public static class SecuringTheHuman extends AbstractSamlSPProperties {
        public SecuringTheHuman() {
            setAttributes(Arrays.asList("firstName", "lastName", EMAIL, "scopedUserId", "department", "reference"));
        }
    }

    public static class Slack extends AbstractSamlSPProperties {
        public Slack() {
            setNameIdFormat("persistent");
            setAttributes(Arrays.asList("User.Email", "User.Username", "first_name", "last_name"));
            setNameIdAttribute("employeeId");
        }
    }

    public static class Zendesk extends AbstractSamlSPProperties {
        public Zendesk() {
            setNameIdFormat("emailAddress");
            setNameIdAttribute("email");
            setAttributes(Arrays.asList("organization", "tags", "phone", "role"));
        }
    }

    public static class Gartner extends AbstractSamlSPProperties {
        public Gartner() {
            setAttributes("urn:oid:2.5.4.42", "urn:oid:2.5.4.4", "urn:oid:0.9.2342.19200300.100.1.3");
        }
    }
}
