module.exports = {
    extractServiceTicket: (context, ee, next) => {
        const url = new URL(context.vars.redirectLocation); 
        context.vars.serviceTicket = url.searchParams.get('ticket');
        console.log(`Extracted ticket: ${context.vars.serviceTicket}`);
        return next();
    },
    logServiceResponse: (context, ee, next) => {
        console.dir(context.vars.serviceResponse, {depth: null, colors: true});
        return next();
    }
};
