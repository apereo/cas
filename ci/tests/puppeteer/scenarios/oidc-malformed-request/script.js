
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const service = "https://localhost:9859/anything/unknown";

    let response = await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() >= 400);

    response = await cas.gotoLogin(page, `${service}&client_id=client`);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() >= 400);

    await cas.log("Checking for unauthorized logout redirect...");
    response = await cas.goto(page, `https://localhost:8443/cas/logout?client_id=client&service=${service}`);
    await cas.sleep(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() >= 400);
    
    await verifyResponseTypes(page);
    await cas.closeBrowser(browser);
})();

/**
 * Reads a parameter of the authorization response, which may arrive in the query string or in the fragment.
 *
 * @param page the page
 * @param name the parameter name
 * @returns the value, or null
 */
function responseParameter(page, name) {
    const url = new URL(page.url());
    return url.searchParams.get(name) ?? new URLSearchParams(url.hash.substring(1)).get(name);
}

/**
 * Asks for an authorization response of a given type and returns the error the client receives at its redirect URI.
 *
 * @param page the page
 * @param responseType the response type
 * @returns the error parameter of the response
 */
async function authorizationError(page, responseType) {
    const redirectUri = "https://localhost:9859/anything/cas";
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=${encodeURIComponent(responseType)}`
        + `&client_id=client&scope=openid&redirect_uri=${redirectUri}&state=af0ifjsldkj&nonce=n-0S6_WzA2Mj`;
    await cas.goto(page, url);
    await cas.sleep(1000);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(1000);
    }
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, redirectUri);
    assert.equal(responseParameter(page, "state"), "af0ifjsldkj");
    for (const name of ["code", "id_token", "access_token"]) {
        assert.equal(responseParameter(page, name), null, `No ${name} may be issued for ${responseType}`);
    }
    return responseParameter(page, "error");
}

/**
 * Hybrid response types are not supported: discovery does not list them and asking for one is refused at the redirect
 * URI. A response type the client is not registered for is refused too.
 *
 * @param page the page
 */
async function verifyResponseTypes(page) {
    const discovery = JSON.parse(await cas.doRequest("https://localhost:8443/cas/oidc/.well-known/openid-configuration"));
    for (const hybrid of ["code id_token", "code token", "code id_token token"]) {
        assert(!discovery.response_types_supported.includes(hybrid), `${hybrid} must not be advertised`);
    }
    assert.equal(await authorizationError(page, "code id_token"), "unsupported_response_type");
    assert.equal(await authorizationError(page, "code id_token token"), "unsupported_response_type");
    assert.equal(await authorizationError(page, "token"), "unauthorized_client");
}
