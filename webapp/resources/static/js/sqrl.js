$(document).ready(function () {
    setInterval(function() {
        $.ajax({
            type:"get",
            url:'/cas/sqrl/authcheck?nut=' + [[${nut}]],
            datatype:"html",
            statusCode:{
                205: function(){
                    alert("SQRL authentication has succeeded.")
                }
            }
        });
    }, 5000);
});
