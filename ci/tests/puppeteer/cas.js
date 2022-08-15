const assert = require('assert');
const axios = require('axios');
const https = require('https');
const {spawn} = require('child_process');
const waitOn = require('wait-on');
const JwtOps = require('jsonwebtoken');
const colors = require('colors');
const fs = require("fs");
const {ImgurClient} = require('imgur');
const path = require("path");
const mockServer = require('mock-json-server');
const {Buffer} = require('buffer');
const {PuppeteerScreenRecorder} = require('puppeteer-screen-recorder');
const ps = require("ps-node");
const NodeStaticAuth = require("node-static-auth");
const operativeSystemModule = require("os");
const figlet = require("figlet");
const CryptoJS = require("crypto-js");


const BROWSER_OPTIONS = {
    ignoreHTTPSErrors: true,
    headless: process.env.CI === "true" || process.env.HEADLESS === "true",
    devtools: process.env.CI !== "true",
    defaultViewport: null,
    slowMo: process.env.CI === "true" ? 0 : 10,
    args: ['--start-maximized', "--window-size=1920,1080"]
};

exports.browserOptions = () => BROWSER_OPTIONS;
exports.browserOptions = (opt) => ({
    ...BROWSER_OPTIONS,
    ...opt
});

exports.logy = async (text) => {
    console.log(colors.yellow(text));
};

exports.logb = async (text) => {
    console.log(colors.blue(text));
};

exports.logg = async (text) => {
    console.log(colors.green(text));
};

exports.logr = async (text) => {
    console.log(colors.red(text));
};

exports.removeDirectory = async (directory) => {
    console.log(`Removing directory ${colors.green(directory)}`);
    if (fs.existsSync(directory)) {
        await fs.rmSync(directory, {recursive: true});
    }
    console.log(`Removed directory ${colors.green(directory)}`);
    if (fs.existsSync(directory)) {
        await this.logr(`Removed directory still present at: ${directory}`);
    }
};

exports.click = async (page, button) => {
    await page.evaluate((button) => {
        let buttonNode = document.querySelector(button);
        console.log(`Clicking element ${button} with link ${buttonNode.href}`);
        buttonNode.click();
    }, button);
};

exports.asciiart = async (text) => {
    await this.logb(figlet.textSync(text));
};

exports.clickLast = async (page, button) => {
    await page.evaluate((button) => {
        let buttons = document.querySelectorAll(button);
        buttons[buttons.length - 1].click();
    }, button);
};

exports.innerHTML = async (page, selector) => {
    let text = await page.$eval(selector, el => el.innerHTML.trim());
    console.log(`HTML for selector [${selector}] is: [${text}]`);
    return text;
};

exports.innerText = async (page, selector) => {
    let text = await page.$eval(selector, el => el.innerText.trim());
    console.log(`Text for selector [${selector}] is: [${text}]`);
    return text;
};

exports.textContent = async (page, selector) => {
    let element = await page.$(selector);
    let text = await page.evaluate(element => element.textContent.trim(), element);
    console.log(`Text content for selector [${selector}] is: [${text}]`);
    return text;
};

exports.inputValue = async (page, selector) => {
    const element = await page.$(selector);
    const text = await page.evaluate(element => element.value, element);
    console.log(`Input value for selector [${selector}] is: [${text}]`);
    return text;
};

exports.uploadImage = async (imagePath) => {
    let clientId = process.env.IMGUR_CLIENT_ID;
    if (clientId !== null && clientId !== undefined) {
        const client = new ImgurClient({clientId: clientId});
        console.log(`Uploading image ${colors.green(imagePath)}`);
        client.on('uploadProgress', (progress) => console.log(progress));
        const response = await client.upload({
            image: fs.createReadStream(imagePath),
            type: 'stream',
        });
        console.log(`Uploaded image is at ${colors.green(response.data.link)}`);
    }
};

exports.loginWith = async (page, user, password,
                           usernameField = "#username",
                           passwordField = "#password") => {
    console.log(`Logging in with ${user} and ${password}`);
    await page.waitForSelector(usernameField, {visible: true});
    await this.type(page, usernameField, user);

    await page.waitForSelector(passwordField, {visible: true});
    await this.type(page, passwordField, password);

    await page.keyboard.press('Enter');
    await page.waitForNavigation();
};

