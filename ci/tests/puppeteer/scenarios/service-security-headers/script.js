const assert = require("assert");
const cas = require("../../cas.js");

/**
 * Reads the response headers CAS sends for a request that names a service. Ticket validation is used rather than
 * the login page because the login flow sets its own cache headers on top of the ones decided for the service.
 *
 * @param service the service URL
 * @returns the response headers
 */
async function serviceHeaders(service) {
    let headers = {};
    await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=ST-unknown`, "GET", {}, 0, undefined,
        (res) => {
            headers = res.headers;
        });
    await cas.log(headers);
    return headers;
}

/**
 * RequestParameterPolicyEnforcementFilter throws from the servlet filter chain when a request breaks the
 * cas.http-web-request policy, which the container answers with 500 before any controller runs.
 */
async function verifyRequestParameterPolicy() {
    const login = "https://localhost:8443/cas/login";
    const service = "https://localhost:9859/anything/default";
    const blocked = 500;

    await cas.logg("A parameter listed in params-to-check may not repeat");
    await cas.doRequest(`${login}?service=${service}&service=https://localhost:9859/anything/custom`, "GET", {}, blocked);
    await cas.doRequest(`${login}?locale=en&locale=de`, "GET", {}, blocked);
    await cas.logg("A parameter left out of params-to-check may repeat");
    await cas.doRequest(`${login}?unchecked=en&unchecked=de`, "GET", {}, 200);

    await cas.logg("Parameters listed in only-post-params are refused on GET");
    await cas.doRequest(`${login}?username=casuser`, "GET", {}, blocked);
    await cas.doRequest(`${login}?password=Mellon`, "GET", {}, blocked);

    await cas.logg("A forbidden character in a checked parameter is refused");
    await cas.doRequest(`${login}?service=${encodeURIComponent(`${service}/<x`)}`, "GET", {}, blocked);
    await cas.doRequest(`${login}?unchecked=${encodeURIComponent("<x")}`, "GET", {}, 200);

    await cas.logg("A request URI matching pattern-to-block is refused, whatever the parameter");
    await cas.doRequest(`${login}?unchecked=javascript:void`, "GET", {}, blocked);
    await cas.doRequest(`${login}?unchecked=javascript%3Avoid`, "GET", {}, blocked);

    await cas.logg("A well-formed login still works, posting the credentials");
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page, service);
        await cas.loginWith(page);
        await cas.sleep(1000);
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(service, ticket);
        assert.equal(json.serviceResponse.authenticationSuccess.user, "casuser");
    } finally {
        await cas.closeBrowser(browser);
    }
}

(async () => {
    await cas.logg("A service without header properties gets the global headers; X-XSS-Protection is turned off globally");
    let headers = await serviceHeaders("https://localhost:9859/anything/default");
    assert.equal(headers["x-frame-options"], "DENY");
    assert(headers["content-security-policy"] !== undefined);
    assert.equal(headers["strict-transport-security"], "max-age=15768000 ; includeSubDomains");
    assert(headers["cache-control"] !== undefined);
    assert.equal(headers["pragma"], "no-cache");
    assert.equal(headers["x-content-type-options"], "nosniff");
    assert.equal(headers["x-xss-protection"], undefined);

    await cas.logg("A service can turn the global headers off");
    headers = await serviceHeaders("https://localhost:9859/anything/relaxed");
    for (const name of ["x-frame-options", "content-security-policy", "strict-transport-security", "cache-control", "pragma", "x-content-type-options"]) {
        assert.equal(headers[name], undefined, `${name} must not be sent for the relaxed service`);
    }

    await cas.logg("A service can set its own values and turn on a header that is off globally");
    headers = await serviceHeaders("https://localhost:9859/anything/custom");
    assert.equal(headers["x-frame-options"], "SAMEORIGIN");
    assert.equal(headers["strict-transport-security"], "max-age=60");
    assert.equal(headers["x-xss-protection"], "1; mode=block");
    assert(headers["content-security-policy"] !== undefined);

    await verifyRequestParameterPolicy();
})();
