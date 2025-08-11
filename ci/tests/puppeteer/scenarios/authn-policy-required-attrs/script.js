
const cas = require("../../cas.js");
const assert = require("assert");
const querystring = require("querystring");

async function authenticateWithRestApi(username, status = 200) {
    const formData = {
        username: username,
        password: "p@ssw0rd"
    };
    const postData = querystring.stringify(formData);
    const body = await cas.doRequest("https://localhost:8443/cas/v1/users",
        "POST",
        {
            "Accept": "application/json",
            "Content-Length": Buffer.byteLength(postData),
            "Content-Type": "application/x-www-form-urlencoded"
        },
        status,
        postData);
    await cas.log(body);
    return JSON.parse(body);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "casweb", "p@ssw0rd");
    await cas.assertCookie(page);
    await cas.gotoLogout(page);

    await cas.gotoLogin(page);
    const response = await cas.loginWith(page, "casrest", "p@ssw0rd");
    await cas.sleep(2000);
    assert(response.status() === 401);
    await cas.screenshot(page);
    await cas.assertCookie(page, false);
    await cas.assertInnerTextStartsWith(page, "#loginErrorsPanel p", "Authentication attempt has failed");
    await cas.closeBrowser(browser);

    const restResult = await authenticateWithRestApi("casweb");
    assert(restResult.authentication.principal.id === "casweb");

    await authenticateWithRestApi("casrest", 401);
})();
