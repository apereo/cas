const cas = require("../../cas.js");
const assert = require("assert");

async function createVerifiableCredentialTransaction(credentialConfigurationIds) {
    await cas.log(`Creating verifiable credential transaction for ${credentialConfigurationIds}`);
    
    const body = JSON.stringify({
        "principal": "casuser",
        "credentialConfigurationIds": credentialConfigurationIds
    });
    const transaction = JSON.parse(
        await cas.doRequest("https://localhost:8443/cas/oidc/oidcVcCredentialOfferTransactions?scope=openid", "POST",
            {
                "Authorization": `Basic ${btoa("wallet-client:wallet-secret")}`,
                "Content-Length": body.length,
                "Content-Type": "application/json"
            },
            200,
            body)
    );
    assert(transaction.transactionId !== undefined);
    assert(transaction.credentialOfferUri !== undefined);
    return transaction;
}

async function useCredentialOffer(page, wallet, offerRequest) {
    const url = `http://localhost:7001/wallet-api/wallet/${wallet.walletId}/exchange/useOfferRequest?did=${wallet.did}&requireUserInput=false`;
    const authCookie = `${wallet.cookie.name}=${wallet.cookie.value}`;
    console.log(authCookie);
    const response = await cas.doRequest(url, "POST",
        {
            "Content-Length": offerRequest.length,
            "Content-Type": "text/plain",
            "Cookie": authCookie,
            "Authorization": `Bearer ${wallet.cookie.value}`
        },
        200,
        offerRequest);
    await cas.log(response);
    return response;
}

async function startVerifiableCredentialFlowForConfiguration(...configurationId) {
    await cas.logg(`Starting verifiable credential flow for ${configurationId}`);

    const browser = await cas.newBrowser(cas.browserOptions());
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);
    await cas.gotoLogout(page);
    const wallet = await loginToWallet(page);
    await deleteAllCredentialsInWallet(wallet);

    const transaction = await createVerifiableCredentialTransaction(configurationId);
    const credentialOfferUri = new URL(transaction.credentialOfferUri);

    await cas.logg(`Credential offer URI ${credentialOfferUri.toString()}`);
    const offerRequest =
        `openid-credential-offer://?${new URLSearchParams({
            credential_offer_uri: credentialOfferUri.toString()
        })}`;

    await cas.logg(`Credential offer request: ${offerRequest}`);
    const exchange = await useCredentialOffer(page, wallet, offerRequest);
    await cas.log(`Wallet exchange response ${exchange}`);

    const result = JSON.parse(exchange);
    assert(result.length > 0);

    for (const credential of result) {
        assert.equal(credential.pending, false);
        switch (configurationId) {
        case "myorg":
            assert.equal(credential.format, "dc+sd-jwt");
            break;
        case "employee":
            assert.equal(credential.format, "jwt_vc_json-ld");
            break;
        }
        assert.equal(credential.parsedDocument.sub, "casuser");
        assert.ok(credential.document);
        assert.match(credential.document, /^[^.]+\.[^.]+\.[^.]+$/);
        assert.deepEqual(credential.parsedDocument.roles, ["admin", "user"]);
        assert.equal(credential.parsedDocument.student_id, "S12345");
        assert.equal(credential.parsedDocument.family_name, "User");
        assert.equal(credential.parsedDocument.given_name, "CAS");
        assert.equal(credential.parsedDocument.email, "casuser@example.org");

        const [headerPart, payloadPart] = credential.document.split(".");
        const header = JSON.parse(Buffer.from(headerPart, "base64url").toString("utf8"));
        const payload = JSON.parse(Buffer.from(payloadPart, "base64url").toString("utf8"));
        assert.equal(header.alg, "RS256");
        assert.equal(header.client_id, "wallet-client");
        assert.equal(payload.sub, "casuser");
    }
    
    await cas.goto(page, `http://localhost:7104/wallet/${wallet.walletId}`);
    await cas.sleep();
    const href = await cas.attributeValue(page, "main ul li a", "href");
    const credentialId = href.split("/").pop();
    await cas.logg(`Credential ID: ${credentialId}`);

    const url =
        `http://localhost:7001/wallet-api/wallet/${wallet.walletId}` +
        `/credentials/${credentialId}`;
    const response = await cas.doRequest(
        url,
        "GET",
        {
            "Authorization": `Bearer ${wallet.cookie.value}`
        },
        200
    );

    const credential = JSON.parse(response);
    await cas.log(credential);

    await context.close();
    await cas.closeBrowser(browser);
}

async function loginToWallet(page) {
    await cas.goto(page, "http://localhost:7104/login");
    await cas.type(page, "input[type=\"email\"]", "casuser@apereo.org");
    await cas.type(page, "input[type=\"password\"]", "Mellon");
    await cas.click(page, "button[type=\"submit\"]");
    await cas.waitForNavigation(page);
    await cas.sleep();
    await cas.logPage(page);

    await page.locator("::-p-text(View wallet)").click();
    await cas.sleep();
    await cas.logPage(page);
    const walletId = new URL(await page.url()).pathname.split("/").pop();
    await cas.logg(`Wallet ID is ${walletId}`);

    await cas.goto(page, `http://localhost:7104/wallet/${walletId}/settings/dids`);
    await cas.sleep();
    const handle = await page
        .locator("main p::-p-text(did:jwk:)")
        .waitHandle();
    const did = await handle.evaluate(
        (element) => element.textContent?.trim()
    );
    await cas.logg(`DID: ${did}`);
    const authCookie = await cas.assertCookie(page, true, "auth.token");
    return {
        walletId: walletId,
        did: did,
        cookie: authCookie
    };
}

async function deleteAllCredentialsInWallet(wallet) {
    const apiBase = "http://localhost:7001";

    const credentialsResponse = await cas.doRequest(
        `${apiBase}/wallet-api/wallet/${wallet.walletId}/credentials`,
        "GET",
        {
            Authorization: `Bearer ${wallet.cookie.value}`,
            Accept: "application/json"
        },
        200
    );

    const credentials = JSON.parse(credentialsResponse);

    for (const credential of credentials) {
        const credentialId = credential.id;

        await cas.doRequest(
            `${apiBase}/wallet-api/wallet/${wallet.walletId}/credentials/`
            + `${encodeURIComponent(credentialId)}?permanent=true`,
            "DELETE",
            {
                Authorization: `Bearer ${wallet.cookie.value}`,
                Accept: "application/json"
            },
            0
        );
    }
}

(async () => {
    await startVerifiableCredentialFlowForConfiguration("myorg");
    await cas.separator();
    await startVerifiableCredentialFlowForConfiguration("employee");
    await cas.separator();
    await startVerifiableCredentialFlowForConfiguration("myorg", "employee");
})();
