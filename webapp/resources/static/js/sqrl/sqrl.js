function sqrlInProgress(sqrlurl) {
    var sqrlImgSrc = $("#sqrlImg").attr("src");
    var showingSqrlQr = sqrlImgSrc != "/cas/images/spinner.gif";
    if (!showingSqrlQr) {
        return;
    }
    $("#cancelSqrl").hide();

    $("#sqrlImg").attr("src", "/cas/images/spinner.gif");
    $("#cancelSqrl").show();
    $("#cancelSqrl").css("visibility", "");
    $("#fm1").hide();
    
    window.location.replace(sqrlurl);
}

$(document).ready(function () {
    $("#cancelSqrl").hide();

});
