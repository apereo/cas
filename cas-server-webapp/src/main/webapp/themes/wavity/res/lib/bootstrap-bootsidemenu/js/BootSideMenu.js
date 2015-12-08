(function ( $ ) {

	$.fn.BootSideMenu = function( options ) {

		var oldCode, newCode, side;

		newCode = "";

		var settings = $.extend({
			side:"left",
			autoClose:true
		}, options );

		side = settings.side;
		autoClose = settings.autoClose;

		this.addClass("dccontainer sidebar");

		if(side=="left"){
			this.addClass("sidebar-left");
		}else if(side=="right"){
			this.addClass("sidebar-right");
		}else{
			this.addClass("sidebar-left");	
		}

		oldCode = this.html();

		newCode += "<div class=\"row\">\n";
		newCode += "	<div class=\"col-xs-12 col-sm-12 col-md-12 col-lg-12\" data-side=\""+side+"\">\n"+ oldCode+" </div>\n";
		newCode += "</div>";
		newCode += "<div class=\"toggler\" id=\"menuHide\">\n";
		newCode += "	<span class=\"fa fa-chevron-right\">&nbsp;</span> <span class=\"fa fa-chevron-left\">&nbsp;</span>\n";
		newCode += "</div>\n";

		this.html(newCode);

		if(autoClose){
			$(this).find(".toggler").trigger("click");
		}

	};

	$(document).on('click','.toggler', function(){
		var toggler = $(this);
		var dccontainer = toggler.parent();
		var listaClassi = dccontainer[0].classList;
		var side = getSide(listaClassi);
		var dccontainerWidth = dccontainer.width();
		var status = dccontainer.attr('data-status');
		if(!status){
			status = "opened";
		}
		doAnimation(dccontainer, dccontainerWidth, side, status);
	});

function getSide(listaClassi){
	var side;
	for(var i = 0; i<listaClassi.length; i++){
		if(listaClassi[i]=='sidebar-right'){
			side = "right";
			break;
		}else{
			side = null;
		}
	}
	return side;
}

function doAnimation(dccontainer, dccontainerWidth, sidebarSide, sidebarStatus){
	var toggler = dccontainer.children()[1];
	if(sidebarStatus=="opened"){
		$("#rightControls>ul.list-unstyled li").draggable({ helper: "clone",stack: "div",cursor: "move", cancel: null});
		if(sidebarSide=="right"){
			dccontainer.animate({
				right:- (dccontainerWidth +2)
			});
			toggleArrow(toggler, "right");
		}
		dccontainer.attr('data-status', 'closed');
	}else{
		$("#rightControls>ul.list-unstyled li").draggable({ helper: "clone",stack: "div",cursor: "move", cancel: null});
		if(sidebarSide=="right"){
			dccontainer.animate({
				right:0
			});
			toggleArrow(toggler, "left");
		}
		dccontainer.attr('data-status', 'opened');

	}

}

function toggleArrow(toggler, side){
	if(side=="left"){
        $(toggler).children(".fa-chevron-right").css('display', 'block');
        $(toggler).children(".fa-chevron-left").css('display', 'none');
    }else if(side=="right"){
        $(toggler).children(".fa-chevron-left").css('display', 'block');
        $(toggler).children(".fa-chevron-right").css('display', 'none');
    }
}

}( jQuery ));

