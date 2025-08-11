
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    for (const endpoint of ["validate", "serviceValidate", "p3/serviceValidate"]) {
        await cas.log(`Checking validation endpoint: ${endpoint}`);

        const browser = await cas.newBrowser(cas.browserOptions());
        const page = await cas.newPage(browser);

        const service1 = "https://localhost:9859/get";
        await cas.log(`Logging into ${service1} without renew to create SSO`);
        await cas.gotoLogin(page, service1);
        await cas.loginWith(page);

        let ticket = await cas.assertTicketParameter(page);
        let body = await validate(endpoint, service1, ticket, false);

        if (endpoint === "validate") {
            assert(body === "yes\ncasuser\n");
        } else {
            assert(body.includes("<cas:authenticationSuccess>"));
        }

        const service2 = "https://localhost:9859/get";
        await cas.log(`Logging into ${service2} to validate with renew=true and existing SSO`);
        await cas.gotoLogin(page, service2);
        ticket = await cas.assertTicketParameter(page);
        body = await validate(endpoint, service2, ticket, true);

        if (endpoint === "validate") {
            assert(body === "no\n\n");
        } else {
            assert(body.includes("<cas:authenticationFailure code=\"INVALID_TICKET\">"));
        }
        await cas.closeBrowser(browser);
    }
})();

async function validate(endpoint, service, ticket, renew = false) {
    let path = `/cas/${endpoint}?service=${service}&ticket=${ticket}`;
    if (renew) {
        path = `${path}&renew=true`;
    }
    await cas.log(`Validating ${path}`);
    const result = await cas.doRequest(`https://localhost:8443${path}`);
    await cas.log(result);
    return result;
}
