const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');
const path = require("path");
const os = require("os");
const fs = require("fs");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    try {
        let page = await cas.newPage(browser);
        const service = "https://example.com";
        await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
        await page.waitForTimeout(1000);
        await cas.loginWith(page);
        let ticket = await cas.assertTicketParameter(page);
        const body = await cas.doRequest(`https://localhost:8443/cas/validate?service=${service}&ticket=${ticket}`);
        assert(body === "yes\ncasuser\n");

        await page.waitForTimeout(1000);
        const SAMLRequest = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPHNhbWwycDpMb2dvdXRSZXF1ZXN0IERlc3RpbmF0aW9uPSJodHRwczovL2xvY2FsaG9zdDo4NDQzL2Nhcy9pZHAvcHJvZmlsZS9TQU1MMi9QT1NUL1NMTyIgSUQ9IkJURmsiIElzc3VlSW5zdGFudD0iMjAyMy0xMC0wNFQxMTozNDo1MC41MDdaIiBWZXJzaW9uPSIyLjAiIHhtbG5zOnNhbWwycD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnByb3RvY29sIj48c2FtbDI6SXNzdWVyIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj5odHRwOi8vbG9jYWxob3N0Ojk0NDMvc2ltcGxlc2FtbC9tb2R1bGUucGhwL3NhbWwvc3AvbWV0YWRhdGEucGhwL2RlZmF1bHQtc3A8L3NhbWwyOklzc3Vlcj48c2FtbDI6TmFtZUlEIHhtbG5zOnNhbWwyPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXNzZXJ0aW9uIj43NjBiYWM3NS0yY2MwLTQ3YTQtODNiZS05YmY0OTZhYzYwZjU8L3NhbWwyOk5hbWVJRD48L3NhbWwycDpMb2dvdXRSZXF1ZXN0Pg==";
        const sloPage = `
        <html>
        <body onLoad="document.forms[0].submit()">
        <form action="https://localhost:8443/cas/idp/profile/SAML2/POST/SLO" method="post">
            <input type="hidden" name="RelayState" value="36ece039-7a02-42af-90b1-553a3de4f27c"/>
            <input type="hidden" name="SAMLRequest" value="${SAMLRequest}"/>
        </form>
        </body>
        </html>`.trim();
        await cas.log(sloPage);

        const tempDir = os.tmpdir();
        let sloFile = `${tempDir}/saml2slo.html`;
        
        await fs.writeFileSync(sloFile, sloPage);
        await cas.log(`Logout page is written to ${sloFile}`);

        await cas.goto(page, `file://${sloFile}`);
        await page.waitForTimeout(2000);

        await cas.logPage(page);
        await cas.screenshot(page);
        await cas.assertInnerText(page, "div h2", "Logout successful");
        await cas.assertVisibility(page, "#logoutRedirectButton");
        await cas.click(page, "#logoutRedirectButton");
    } finally {
        await cas.removeDirectoryOrFile(path.join(__dirname, '/saml-md'));
        await browser.close();
    }
})();
