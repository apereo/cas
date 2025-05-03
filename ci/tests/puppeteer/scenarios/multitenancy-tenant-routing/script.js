const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions({ args: ["--proxy-server=http://127.0.0.1:8888"] }));
    const page = await cas.newPage(browser);
    await cas.sleep(1000);
    const casServer = "http://localhost:8888/cas";
    const service = "https://localhost:9859/anything/cas";
    await cas.goto(page, `${casServer}/login`);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);
    const cookie = await cas.assertCookie(page);
    assert(cookie.path === "/cas");
    await cas.goto(page, `${casServer}/login?service=${service}`);
    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`${casServer}/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    const json = JSON.parse(body);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.attributes["name"][0] === "CAS");
    assert(authenticationSuccess.attributes["username"][0] === "casuser");
    assert(authenticationSuccess.attributes["department"][0] === "SSO");
    await browser.close();
})();
