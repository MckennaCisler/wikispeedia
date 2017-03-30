$(document).ready(function() {
	$("#advanced_ops").hide();
	$("#advanced").on('click', function() {
		if ($("#advanced_ops").is(":hidden")) {
			$("#advanced_ops").show();
			$("#advanced_im").attr("src", "down-triangle.png");
		} else {
			$("#advanced_ops").hide();
			$("#advanced_im").attr("src", "side-triangle.png");
		}
	});
});