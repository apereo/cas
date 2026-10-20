
const cas = require("../../cas.js");
const http = require("http");
const httpCasClient = require("http-cas-client");
const assert = require("assert");

(async () => {
    let failed = false;
    try {
        const handler = httpCasClient({
            cas: 2,
            casServerUrlPrefix: "https://localhost:8443/cas",
            serverName: "http://localhost:8080"
        });
        await cas.log("Creating HTTP server for CAS client on port 8080");
        const server = await http.createServer(async (req, res) => {
            if (!await handler(req, res)) {
                return res.end();
            }
            const {principal} = req;
            if (principal !== undefined) {
                await cas.log(`Principal: ${principal}`);
                assert(principal.user === "casuser");
            }
            res.end();
        }).listen(8080);

        await server.on("listening", () => server.closeAllConnections());

        const browser = await cas.newBrowser(cas.browserOptions());
        const page = await cas.newPage(browser);
        const captureOrigin = "http://127.0.0.1:8081";
        const captureRequests = [];
        await page.setRequestInterception(true);
        page.on("request", async (request) => {
            if (new URL(request.url()).origin === captureOrigin) {
                captureRequests.push(request.url());
                await request.respond({status: 200, body: "Unexpected service redirect"});
            } else {
                await request.continue();
            }
        });
        const verifyUnsafeServicesRejected = async () => {
            for (const userInfo of ["localhost%23", "localhost"]) {
                const service = `http://${userInfo}@127.0.0.1:8081/capture`;
                const response = await cas.gotoLogin(page, encodeURIComponent(service));
                assert(response !== null);
                assert.strictEqual(response.status(), 403);
                assert.strictEqual(new URL(page.url()).origin, "https://localhost:8443");
                assert.strictEqual(response.headers().location, undefined);
                assert.deepStrictEqual(captureRequests, []);
            }
        };
        await verifyUnsafeServicesRejected();
        const response = await cas.goto(page, "http://localhost:8080");
        assert(response !== null && response.ok());
        await cas.loginWith(page);
        await verifyUnsafeServicesRejected();

        await cas.closeBrowser(browser);
    } catch (e) {
        failed = true;
        throw e;
    } finally {
        if (!failed) {
            await process.exit(0);
        }
    }
})();
