function updateRegisteredServiceOrder(movedService, pos) {
	var rowId = $(movedService).attr('id');		
	var id = $('#' + rowId + ' td.td1').attr('id');
	var evalOrder = $('#' + rowId + ' td.ac.td6').html();

	var targetRow = $(pos).attr('element');
	var relPosition = $(pos).attr('position');
	
	var targetRowId = $(targetRow).attr('id');		
	var targetRowEvalOrder = $('#' + targetRowId + ' td.ac.td6').html();
	
	switch (relPosition) {
		case fluid.position.BEFORE:
			evalOrder = eval(targetRowEvalOrder) - 1;
			break;
		case fluid.position.AFTER:
			evalOrder = eval(targetRowEvalOrder) + 1;
			break;
	}
	
	var result = true;
	$.ajax({
		type: "GET",
		async: false,
		url: "updateRegisteredServiceEvaluationOrder.html?id=" + id + "&evaluationOrder=" + evalOrder,
		success: function(data){
			if (!data.removed) {
				$("#errorsDiv").show();
				$("#errorsDiv").html(data.error);
				result = false;
			} else
				$('#' + rowId + ' td.ac.td6').html(evalOrder);
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
			onBeginMove: function(item) {
				$("#errorsDiv").hide();
			}
		}
	};
	fluid.defaults("fluid.reorderer.labeller",{strings:{overallTemplate:""}});
	return fluid.reorderList("#tableWrapper #scrollTable tbody", opts);
});