const cas = require("../../cas.js");
const assert = require("assert");

async function authenticate(page, casServer) {
    await cas.sleep(1000);
    const service = "https://localhost:9859/anything/cas";
    await cas.goto(page, `${casServer}/login`);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);
    const cookie = await cas.assertCookie(page);
    assert(cookie.path === "/cas");
    assert(cookie.domain === new URL(casServer).hostname);
    await cas.goto(page, `${casServer}/login?service=${service}`);
    await cas.sleep(1000);
    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`${casServer}/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    const json = JSON.parse(body);
    return json.serviceResponse.authenticationSuccess;
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions({ options: [
        "--host-resolver-rules=MAP shire.localhost 127.0.0.1, MAP london.localhost 127.0.0.1"
    ] }));
    const page = await cas.newPage(browser);
    await cas.sleep(1000);
    let payload = await authenticate(page, "http://shire.localhost:8888/cas");
    assert(payload.user === "casuser");
    assert(payload.attributes["name"][0] === "CAS");
    assert(payload.attributes["username"][0] === "casuser");
    assert(payload.attributes["department"][0] === "SSO");
    await cas.separator();
    payload = await authenticate(page, "http://london.localhost:8889/cas");
    assert(payload.user === "casuser");
    assert(payload.attributes["city"][0] === "London");
    assert(payload.attributes["username"][0] === "casuser");
    assert(payload.attributes["country"][0] === "UK");
    await browser.close();
})();
