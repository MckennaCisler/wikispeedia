/* jshint esversion: 6 */
let errNum = 0;
function my_alert(alertText) {
	"use strict";
	$("body").append(`<div class="modal fade" id="m${errNum}">
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
	$(`#m${errNum}`).modal();
	errNum++;
}