exports.fetchGoogleAuthenticatorScratchCode = async (user = "casuser") => {
    console.log(`Fetching Scratch codes for ${user}...`);
    const response = await this.doRequest(`https://localhost:8443/cas/actuator/gauthCredentialRepository/${user}`,
        "GET", {
            'Accept': 'application/json'
        });
    return JSON.stringify(JSON.parse(response)[0].scratchCodes[0]);
};
exports.isVisible = async (page, selector) => {
    let element = await page.$(selector);
    let result = (element != null && await element.boundingBox() != null);
    console.log(`Checking element visibility for ${selector} while on page ${page.url()}: ${result}`);
    return result;
};

exports.assertVisibility = async (page, selector) => {
    assert(await this.isVisible(page, selector));
};

exports.assertInvisibility = async (page, selector) => {
    let element = await page.$(selector);
    let result = element == null || await element.boundingBox() == null;
    console.log(`Checking element invisibility for ${selector} while on page ${page.url()}:${result}`);
    assert(result);
};


exports.assertCookie = async (page, present = true, cookieName = "TGC") => {
    const theCookie = (await page.cookies()).filter(value => {
        console.log(`Checking cookie ${value.name}`);
        return value.name === cookieName
    });
    if (present) {
        console.log(`Checking for cookie ${cookieName}`);
        assert(theCookie.length !== 0);
        console.log(`Asserting cookie:\n${colors.green(JSON.stringify(theCookie, undefined, 2))}`);
        return theCookie[0];
    } else {
        assert(theCookie.length === 0);
        console.log(`Cookie ${cookieName} cannot be found`);
    }
};

exports.submitForm = async (page, selector) => {
    console.log(`Submitting form ${selector}`);
    await page.$eval(selector, form => form.submit());
    await page.waitForTimeout(2500)
};

exports.type = async (page, selector, value, obfuscate = false) => {
    let logValue = obfuscate ? `******` : value;
    console.log(`Typing ${logValue} in field ${selector}`);
    await page.$eval(selector, el => el.value = '');
    await page.type(selector, value);
};

exports.newPage = async (browser) => {
    let page = (await browser.pages())[0];
    if (page === undefined) {
        page = await browser.newPage();
    }
    await page.setDefaultNavigationTimeout(0);
    // await page.setRequestInterception(true);
    await page.bringToFront();
    page
        .on('console', message => {
            if (message.type() === "warning") {
                this.logy(`Console ${message.type()}: ${message.text()}`)
            } else {
                this.logg(`Console ${message.type()}: ${message.text()}`)
            }
        })
        .on('pageerror', ({message}) => this.logr(`Console: ${message}`));
    return page;
};

exports.assertParameter = async (page, param) => {
    console.log(`Asserting parameter ${param} in URL: ${page.url()}`);
    let result = new URL(page.url());
    let value = result.searchParams.get(param);
    console.log(`Parameter ${param} with value ${value}`);
    assert(value != null);
    return value;
};

exports.assertMissingParameter = async (page, param) => {
    let result = new URL(page.url());
    assert(result.searchParams.has(param) === false);
};

exports.sleep = async (ms) =>
    new Promise((resolve) => {
        this.logg(`Waiting for ${ms / 1000} second(s)...`);
        setTimeout(resolve, ms);
    });

exports.assertTicketParameter = async (page, found = true) => {
    console.log(`Page URL: ${page.url()}`);
    let result = new URL(page.url());
    if (found) {
        assert(result.searchParams.has("ticket"));
        let ticket = result.searchParams.get("ticket");
        console.log(`Ticket: ${ticket}`);
        assert(ticket != null);
        return ticket;
    }
    assert(result.searchParams.has("ticket") === false);
    return null;
};

