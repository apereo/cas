const assert = require("assert");
const cas = require("../../cas.js");

const pgtUrl = "http://localhost:56789/cas";

async function proxyValidateRequest(service, ticket, pgtCallback = pgtUrl, format = "JSON") {
    let url = `https://localhost:8443/cas/p3/proxyValidate?service=${service}&ticket=${ticket}&format=${format}`;
    if (pgtCallback !== null && pgtCallback !== "") {
        url += `&pgtUrl=${pgtCallback}`;
    }
    const body = await cas.doRequest(`${url}`);
    await cas.log(body);
    return body;
}

async function serviceValidateRequest(service, ticket, format = "XML") {
    const body = await cas.doRequest(`https://localhost:8443/cas/serviceValidate?service=${service}&ticket=${ticket}&format=${format}`);
    await cas.log(body);
    return body;
}

async function requestProxyTicket(service, ticket) {
    const body = await cas.doRequest(`https://localhost:8443/cas/proxy?targetService=${service}&pgt=${ticket}`);
    await cas.log(body);
    const match = body.match(/<cas:proxyTicket>(.*?)<\/cas:proxyTicket>/);
    return match[1];
}

async function assertProxyTicketRejected(body, endpoint) {
    await cas.log(`Response from ${endpoint} must reject the proxy ticket`);
    assert(body.includes("<cas:authenticationSuccess>") === false);
    assert(body.includes("<cas:authenticationFailure"));
    assert(body.includes("code=\"INVALID_TICKET_SPEC\""));
}

(async () => {
    await cas.doDelete("http://localhost:56789/api/baskets/cas/requests", 204,
        () => {
        },
        (err) => {
            throw err;
        }, {
            "Content-Type": "application/json",
            "Authorization": process.env.REQUEST_BASKET_AUTHZ_TOKEN
        });

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";

    await cas.gotoLogin(page, service);
    await cas.loginWith(page);

    const ticket = await cas.assertTicketParameter(page);
    const body = await proxyValidateRequest(service, ticket, pgtUrl);
    const authenticationSuccess = JSON.parse(body).serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.proxyGrantingTicket.includes("PGTIOU-"));

    await cas.doGet("http://localhost:56789/api/baskets/cas/requests?max=1",
        async (res) => {
            const params = new URLSearchParams(res.data.requests[0].query);
            const pgtId = params.get("pgtId");
            await cas.log(`pgtId: ${pgtId}`);

            await cas.logg("Proxy tickets must not validate via the CAS 2.0 service ticket validator");
            let proxyTicket = await requestProxyTicket(pgtUrl, pgtId);
            await assertProxyTicketRejected(await serviceValidateRequest(pgtUrl, proxyTicket), "/serviceValidate");

            await cas.logg("Proxy tickets must not validate via the CAS 3.0 service ticket validator");
            proxyTicket = await requestProxyTicket(pgtUrl, pgtId);
            await assertProxyTicketRejected(await cas.validateTicket(pgtUrl, proxyTicket, "XML"), "/p3/serviceValidate");

            await cas.logg("Proxy tickets must still validate via the proxy ticket validator");
            proxyTicket = await requestProxyTicket(pgtUrl, pgtId);
            const proxyResponse = JSON.parse(await proxyValidateRequest(pgtUrl, proxyTicket, ""));
            assert(proxyResponse.serviceResponse.authenticationSuccess.user === "casuser");
        },
        async (error) => {
            throw error;
        }, {
            "Content-Type": "application/json",
            "Authorization": process.env.REQUEST_BASKET_AUTHZ_TOKEN
        });

    await cas.closeBrowser(browser);
})();
