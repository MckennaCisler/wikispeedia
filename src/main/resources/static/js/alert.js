/* jshint esversion: 6 */
let errNum = 0;
function my_alert(alertText) {
	"use strict";
	my_alert_cb(alertText, () => {});
}
function my_alert_cb(alertText, callback) {
	"use strict";
	$("body").append(`<div class="modal fade alert-msg" id="m${errNum}" data-animation="false">
	  <div class="modal-dialog" role="document">
		<div class="modal-content">
		  <div class="modal-header">
			<h5 class="modal-title">Uh oh, something went wrong</h5>
			<button type="button" class="close" data-dismiss="modal" aria-label="Close">
			  <span aria-hidden="true">&times;</span>
			</button>
		  </div>
		  <div class="modal-body">
			<p>${alertText}</p>
		  </div>
		</div>
	  </div>
	</div>`);
	$(`#m${errNum} .close`).click(callback);
	$(`#m${errNum}`).modal();
	errNum++;
}

function clear_my_alert() {
	// $(`.alert-msg`).modal('hide');
	$(`.alert-msg .close`).click();
}
