$(document).ready(function() {
	
	$.ajax({
		type: 'POST',
		url: 'getFile',
		contentType: "application/json",
		dataType: 'json',
		success: function(Response) {
			// Delete all row first
			$('#tbFile tr').slice(1).remove();
			// ReDraw table.
			drawTable(Response);
		}
	});
	
var options = {
	beforeSend : function() {
		$("#progressbox").show();
		// clear everything
		$("#progressbar").width('0%');
		$("#message").empty();
		$("#percent").html("0%");
	},
	uploadProgress : function(event, position, total, percentComplete) {
		$("#progressbar").width(percentComplete + '%');
		$("#percent").html(percentComplete + '%');

		// change message text and % to red after 50%
		if (percentComplete > 50) {
			$("#message").html("<font color='red'>File Upload is in progress .. </font>");
		}
	},
	success : function() {
		$("#progressbar").width('100%');
		$("#percent").html('100%');
	},
	complete : function(response) {
		$("#message").html("<font color='blue'>Your file has been uploaded!</font>");
		
		$.ajax({
			type: 'POST',
			url: 'getFile',
			contentType: "application/json",
			dataType: 'json',
			success: function(Response) {
				// Delete all row first
				$('#tbFile tr').slice(1).remove();
				// ReDraw table.
				drawTable(Response);
			}
		});
	},
	error : function() {
		$("#message").html("<font color='red'> ERROR: unable to upload files</font>");
	}
};
$("#UploadForm").ajaxForm(options);
});

function drawTable(data) {
    for (var i = 0; i < data.length; i++) {
        drawRow(data[i]);
    }
}

function drawRow(rowData) {
    var row = $("<tr />")
    $("#tbFile").append(row); //this will append tr element to table... keep its reference for a while since we will add cels into it
    row.append($("<td>" + rowData.id + "</td>"));
    row.append($("<td><a href=\"download?fileName=" + rowData.name +
    		"\" >" + rowData.name + "</a></td>"));
}

