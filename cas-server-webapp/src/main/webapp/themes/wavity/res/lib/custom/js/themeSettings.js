/* --------------------------------------------------------
	Template Settings
-----------------------------------------------------------*/
// IE mode
var isRTL = false;   //Enables right-to-left layout for languages like Hebrew and Arabic.
var isIE8 = false;
var isIE9 = false;
var isIE10 = false;

var settings =  '<div class="modal fade" id="changeSkin" tabindex="-1" role="dialog" aria-hidden="true">' +
        '<div class="modal-dialog modal-lg">' +
        '<div class="modal-content">' +
        '<div class="modal-header">' +
        '<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>' +
        '<h4 class="modal-title">Change Template Skin</h4>' +
        '</div>' +
        '<div class="modal-body">' +
        '<div class="row template-skins">' +
        '<a data-skin="style-red" class="col-sm-2 col-xs-4" href="">' +
        '<img src="res/lib/custom/img/skin-violate.jpg" alt="">' +
        '</a>'+
        '<a data-skin="style-grey" class="col-sm-2 col-xs-4" href="">' +
        '<img src="res/lib/custom/img/grey.jpeg" alt="">' +
        '</a>'+
        '</div>' +
        '</div>' +
        '</div>' +
        '</div>' +
        '</div>';
    $('#ot-main').prepend(settings);

    $('body').on('click', '.template-skins > a', function(e){
        e.preventDefault();
        var skin = $(this).attr('data-skin');
        $('body').attr('id', skin);
        $('#theme_style').attr("href", "res/lib/custom/css/" + skin + (isRTL ? '-rtl' : '') + ".css");
        $('#changeSkin').modal('hide');
    });

	/* EOF */			