
const cas = require("../../cas.js");
const assert = require("assert");
const querystring = require("querystring");

async function loginAndVerify(browser) {
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.click(page, "#rememberMeButton");
    await cas.loginWith(page);
    await cas.sleep(1000);
    let tgc = await cas.assertCookie(page);
    let date = new Date(tgc.expires * 1000);
    await cas.logg(`TGC expiration date: ${date}`);

    let now = new Date();
    await cas.logg(`Current date: ${now}`);
    now.setDate(now.getDate() + 1);
    assert(now.getDate() === date.getDate());
    
    const page2 = await cas.newPage(browser);
    await cas.gotoLogin(page2);
    tgc = await cas.assertCookie(page2);
    date = new Date(tgc.expires * 1000);
    await cas.logg(`TGC expiration date: ${date}`);

    now = new Date();
    await cas.logg(`Current date: ${now}`);
    now.setDate(now.getDate() + 1);
    assert(now.getDate() === date.getDate());
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
}

async function executeRequest(url, method, statusCode, contentType = "application/x-www-form-urlencoded", requestBody = undefined) {
    return cas.doRequest(url, method,
        {
            "Accept": "application/json",
            "Content-Length": requestBody === undefined ? 0 : Buffer.byteLength(requestBody),
            "Content-Type": contentType
        },
        statusCode, requestBody);
}

async function fetchSsoSessions() {
    await cas.logg("Removing all SSO Sessions");
    await cas.doDelete("https://localhost:8443/cas/actuator/ssoSessions?type=ALL&from=1&count=100000");
    
    const formData = {
        username: "casuser",
        password: "Mellon",
        rememberMe: true
    };
    const postData = querystring.stringify(formData);
    for (let i = 0; i < 100; i++) {
        const tgt = await executeRequest("https://localhost:8443/cas/v1/tickets", "POST", 201, "application/x-www-form-urlencoded", postData);
        assert(tgt !== undefined);
    }
    await cas.doDelete("https://localhost:8443/cas/actuator/ticketRegistry/clean", 200,
        async (res) => assert(res.status === 200), async (err) => {
            throw err;
        }, {
            "Accept": "application/json",
            "Content-Type": "application/x-www-form-urlencoded"
        });
    
    await cas.doGet("https://localhost:8443/cas/actuator/ssoSessions?type=ALL", async (res) => assert(res.status === 200), (err) => {
        throw err;
    });
}

async function verifyWithoutRememberMe() {
    let browser = await cas.newBrowser(cas.browserOptions());
    let page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);
    const tgc = await cas.assertCookie(page);
    const date = new Date(tgc.expires * 1000);
    await cas.logg(`TGC expiration date: ${date}`);
    const now = new Date();
    await cas.logg(`Current date: ${now}`);
    now.setDate(now.getDate() + 1);
    assert(now.getDate() !== date.getDate());

    await browser.close();
    browser = await cas.newBrowser(cas.browserOptions());
    page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertCookie(page, false);
    await browser.close();
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    await loginAndVerify(browser);
    await cas.refreshContext();
    await loginAndVerify(browser);
    await browser.close();
    await fetchSsoSessions();
    await verifyWithoutRememberMe();
})();
