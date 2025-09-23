const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.gotoLogout(page);
    await cas.goto(page, "http://localhost:16686/search?end=1726227064882000&limit=1&lookback=5m&maxDuration&minDuration&operation=http%20get&service=cas&start=1726226764882000");
    await cas.sleep(2000);
    await cas.click(page, "button.SearchForm--submit");
    await cas.sleep(2000);
    const count = await cas.innerText(page, ".SearchResults--headerOverview h2");
    assert(count === "1 Trace");
    await cas.closeBrowser(browser);
})();
