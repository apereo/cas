const assert = require("assert");
const cas = require("../../cas.js");

const pgtUrl = "http://localhost:56789/cas";
const REQUEST_BASKET_AUTHZ_TOKEN = "SV00cPIKRdWjGkN1vkbEbPdhtvV5vIJ0ajygcdnZVBgl";

async function proxyValidateRequest(service, ticket, pgtCallback = pgtUrl, format = "JSON") {
    let url = `https://localhost:8443/cas/p3/proxyValidate?service=${service}&ticket=${ticket}&format=${format}`;
    if (pgtCallback !== null && pgtCallback !== "") {
        url += `&pgtUrl=${pgtCallback}`;
    }
    const body = await cas.doRequest(`${url}`);
    await cas.log(body);
    return body;
}

async function requestProxyTicket(service, ticket) {
    const body = await cas.doRequest(`https://localhost:8443/cas/proxy?targetService=${service}&pgt=${ticket}`);
    await cas.log(body);
    const match = body.match(/<cas:proxyTicket>(.*?)<\/cas:proxyTicket>/);
    return match[1];
}

(async () => {
    await cas.doDelete("http://localhost:56789/api/baskets/cas/requests", 204,
        () => {
        },
        (err) => {
            throw err;
        }, {
            "Content-Type": "application/json",
            "Authorization": REQUEST_BASKET_AUTHZ_TOKEN
        });

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";

    await cas.gotoLogin(page, service);
    await cas.loginWith(page);

    let ticket = await cas.assertTicketParameter(page);
    const body = await proxyValidateRequest(service, ticket, pgtUrl);

    const json = JSON.parse(body);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.attributes.credentialType !== undefined);
    assert(authenticationSuccess.proxyGrantingTicket.includes("PGTIOU-"));

    await cas.doGet("http://localhost:56789/api/baskets/cas/requests?max=1",
        async (res) => {
            const query = res.data.requests[0].query;
            const params = new URLSearchParams(query);
            const pgtId = params.get("pgtId");
            await cas.log("pgtIou:", params.get("pgtIou"));
            await cas.log("pgtId:", pgtId);
            
            let proxyTicket = await requestProxyTicket(pgtUrl, pgtId);
            let proxyResponse = JSON.parse(await proxyValidateRequest(pgtUrl, proxyTicket, ""));
            const authenticationSuccess = proxyResponse.serviceResponse.authenticationSuccess;
            assert(authenticationSuccess.user === "casuser");
            assert(authenticationSuccess.proxies[0] === pgtUrl);

            proxyTicket = await requestProxyTicket(pgtUrl, pgtId);
            proxyResponse = await proxyValidateRequest(pgtUrl, proxyTicket, "", "XML");
            assert(proxyResponse.includes(`<cas:proxy>${pgtUrl}</cas:proxy>`));
        },
        async (error) => {
            throw (error);
        }, {
            "Content-Type": "application/json",
            "Authorization": REQUEST_BASKET_AUTHZ_TOKEN
        });

    await cas.sleep(1000);
    await cas.gotoLogin(page, service);
    ticket = await cas.assertTicketParameter(page);
    const xmlBody = await proxyValidateRequest(service, ticket, pgtUrl, "XML");
    assert(xmlBody.includes("<cas:proxyGrantingTicket>"));
    assert(xmlBody.includes("<cas:user>casuser</cas:user>"));
    await cas.sleep(1000);
    await browser.close();
})();
