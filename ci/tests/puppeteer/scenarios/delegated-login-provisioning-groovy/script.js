
const cas = require("../../cas.js");
const fs = require("fs");
const os = require("os");
const assert = require("assert");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.click(page, "li #CasClient");
    await cas.waitForNavigation(page);
    await cas.loginWith(page);
    await cas.sleep(6000);
    await cas.assertTicketParameter(page);

    const result = path.join(os.tmpdir(), "profile.txt");
    await cas.log(`Target file should be at ${result}`);
    assert(fs.existsSync(result) === true);
    
    await cas.closeBrowser(browser);
})();
