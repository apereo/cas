const assert = require("assert");
const cas = require("../../cas.js");

const CAS_V1_FAILURE = "no\n\n";

async function validate(endpoint, query) {
    const body = await cas.doRequest(`https://localhost:8443/cas/${endpoint}?${query}`);
    await cas.log(body);
    return body;
}

async function serviceTicket(page, service) {
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
    }
    return cas.assertTicketParameter(page);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service);
    await cas.attributeValue(page, "#username", "autocapitalize", "none");
    await cas.attributeValue(page, "#username", "spellcheck", "false");
    await cas.attributeValue(page, "#username", "autocomplete", "username");
    await cas.loginWith(page);
    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/validate?service=${service}&ticket=${ticket}`);
    await cas.log(body);
    assert(body === "yes\ncasuser\n");

    await cas.log("A service ticket cannot be validated twice");
    assert(await validate("validate", `service=${service}&ticket=${ticket}`) === CAS_V1_FAILURE);

    await cas.log("Requests without a ticket or without a service are invalid");
    assert(await validate("validate", `service=${service}`) === CAS_V1_FAILURE);
    assert(await validate("validate", `ticket=${ticket}`) === CAS_V1_FAILURE);

    await cas.log("A ticket not issued by CAS is refused");
    assert(await validate("validate", `service=${service}&ticket=ST-1-forged-ticket`) === CAS_V1_FAILURE);

    const v3OnlyService = "https://localhost:9859/anything/v3only";
    await cas.log("A ticket validated for another service is refused, and the failed attempt consumes it");
    let mismatched = await serviceTicket(page, service);
    assert(await validate("validate", `service=${v3OnlyService}&ticket=${mismatched}`) === CAS_V1_FAILURE);
    assert(await validate("validate", `service=${service}&ticket=${mismatched}`) === CAS_V1_FAILURE);

    await cas.log("The same mismatch over CAS v2 reports INVALID_SERVICE");
    mismatched = await serviceTicket(page, service);
    let xml = await validate("serviceValidate", `service=${v3OnlyService}&ticket=${mismatched}`);
    assert(xml.includes("<cas:authenticationFailure code=\"INVALID_SERVICE\">"));

    await cas.log("A service that only supports CAS v3 refuses tickets validated over CAS v1 and v2");
    let v3Ticket = await serviceTicket(page, v3OnlyService);
    assert(await validate("validate", `service=${v3OnlyService}&ticket=${v3Ticket}`) === CAS_V1_FAILURE);

    v3Ticket = await serviceTicket(page, v3OnlyService);
    xml = await validate("serviceValidate", `service=${v3OnlyService}&ticket=${v3Ticket}`);
    assert(xml.includes("<cas:authenticationFailure code=\"INVALID_TICKET_SPEC\">"));
    assert(!xml.includes("<cas:authenticationSuccess>"));

    v3Ticket = await serviceTicket(page, v3OnlyService);
    let json = JSON.parse(await validate("serviceValidate", `service=${v3OnlyService}&ticket=${v3Ticket}&format=JSON`));
    assert(json.serviceResponse.authenticationFailure.code === "INVALID_TICKET_SPEC");
    assert(json.serviceResponse.authenticationSuccess === undefined);

    await cas.log("The same service accepts tickets validated over CAS v3, in XML and JSON");
    v3Ticket = await serviceTicket(page, v3OnlyService);
    xml = await validate("p3/serviceValidate", `service=${v3OnlyService}&ticket=${v3Ticket}`);
    assert(xml.includes("<cas:user>casuser</cas:user>"));

    v3Ticket = await serviceTicket(page, v3OnlyService);
    json = JSON.parse(await validate("p3/serviceValidate", `service=${v3OnlyService}&ticket=${v3Ticket}&format=JSON`));
    assert(json.serviceResponse.authenticationSuccess.user === "casuser");

    await cas.closeBrowser(browser);
})();