exports.doRequest = async (url, method = "GET", headers = {},
                           statusCode = 200,
                           requestBody = undefined,
                           callback = undefined) =>
    new Promise((resolve, reject) => {
        let options = {
            method: method,
            rejectUnauthorized: false,
            headers: headers
        };
        console.log(`Contacting ${colors.green(url)} via ${colors.green(method)}`);
        const handler = (res) => {
            console.log(`Response status code: ${colors.green(res.statusCode)}`);
            // console.log(`Response headers: ${colors.green(res.headers)}`)
            if (statusCode > 0) {
                assert(res.statusCode === statusCode);
            }
            res.setEncoding("utf8");
            const body = [];
            res.on("data", chunk => body.push(chunk));
            res.on("end", () => resolve(body.join("")));
            if (callback !== undefined) {
                callback(res);
            }
        };

        if (requestBody !== undefined) {
            let request = https.request(url, options, res => handler(res)).on("error", reject);
            request.write(requestBody);
        } else {
            https.get(url, options, res => handler(res)).on("error", reject);
        }
    });

exports.doGet = async (url, successHandler, failureHandler, headers = {}, responseType = undefined) => {
    const instance = axios.create({
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });
    let config = {
        headers: headers
    };
    if (responseType !== undefined) {
        config["responseType"] = responseType
    }
    await instance
        .get(url, config)
        .then(res => {
            if (responseType !== "blob" && responseType !== "stream") {
                console.log(res.data);
            }
            successHandler(res);
        })
        .catch(error => {
            failureHandler(error);
        })
};

exports.doPost = async (url, params = "", headers = {}, successHandler, failureHandler) => {
    const instance = axios.create({
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });
    let urlParams = params instanceof URLSearchParams ? params : new URLSearchParams(params);
    console.log(`Posting to URL ${colors.green(url)}`);
    await instance
        .post(url, urlParams, {headers: headers})
        .then(res => {
            console.log(res.data);
            successHandler(res);
        })
        .catch(error => {
            if (error.response !== undefined) {
                this.logr(error.response.data)
            }
            failureHandler(error);
        })
};

exports.waitFor = async (url, successHandler, failureHandler) => {
    let opts = {
        resources: [url],
        delay: 1000,
        interval: 2000,
        timeout: 120000
    };
    await waitOn(opts)
        .then(() => {
            successHandler("good")
        })
        .catch(err => {
            failureHandler(err);
        });
};

exports.runGradle = async (workdir, opts = [], exitFunc) => {
    let gradleCmd = './gradlew';
    if (operativeSystemModule.type() === 'Windows_NT') {
        gradleCmd = 'gradlew.bat';
    }
    const exec = spawn(gradleCmd, opts, {cwd: workdir});
    await this.logg(`Spawned ${gradleCmd} process ID: ${exec.pid}`);
    exec.stdout.on('data', (data) => {
        console.log(data.toString());
    });
    exec.stderr.on('data', (data) => {
        console.error(data.toString());
    });
    exec.on('exit', exitFunc);
    return exec;
};

exports.launchWsFedSp = async (spDir, opts = []) => {
    let args = ['build', 'appStart', '-q', '-x', 'test', '--no-daemon', `-Dsp.sslKeystorePath=${process.env.CAS_KEYSTORE}`];
    args = args.concat(opts);
    await this.logg(`Launching WSFED SP in ${spDir} with ${args}`);
    return this.runGradle(spDir, args, (code) => {
        console.log(`WSFED SP Child process exited with code ${code}`);
    });
};

exports.stopGradleApp = async (gradleDir, deleteDir = true) => {
    let args = ['appStop', '-q', '--no-daemon'];
    await this.logg(`Stopping process in ${gradleDir} with ${args}`);
    return this.runGradle(gradleDir, args, (code) => {
        console.log(`Stopped child process exited with code ${code}`);
        if (deleteDir) {
            this.sleep(3000);
            this.removeDirectory(gradleDir);
        }
    });
};

exports.shutdownCas = async (baseUrl) => {
    await this.logg(`Stopping CAS via shutdown actuator`);
    const response = await this.doRequest(`${baseUrl}/actuator/shutdown`,
        "POST", {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
        });
    return JSON.parse(response)
};

exports.assertInnerTextStartsWith = async (page, selector, value) => {
    const header = await this.innerText(page, selector);
    assert(header.startsWith(value));
};

exports.assertInnerTextContains = async (page, selector, value) => {
    const header = await this.innerText(page, selector);
    assert(header.includes(value));
};

