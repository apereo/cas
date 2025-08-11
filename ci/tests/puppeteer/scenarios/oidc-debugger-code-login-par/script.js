
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const authzUrl = "https://localhost:8443/cas/oidc/oidcAuthorize";
    const params = "client_id=client&" +
        "redirect_uri=https://localhost:9859/post&" +
        `scope=${encodeURIComponent("openid email profile address phone")}&` +
        "response_type=code&" +
        "response_mode=form_post&" +
        "nonce=vn4qulthnx";
    
    let url = `${authzUrl}?${params}`;
    let response = await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.log(`Status: ${response.status()} ${response.statusText()}`);
    assert(response.status() === 403);

    const value = "client:secret";
    const buff = Buffer.alloc(value.length, value);
    const authzHeader = `Basic ${buff.toString("base64")}`;
    await cas.log(`Authorization header: ${authzHeader}`);

    const body = await cas.doRequest(`https://localhost:8443/cas/oidc/oidcPushAuthorize?${params}`, "POST", {
        "Content-Type": "application/x-www-form-urlencoded",
        "User-Agent": "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36",
        "Authorization": authzHeader
    }, 201);
    const result = JSON.parse(body);
    await cas.log(result);
    const requestUri = result.request_uri;
    assert(requestUri !== undefined);

    url = `${authzUrl}?client_id=client&request_uri=${requestUri}`;
    await cas.log(`Going to ${url}`);
    response = await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.log(`Status: ${response.status()} ${response.statusText()}`);

    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.click(page, "#allow");
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    const content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    await cas.log(payload);
    assert(payload.form.code !== undefined);
    assert(payload.form.nonce !== undefined);
    
    await cas.log(`Attempting to use request_uri ${requestUri}`);
    url = `${authzUrl}?client_id=client&request_uri=${requestUri}`;
    await cas.log(`Going to ${url}`);
    response = await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.log(`Status: ${response.status()} ${response.statusText()}`);
    assert(response.status() === 403);
    
    await cas.closeBrowser(browser);
})();

