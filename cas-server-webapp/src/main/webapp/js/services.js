function updateRegisteredServiceOrder(movedService, pos) {
	
	var COLUMN_SERVICE_ID = "td1";
	var COLUMN_SERVICE_EVAL_ORDER = "td8";
	
	var rowId = $(movedService).attr('id');	
	
	var serviceId = $('#' + rowId + ' td.' + COLUMN_SERVICE_ID).attr('id');
	var evalOrder = $('#' + rowId + ' td.ac.' + COLUMN_SERVICE_EVAL_ORDER).html();
	
	var targetRow = $(pos).attr('element');
	var relPosition = $(pos).attr('position');
	
	var targetRowId = $(targetRow).attr('id');		
	var targetRowEvalOrder = $('#' + targetRowId + ' td.ac.' + COLUMN_SERVICE_EVAL_ORDER).html();
	
	switch (relPosition) {
		case fluid.position.BEFORE:
			evalOrder = eval(targetRowEvalOrder) - 1;
			break;
		case fluid.position.AFTER:
			evalOrder = eval(targetRowEvalOrder) + 1;
			break;
	}
	
	var result = false;
	$.ajax({
		type: "POST",
		async: false,
		url: "updateRegisteredServiceEvaluationOrder.html",
		data: { id: serviceId, evaluationOrder: evalOrder },
		success: 
			function(data, textStatus, jqXHR) {
				$('#' + rowId + ' td.ac.' + COLUMN_SERVICE_EVAL_ORDER).html(evalOrder);
				result = true;
			},
		error: 
			function(jqXHR, textStatus, errorThrown) {
				$("#errorsDiv").show();
				console.log(data.error);
			}
	});
	return result;
}

$(document).ready(function () {
	$("#errorsDiv").hide();
	var opts = {
		selectors: {
			movables: "tr"
		},
		listeners: {
			onMove: updateRegisteredServiceOrder,
			onBeginMove: function() {
				$("#errorsDiv").hide();
			},
			afterMove: function() {
				$("#fluid-ariaLabeller-liveRegion").remove();
			},
			onHover: function(item, state) {
				$(item).css('cursor', state ? 'move' : 'auto');
			}
		}
	};
	return fluid.reorderList("#tableWrapper #scrollTable tbody", opts);
});