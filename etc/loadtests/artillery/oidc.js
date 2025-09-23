module.exports = {
  extractCookies: (requestParams, response, context, ee, next) => {
    context.vars.currentPageUrl = response.url;
    const setCookieHeaders = response.headers["set-cookie"];

    if (setCookieHeaders) {
      setCookieHeaders.forEach((cookie) => {
        const match = cookie.match(/^([^=]+)=([^;]+);/);
        if (match) {
          const [_, cookieName, cookieValue] = match;
          context.vars[cookieName] = cookieValue;
        }
      });
    }
    return next();
  },
  extractCode: (requestParams, response, context, ee, next) => {
    const url = new URL(context.vars.redirectLocation);
    context.vars.code = url.searchParams.get('code');
    return next();
  },
};
