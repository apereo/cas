const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.goto(page, "https://localhost:8443/cas/logout");

    await cas.doGet("https://localhost:8443/cas/actuator/caches",
        res => {
            assert(res.data.cacheManagers.ehCacheJCacheCacheManager.caches.proxyGrantingTicketsCache !== null);
            assert(res.data.cacheManagers.ehCacheJCacheCacheManager.caches.proxyTicketsCache !== null);
            assert(res.data.cacheManagers.ehCacheJCacheCacheManager.caches.ticketGrantingTicketsCache !== null);
            assert(res.data.cacheManagers.ehCacheJCacheCacheManager.caches.serviceTicketsCache !== null);
            assert(res.data.cacheManagers.ehCacheJCacheCacheManager.caches.transientSessionTicketsCache !== null);
        }, error => {
            throw error;
        }, { 'Content-Type': "application/json" })

    await cas.doGet("https://localhost:8443/cas/actuator/metrics/cache.puts",
        res => {
            assert(res.data.measurements[0].value === 1);
        }, error => {
            throw error;
        }, { 'Content-Type': "application/json" })
    await cas.doGet("https://localhost:8443/cas/actuator/metrics/cache.removals",
        res => {
            assert(res.data.measurements[0].value === 1);
        }, error => {
            throw error;
        }, { 'Content-Type': "application/json" })
    await cas.doGet("https://localhost:8443/cas/actuator/metrics/cache.gets",
        res => {
            assert(res.data.measurements[0].value >= 3);
        }, error => {
            throw error;
        }, { 'Content-Type': "application/json" })
    await browser.close();
})();
