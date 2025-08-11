const assert = require("assert");
const cas = require("../../cas.js");
const performance = require("perf_hooks").performance;
const fs = require("fs");
const pino = require("pino");
const path = require("path");

const logFilePath = path.join(__dirname, "report.json.ignore");
if (fs.existsSync(logFilePath)) {
    fs.unlinkSync(logFilePath);
}

const logger = pino({
    base: null,
    timestamp: false
}, pino.destination(logFilePath));

const refreshTokenDurations = [];
const idTokenDurations = [];

const fetchRefreshToken = async () =>
    new Promise((resolve) =>
        setImmediate(async () => {
            const params = "grant_type=client_credentials&scope=openid";
            const url = `https://localhost:8443/cas/oidc/token?${params}`;
            const start = performance.now();
            return cas.doPost(url, "", {
                "Content-Type": "application/json",
                "Authorization": `Basic ${btoa("client:secret")}`
            }, async (res) => {
                assert(res.data.access_token === undefined);
                assert(res.data.token_type === undefined);
                assert(res.data.id_token !== undefined);
                assert(res.data.refresh_token !== undefined);
                const end = performance.now();
                const duration = (end - start);
                logger.info({
                    op: "REFRESH_TOKEN",
                    refreshToken: res.data.refresh_token,
                    duration: duration.toFixed(2),
                    timestamp: new Date().toISOString()
                });
                refreshTokenDurations.push(duration);
                resolve(res.data.refresh_token);
            }, (error) => {
                throw `Operation failed: ${error}`;
            }, false);
        }));

const fetchIdToken = async (refreshToken) =>
    new Promise((resolve) =>
        setImmediate(async () => {
            const params = `grant_type=refresh_token&scope=openid&refresh_token=${refreshToken}`;
            const url = `https://localhost:8443/cas/oidc/token?${params}`;
            const start = performance.now();
            return cas.doPost(url, "", {
                "Content-Type": "application/json",
                "Authorization": `Basic ${btoa("client:secret")}`
            }, async (res) => {
                assert(res.data.id_token !== undefined);
                assert(res.data.access_token === undefined);
                const end = performance.now();
                const duration = (end - start);
                logger.info({
                    op: "ID_TOKEN",
                    refreshToken: refreshToken,
                    idToken: res.data.id_token,
                    duration: duration.toFixed(2),
                    timestamp: new Date().toISOString()
                });
                idTokenDurations.push(duration);
                resolve(res.data.id_token);
            }, (error) => {
                throw `Operation failed: ${error}`;
            }, false);
        }));

async function printSummary(durations) {
    const minDuration = Math.min(...durations);
    const maxDuration = Math.max(...durations);
    const avgDuration = durations.reduce((acc, cur) => acc + cur, 0) / durations.length;
    await cas.logb(`Min Duration: \t\t${minDuration.toFixed(2)} ms`);
    await cas.logb(`Max Duration: \t\t${maxDuration.toFixed(2)} ms`);
    await cas.logb(`Average Duration: \t${avgDuration.toFixed(2)} ms`);
}

(async () => {
    const totalTokens = 1000;
    const totalThreads = 10;
    
    const { default: pLimit } = await import("p-limit");
    const threadControl = pLimit(totalThreads);
    const refreshTokenPromises = Array.from({length: totalTokens},
        () => threadControl(() => fetchRefreshToken())
    );
    const results = await Promise.all(refreshTokenPromises);

    await cas.separator();
    await printSummary(refreshTokenDurations);
    
    const idTokenPromises = Array.from({length: results.length},
        (_, index) => threadControl(async () => {
            const refreshToken = results[index];
            await fetchIdToken(refreshToken);
        })
    );
    await Promise.all(idTokenPromises).then(async () => {
        await cas.separator();
        await printSummary(idTokenDurations);
    });
})();
