
const cas = require("../../cas.js");
const assert = require("assert");

/**
 * An unsigned request object, whose algorithm is none, carries no proof of who sent it: it is refused with
 * invalid_request_object at the registered redirect URI, and discovery does not advertise none for request objects.
 */
(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUri = "https://localhost:9859/anything/cas";
    const request = "eyJhbGciOiJub25lIn0.eyJyZXNwb25zZV90eXBlIjoiY29kZSIsInJlZGlyZWN0X3VyaSI6Imh0dHBzOi8vbG9jYWxob3N0Ojk4NTkvYW55dGhpbmcvY2FzIiwic3RhdGUiOiJ2SU4xYjBZNENrIiwibm9uY2UiOiIxTjltcVBPOWZ0IiwiY2xpZW50X2lkIjoiY2xpZW50Iiwic2NvcGUiOiJvcGVuaWQifQ.";
    const url = `https://localhost:8443/cas/oidc/authorize?request=${request}&client_id=client&scope=openid&redirect_uri=${redirectUri}`;

    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.sleep(1000);
    assert(page.url().startsWith(redirectUri), page.url());
    const error = new URL(page.url()).searchParams.get("error");
    assert.equal(error, "invalid_request_object");

    const discovery = JSON.parse(await cas.doRequest("https://localhost:8443/cas/oidc/.well-known/openid-configuration", "GET", {}, 200));
    assert(!discovery.request_object_signing_alg_values_supported.includes("none"));

    await cas.closeBrowser(browser);
})();
