const cas = require("../../cas.js");
const fs = require("fs");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogout(page);
    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);

    const template = path.join(__dirname, "attribute-repository.json");
    const original = fs.readFileSync(template, "utf8");
    try {
        const repository = JSON.parse(original);
        repository["casuser"]["expiration"] = [Math.floor(Date.now() / 1000) + 2];
        fs.writeFileSync(path.join(__dirname, "/attribute-repository.json"), JSON.stringify(repository, undefined, 2));
        await cas.sleep(3000);
        await cas.gotoLogin(page, service);
        await cas.assertCookie(page, false);
    } finally {
        fs.writeFileSync(path.join(__dirname, "/attribute-repository.json"), original);
        await cas.closeBrowser(browser);
    }

})();
