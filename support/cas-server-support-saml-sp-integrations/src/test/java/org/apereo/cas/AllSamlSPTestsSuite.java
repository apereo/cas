package org.apereo.cas;

import org.apereo.cas.config.CasSamlSPAcademicHealthPlansConfigurationTests;
import org.apereo.cas.config.CasSamlSPAcademicWorksConfigurationTests;
import org.apereo.cas.config.CasSamlSPAdobeCreativeCloudConfigurationTests;
import org.apereo.cas.config.CasSamlSPAmazonConfigurationTests;
import org.apereo.cas.config.CasSamlSPAppDynamicsConfigurationTests;
import org.apereo.cas.config.CasSamlSPArcGISConfigurationTests;
import org.apereo.cas.config.CasSamlSPArmsSoftwareConfigurationTests;
import org.apereo.cas.config.CasSamlSPAsanaConfigurationTests;
import org.apereo.cas.config.CasSamlSPBenefitFocusConfigurationTests;
import org.apereo.cas.config.CasSamlSPBlackBaudConfigurationTests;
import org.apereo.cas.config.CasSamlSPBoxConfigurationTests;
import org.apereo.cas.config.CasSamlSPBynderConfigurationTests;
import org.apereo.cas.config.CasSamlSPCaliforniaCommunityCollegesConfigurationTests;
import org.apereo.cas.config.CasSamlSPCherWellConfigurationTests;
import org.apereo.cas.config.CasSamlSPConcurSolutionsConfigurationTests;
import org.apereo.cas.config.CasSamlSPConfluenceConfigurationTests;
import org.apereo.cas.config.CasSamlSPCraniumCafeConfigurationTests;
import org.apereo.cas.config.CasSamlSPCrashPlanConfigurationTests;
import org.apereo.cas.config.CasSamlSPDocuSignConfigurationTests;
import org.apereo.cas.config.CasSamlSPDropboxConfigurationTests;
import org.apereo.cas.config.CasSamlSPEasyIepConfigurationTests;
import org.apereo.cas.config.CasSamlSPEgnyteConfigurationTests;
import org.apereo.cas.config.CasSamlSPEmmaConfigurationTests;
import org.apereo.cas.config.CasSamlSPEverBridgeConfigurationTests;
import org.apereo.cas.config.CasSamlSPEvernoteConfigurationTests;
import org.apereo.cas.config.CasSamlSPFamisConfigurationTests;
import org.apereo.cas.config.CasSamlSPGartnerConfigurationTests;
import org.apereo.cas.config.CasSamlSPGitlabConfigurationTests;
import org.apereo.cas.config.CasSamlSPGiveCampusConfigurationTests;
import org.apereo.cas.config.CasSamlSPHipchatConfigurationTests;
import org.apereo.cas.config.CasSamlSPInCommonConfigurationTests;
import org.apereo.cas.config.CasSamlSPInfiniteCampusConfigurationTests;
import org.apereo.cas.config.CasSamlSPJiraConfigurationTests;
import org.apereo.cas.config.CasSamlSPNeoGovConfigurationTests;
import org.apereo.cas.config.CasSamlSPNetPartnerConfigurationTests;
import org.apereo.cas.config.CasSamlSPNewRelicConfigurationTests;
import org.apereo.cas.config.CasSamlSPOffice365ConfigurationTests;
import org.apereo.cas.config.CasSamlSPOpenAthensConfigurationTests;
import org.apereo.cas.config.CasSamlSPPagerDutyConfigurationTests;
import org.apereo.cas.config.CasSamlSPPollEverywhereConfigurationTests;
import org.apereo.cas.config.CasSamlSPQualtricsConfigurationTests;
import org.apereo.cas.config.CasSamlSPRocketChatConfigurationTests;
import org.apereo.cas.config.CasSamlSPSTopHatConfigurationTests;
import org.apereo.cas.config.CasSamlSPSaManageConfigurationTests;
import org.apereo.cas.config.CasSamlSPSafariOnlineConfigurationTests;
import org.apereo.cas.config.CasSamlSPSalesforceConfigurationTests;
import org.apereo.cas.config.CasSamlSPSecuringTheHumanConfigurationTests;
import org.apereo.cas.config.CasSamlSPServiceNowConfigurationTests;
import org.apereo.cas.config.CasSamlSPSlackConfigurationTests;
import org.apereo.cas.config.CasSamlSPSunshineStateEdResearchAllianceConfigurationTests;
import org.apereo.cas.config.CasSamlSPSymplicityConfigurationTests;
import org.apereo.cas.config.CasSamlSPTableauConfigurationTests;
import org.apereo.cas.config.CasSamlSPWarpWireConfigurationTests;
import org.apereo.cas.config.CasSamlSPWebAdvisorConfigurationTests;
import org.apereo.cas.config.CasSamlSPWebexConfigurationTests;
import org.apereo.cas.config.CasSamlSPWorkdayConfigurationTests;
import org.apereo.cas.config.CasSamlSPYujaConfigurationTests;
import org.apereo.cas.config.CasSamlSPZendeskConfigurationTests;
import org.apereo.cas.config.CasSamlSPZimbraConfigurationTests;
import org.apereo.cas.config.CasSamlSPZoomConfigurationTests;
import org.apereo.cas.util.SamlSPUtilsTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllSamlSPTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    CasSamlSPSlackConfigurationTests.class,
    CasSamlSPWebexConfigurationTests.class,
    CasSamlSPEmmaConfigurationTests.class,
    CasSamlSPSunshineStateEdResearchAllianceConfigurationTests.class,
    CasSamlSPArcGISConfigurationTests.class,
    CasSamlSPSaManageConfigurationTests.class,
    CasSamlSPGitlabConfigurationTests.class,
    CasSamlSPAsanaConfigurationTests.class,
    CasSamlSPSTopHatConfigurationTests.class,
    CasSamlSPWorkdayConfigurationTests.class,
    CasSamlSPSymplicityConfigurationTests.class,
    CasSamlSPJiraConfigurationTests.class,
    CasSamlSPDropboxConfigurationTests.class,
    CasSamlSPEverBridgeConfigurationTests.class,
    CasSamlSPNetPartnerConfigurationTests.class,
    CasSamlSPZoomConfigurationTests.class,
    CasSamlSPEasyIepConfigurationTests.class,
    CasSamlSPSalesforceConfigurationTests.class,
    CasSamlSPYujaConfigurationTests.class,
    CasSamlSPRocketChatConfigurationTests.class,
    CasSamlSPWebAdvisorConfigurationTests.class,
    CasSamlSPFamisConfigurationTests.class,
    CasSamlSPOpenAthensConfigurationTests.class,
    CasSamlSPArmsSoftwareConfigurationTests.class,
    CasSamlSPConcurSolutionsConfigurationTests.class,
    CasSamlSPNewRelicConfigurationTests.class,
    CasSamlSPCrashPlanConfigurationTests.class,
    CasSamlSPZimbraConfigurationTests.class,
    CasSamlSPNeoGovConfigurationTests.class,
    CasSamlSPEvernoteConfigurationTests.class,
    CasSamlSPAmazonConfigurationTests.class,
    CasSamlSPAppDynamicsConfigurationTests.class,
    CasSamlSPCherWellConfigurationTests.class,
    CasSamlSPAcademicWorksConfigurationTests.class,
    CasSamlSPConfluenceConfigurationTests.class,
    CasSamlSPHipchatConfigurationTests.class,
    CasSamlSPBlackBaudConfigurationTests.class,
    CasSamlSPBenefitFocusConfigurationTests.class,
    CasSamlSPBynderConfigurationTests.class,
    CasSamlSPAcademicHealthPlansConfigurationTests.class,
    CasSamlSPTableauConfigurationTests.class,
    CasSamlSPCraniumCafeConfigurationTests.class,
    CasSamlSPDocuSignConfigurationTests.class,
    CasSamlSPPollEverywhereConfigurationTests.class,
    CasSamlSPGiveCampusConfigurationTests.class,
    CasSamlSPEgnyteConfigurationTests.class,
    CasSamlSPWarpWireConfigurationTests.class,
    CasSamlSPBoxConfigurationTests.class,
    CasSamlSPSecuringTheHumanConfigurationTests.class,
    CasSamlSPPagerDutyConfigurationTests.class,
    CasSamlSPGartnerConfigurationTests.class,
    CasSamlSPInCommonConfigurationTests.class,
    CasSamlSPAdobeCreativeCloudConfigurationTests.class,
    CasSamlSPInfiniteCampusConfigurationTests.class,
    CasSamlSPServiceNowConfigurationTests.class,
    CasSamlSPOffice365ConfigurationTests.class,
    CasSamlSPSafariOnlineConfigurationTests.class,
    CasSamlSPQualtricsConfigurationTests.class,
    CasSamlSPZendeskConfigurationTests.class,
    CasSamlSPCaliforniaCommunityCollegesConfigurationTests.class,
    SamlSPUtilsTests.class
})
@RunWith(JUnitPlatform.class)
public class AllSamlSPTestsSuite {
}
