$(document).ready(function(){
    //focus username field
    if ($(":focus").length === 0){
      $("input:visible:enabled:first").focus();
    }

    //flash error box
    $('#msg.errors').animate({ backgroundColor: 'rgb(187,0,0)' }, 30).animate({ backgroundColor: 'rgb(255,238,221)' }, 500);

    //flash success box
    $('#msg.success').animate({ backgroundColor: 'rgb(51,204,0)' }, 30).animate({ backgroundColor: 'rgb(221,255,170)' }, 500);
    
    //flash confirm box
    $('#msg.question').animate({ backgroundColor: 'rgb(51,204,0)' }, 30).animate({ backgroundColor: 'rgb(221,255,170)' }, 500);
    
    /* 
     * Using the JavaScript Debug library, you may issue log messages such as: 
     * debug.log("Welcome to Central Authentication Service");
     */
});
