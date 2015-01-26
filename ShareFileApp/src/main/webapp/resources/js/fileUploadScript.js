$(document).ready(function() {

	$(document).on('click', '.avatarUserName', function() {
		var dis = $('.avatarMenu').css("display");
		if (dis == 'none') {
			$('.avatarMenu').css('display', 'block');
		} else {
			$('.avatarMenu').css('display', 'none');
		}
	});

	$('#mfLogOut').click(function() {
		bootbox.confirm("Do you want log out?", function(result) {
			if (result) {
				$.ajax({
					type : 'POST',
					url : 'clearSession',
					success : function(Response) {
						window.location.href = "./";
					}
				});
			}
		});
	});
	
	// get list file of user
	getFileAction();
	
	var options = {
		// Do something before uploading
		beforeSend : function() {
			$("#progressbox").show();
			// clear everything
			$("#progressbar").width('0%');
			$("#message").empty();
			$("#percent").html("0%");
		},
		uploadProgress : function(event, position, total,
				percentComplete) {
			//send an ajax to notify that uploading
			$("#progressbar").width(percentComplete + '%');
			$("#percent").html(percentComplete + '%');

			// change message text and % to red after 50%
			if (percentComplete > 50) {
				$("#message")
						.html(
								"<font color='red'>File Upload is in progress .. </font>");
			}
		},
		success : function() {
			$("#progressbar").width('100%');
			$("#percent").html('100%');
		},
		complete : function(response) {
			$("#message").html("<font color='blue'>Your file has been uploaded!</font>");

			// get list file
			getFileAction();
		},
		error : function() {
			$("#message").html("<font color='red'> ERROR: unable to upload files</font>");
		} 
	};
	
	// button submit click
	$('#submitUp').click(function() {
		var fsize = $('input[type=file]')[0].files[0].size;
        if( fsize > 83886080 ) //do something if file size more than 1 mb (1048576)
        {
            alert("This file's size is " + fsize/1048576 + " MB" + "\n Too big!");
            return false;
        }
        else{
        	$("#UploadForm").ajaxForm(options);
        }
	});
});

function getFileAction() {
	$.ajax({
		type: 'POST',
		url: 'getFile',
		contentType: "application/json",
		dataType: 'json',
		success: function(Response) {
			drawTable(Response);
		}
	});
}

function drawTable(Response) {
	// TT
	oTable = $('#tbFile').dataTable( {
		destroy: true,
		"bLengthChange": false,
		"iDisplayLength": 5,
		"oTableTools": {
            "sRowSelect": "single"
        },
		"bProcessing": true,
		"aaData": Response,// <-- your array of objects
        "aoColumns": [
			{ "sTitle": "File name", "mData": "name" }, // <-- which values to use inside object
			{ "sTitle": "File size", "mData": "size" },
			{ "sTitle": "Date upload", "mData": "date" },
            {  "sTitle": "Action", "sClass": "center", "mRender": function ( data, type, full ) 
            	{
	            	return '<a class="btnDown btn btn-primary" href="download?fileTitle=' + full.title + ' "><span class="glyphicon glyphicon-download-alt" aria-hidden="true"></span></a>' 
            		+ '<button id="delIcon" class="btn btn-danger" href="#" onClick="getIDOfRow(\'' + full.title + '\')">'
            		+ '<span class="glyphicon glyphicon-trash" aria-hidden="true"></span>'
            		+ '</button>'
            		+ '<button id="shareIcon" class="btn btn-success" href="#" onClick="getShareLink(\'' + full.title + '\')">'
            		+ '<span class="glyphicon glyphicon-share" aria-hidden="true"></span>'
            		+ '</button>';
            	} 
            }
        ]
    } );
}

function getIDOfRow(rowID) {
	
	bootbox.confirm("Are you sure?", function(result) {
		if (result) {
			$.ajax({
				type: 'POST',
				url: 'delete',
				data: "fileTitle=" + rowID,
				dataType: 'json',
				success: function(Response) {
					if(Response.error == 2){
						getFileAction();
					}
				}
			});
		}
	});
}

function getShareLink(rowID) {
	//call ajax and get link
	$.ajax({
		type: 'GET',
		url: 'getLink',
		data: "fileTitle=" + rowID,
		dataType: 'json',
		success: function(Response) {
			if(Response.file !== ""){
				var link = "http://104.155.199.62/ShareFileApp/" + "downloadSharedFile?fileTitle=" + Response.file + "&userNameMD5=" + Response.username;
				bootbox.prompt({
					  title: "This is the link to share",
					  value: link,
					  callback: function(result) {
					    if (result === null) {
					      
					    } else {
					    	
					    }
					  }
					});
			}
		}
	});
	
}
