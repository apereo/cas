package org.apereo.cas.adaptors.u2f.web.flow;

import org.apereo.cas.adaptors.u2f.U2FRegistration;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link U2FAccountSaveRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    BaseU2FWebflowActionTests.U2FTestConfiguration.class,
    BaseU2FWebflowActionTests.SharedTestConfiguration.class
})
@Tag("Webflow")
public class U2FAccountSaveRegistrationActionTests extends BaseU2FWebflowActionTests {

    @Test
    public void verifyOperation() throws Exception {
        val id = UUID.randomUUID().toString();
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(id), context);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, u2fStartRegistrationAction.execute(context).getId());

        val u2fReg = context.getFlowScope().get("u2fReg", U2FRegistration.class);
        deviceRepository.requestDeviceRegistration("fb36YebRZyTcR_p4SWrSI7UIJvB6FP82tkcg5AGHhJM", id, u2fReg.getJsonData());

        val tokenResponse = "{\"registrationData\":\"BQQkG0HfMv4hzlfHpaWySCKdvio0-w1zi6s"
            + "uSu0_QJiL4vE-DwggXFWxuWn7rkae1ZAasYl_YnQny7ldxYoGA9riQL2ast4dMon"
            + "mEtZ80VYKltXyWevhxR11OwF07GLZUcETjPlNGE8WFuanXXQo6-6cujXTZ6yltF8iME66jb8I3"
            + "hEwggEbMIHCoAMCAQICCjgtyGkJIYxviTAwCgYIKoZIzj0EAwIwFTETMBEGA1"
            + "UEAxMKVTJGIElzc3VlcjAaFwswMDAxMDEwMDAwWhcLMDAwMTAxMDAwMFowFTETMBEGA1UEAxMKV"
            + "TJGIERldmljZTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABE0eSLFD2Bg3YsE--2pDCrvhQ7_5Liwn"
            + "ssiRSdzgHQHo2RLtnhVJ_EDZkuuzxCeQoSkZBGJkARxjO_bcCitAjDwwCgYIKoZ"
            + "Izj0EAwIDSAAwRQIhAMGjpo4vFqchRicFf2K7coyeA-ehumLQRlJORW0sLz9zAiALX3jlEaoYEp9vI2"
            + "2SEyJ9krTmft9T6BbfsF2dyLkP3jBEAiB9VOA7XifeRUNKa9TefQog57-ojDsdWcDrjKTb5GQf0"
            + "QIgJzxbUiQam_olH10kq3y2FIosS8PTc2LhcotPH7_3uHU\",\"version\":\"U2F_V2\","
            + "\"challenge\":\"fb36YebRZyTcR_p4SWrSI7UIJvB6FP82tkcg5AGHhJM\",\""
            + "clientData\":\"eyJjaGFsbGVuZ2UiOiJmYjM2WWViUlp5VGNSX3A0U1dyU0k"
            + "3VUlKdkI2RlA4MnRrY2c1QUdIaEpNIiwib3J"
            + "pZ2luIjoiaHR0cHM6Ly9tbW9heXllZC51bmljb24ubmV0Ojg0NDMiLCJ0eXAiO"
            + "iJuYXZpZ2F0b3IuaWQuZmluaXNoRW5yb2xsbWVudCJ9\"}";

        request.addParameter("tokenResponse", tokenResponse);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, u2fSaveAccountRegistrationAction.execute(context).getId());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, u2fCheckAccountRegistrationAction.execute(context).getId());
    }
}