exports.assertInnerTextDoesNotContain = async (page, selector, value) => {
    const header = await this.innerText(page, selector);
    assert(!header.includes(value));
}

exports.assertInnerText = async (page, selector, value) => {
    const header = await this.innerText(page, selector);
    assert(header === value)
};

exports.assertPageTitle = async (page, value) => {
    const title = await page.title();
    console.log(`Page Title: ${title}`);
    assert(title === value)
};

exports.assertPageTitleContains = async (page, value) => {
    const title = await page.title();
    console.log(`Page Title: ${title}`);
    assert(title.includes(value))
};

exports.recordScreen = async (page) => {
    let index = Math.floor(Math.random() * 10000);
    let filePath = path.join(__dirname, `/recording-${index}.mp4`);
    const config = {
        followNewTab: true,
        fps: 60,
        videoFrame: {
            width: 1024,
            height: 768,
        },
        aspectRatio: '4:3',
    };
    const recorder = new PuppeteerScreenRecorder(page, config);
    console.log(`Recording screen to ${filePath}`);
    await recorder.start(filePath);
    return recorder;
};

exports.createJwt = async (payload, key, alg = "RS256", options = {}) => {
    let allOptions = {...{algorithm: alg}, ...options};
    const token = JwtOps.sign(payload, key, allOptions, undefined);
    console.log(`Created JWT:\n${colors.green(token)}\n`);
    return token;
};

exports.decodeJwt = async (token, complete = false) => {
    console.log(`Decoding token ${token}`);
    let decoded = JwtOps.decode(token, {complete: complete});
    if (complete) {
        console.log(`Decoded token header: ${colors.green(decoded.header)}`);
        console.log("Decoded token payload:");
        await this.logg(decoded.payload);
    } else {
        console.log("Decoded token payload:");
        await this.logg(decoded);
    }
    return decoded;
};

exports.fetchDuoSecurityBypassCodes = async (user = "casuser") => {
    console.log(`Fetching Bypass codes from Duo Security for ${user}...`);
    const response = await this.doRequest(`https://localhost:8443/cas/actuator/duoAdmin/bypassCodes?username=${user}`,
        "POST", {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
        });
    return JSON.parse(response)["mfa-duo"];
};

exports.fetchDuoSecurityBypassCode = async (user = "casuser") => await this.fetchDuoSecurityBypassCode(user)[0];

exports.base64Decode = async (data) => {
    let buff = Buffer.from(data, 'base64');
    return buff.toString('ascii');
};

exports.screenshot = async (page) => {
    if (process.env.CI === "true") {
        let index = Math.floor(Math.random() * 90000);
        let filePath = path.join(__dirname, `/screenshot${index}.png`);
        try {
            let url = await page.url();
            console.log(`Page URL when capturing screenshot: ${url}`);
            console.log(`Attempting to take a screenshot and save at ${filePath}`);
            await page.setViewport({width: 1920, height: 1080});
            await page.screenshot({path: filePath, captureBeyondViewport: true, fullPage: true});
            console.log(`Screenshot saved at ${colors.green(filePath)}`);
            await this.uploadImage(filePath);
        } catch (e) {
            console.log(colors.red(`Unable to capture screenshot ${filePath}: ${e}`));
        }
    } else {
        console.log("Capturing screenshots is disabled in non-CI environments");
    }
};

exports.assertTextContent = async (page, selector, value) => {
    await page.waitForSelector(selector, {visible: true});
    let header = await this.textContent(page, selector);
    assert(header === value);
};

exports.assertTextContentStartsWith = async (page, selector, value) => {
    await page.waitForSelector(selector, {visible: true});
    let header = await this.textContent(page, selector);
    assert(header.startsWith(value));
};

exports.mockJsonServer = async (pathMappings, port = 8000) => {
    let app = mockServer(pathMappings, port, "localhost");
    await app.start();
    return app;
};

exports.httpServer = async (root, port = 5432,
                            authEnabled = true,
                            authUser = "restapi",
                            authPassword = "YdCP05HvuhOH^*Z") => {
    const config = {
        nodeStatic: {
            root: root
        },
        server: {
            port: port,
            ssl: {
                enabled: false
            }
        },
        auth: {
            enabled: authEnabled,
            name: authUser,
            pass: authPassword
        },
        logger: {
            use: true,
            filename: 'restapi.log',
            folder: root
        }
    };
    new NodeStaticAuth(config);
};

