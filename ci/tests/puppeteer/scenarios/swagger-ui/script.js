
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        let response = await cas.goto(page, "https://localhost:8443/cas/v3/api-docs");
        assert(response.ok());
        const api = await response.json();
        assert(api.openapi.startsWith("3."));
        assert.strictEqual(api.info.title, "Apereo CAS Swagger API Documentation");
        assert(Object.keys(api.paths).length > 0);
        assert(Object.keys(api.components.schemas).length > 0);

        const yamlDocument = await page.evaluate(async () => {
            const answer = await fetch("https://localhost:8443/cas/v3/api-docs.yaml");
            return {ok: answer.ok, text: await answer.text()};
        });
        assert(yamlDocument.ok);
        const yaml = await cas.parseYAML(yamlDocument.text);
        assert.strictEqual(yaml.openapi, api.openapi);
        assert.deepStrictEqual(yaml.paths, api.paths);
        assert.deepStrictEqual(yaml.components.schemas, api.components.schemas);

        response = await cas.goto(page, "https://localhost:8443/cas/swagger-ui/index.html");
        assert(response.ok());
        await cas.waitForElement(page, ".swagger-ui .opblock");
        await cas.assertInnerTextContains(page, ".swagger-ui .info .title", api.info.title);
    } finally {
        await cas.closeBrowser(browser);
    }
})();