exports.randomNumber = async (min = 1, max = 100) =>
    Math.floor(Math.random() * (max - min + 1)) + min;

exports.killProcess = async (command, arguments) => {
    ps.lookup({
        command: command,
        arguments: arguments
    }, (err, resultList) => {
        if (err) {
            throw new Error(err);
        }
        resultList.forEach(process => {
            console.log('PID: %s, COMMAND: %s, ARGUMENTS: %s',
                process.pid, process.command, process.arguments);
            if (process) {
                ps.kill(process.pid, err => {
                    if (err) {
                        throw new Error(err);
                    } else {
                        console.log('Process %s has been killed!', process.pid);
                    }
                });
            }
        });
    });
};

exports.sha256 = async(value) => {
    return CryptoJS.SHA256(value)
};

exports.base64Url = async(value) => {
    return CryptoJS.enc.Base64url.stringify(value);
};

exports.goto = async (page, url, retryCount = 5) => {
    let response = null;
    let attempts = 0;
    const timeout = 2000;

    while (response === null && attempts < retryCount) {
        attempts += 1;
        try {
            console.log(`Navigating to: ${colors.green(url)}`);
            response = await page.goto(url);
            assert(await page.evaluate(() => document.title) !== null);
        } catch (err) {
            console.log(colors.red(`#${attempts}: Failed to goto to ${url}.`));
            console.log(colors.red(err.message));
            await this.sleep(timeout);
        }
    }
    if (response != null) {
        console.log(`Response status: ${colors.green(await response.status())}`);
    }
    return response;
};

exports.refreshContext = async(url = "https://localhost:8443/cas") => {
    console.log("Refreshing CAS application context...");
    const response = await this.doRequest(`${url}/actuator/refresh`, "POST");
    console.log(response);
};

exports.loginDuoSecurityBypassCode = async (page, type) => {
    await page.waitForTimeout(12000);
    if (type === "websdk") {
        const frame = await page.waitForSelector("iframe#duo_iframe");
        await this.screenshot(page);
        const rect = await page.evaluate(el => {
            const {x, y, width, height} = el.getBoundingClientRect();
            return {x, y, width, height};
        }, frame);
        let x1 = rect.x + rect.width - 120;
        let y1 = rect.y + rect.height - 160;
        await page.mouse.click(x1, y1);
        await this.screenshot(page);
    } else {
        await this.click(page, "button#passcode");
    }
    let bypassCodes = await this.fetchDuoSecurityBypassCodes();
    console.log(`Duo Security ${type}: Retrieved bypass codes ${bypassCodes}`);
    if (type === "websdk") {
        let bypassCode = String(bypassCodes[0]);
        await page.keyboard.sendCharacter(bypassCode);
        await this.screenshot(page);
        console.log(`Submitting Duo Security bypass code ${bypassCode}`);
        await page.keyboard.down('Enter');
        await page.keyboard.up('Enter');
        await this.screenshot(page);
        console.log(`Waiting for Duo Security to accept bypass code for ${type}...`);
        await page.waitForTimeout(15000);
    } else {
        let i = 0;
        let error = false;
        while (!error && i < bypassCodes.length) {
            let bypassCode = `${String(bypassCodes[i])}`;
            await page.keyboard.sendCharacter(bypassCode);
            await this.screenshot(page);
            console.log(`Submitting Duo Security bypass code ${bypassCode}`);
            await this.type(page, "input[name='passcode']", bypassCode);
            await this.screenshot(page);
            await page.keyboard.press('Enter');
            console.log(`Waiting for Duo Security to accept bypass code...`);
            await page.waitForTimeout(10000);
            let error = await this.isVisible(page, "div.message.error");
            if (error) {
                console.log(`Duo Security is unable to accept bypass code`);
                await this.screenshot(page);
                i++;
            } else {
                console.log(`Duo Security accepted the bypass code ${bypassCode}`);
                return;
            }
        }
    }
};

console.clear();
this.asciiart("Apereo CAS - Puppeteer");
