<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" 
           uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE htm>
<html>
<head>
<link rel="stylesheet" href="<c:url value ="/resources/mf.css"/>" type="text/css">
<link rel="stylesheet" href="<c:url value ="/resources/indexStyle.css"/>" type="text/css">
<link rel="stylesheet" href="<c:url value ="/resources/uploadStyle.css"/>" type="text/css">
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.8/jquery.js" ></script>
<script src="http://malsup.github.com/jquery.form.js" ></script>
<script
	src="//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
<link
	href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css"
	rel="stylesheet">

<script src="<c:url value ="/resources/bootstrap-filestyle.min.js"/>" type="text/javascript" >
</script>

<script src="<c:url value ="/resources/fileUploadScript.js"/>" type="text/javascript" >
</script>

</head>
<body class="appBarMinHeight">

	<div id="mfAppBar" class="showDiscount noAvatarSelected">
		<div id="appAvatar">
			<div class="appAvatar"
				style="background-image: url(//www.mediafire.com/images/icons/myfiles/default.png?1951)"></div>
			<span class="avatarUserName" title="n.thanhthai3010">n.thanhthai3010<span
				class="avatarEdit"></span></span> <span class="tooltip point-left alt">n.thanhthai3010</span>
			<div class="avatarMenu popupContainer">
				<ul>
					<li id="userEmail">n.thanhthai3010@gmail.com</li>
					<li><a class="ico30rename maskedIcons settingsLink"
						href="https://www.mediafire.com/myaccount/accountbilling.php">Edit
							username</a></li>
					<li><a class="ico30changeAvatar maskedIcons settingsLink"
						href="https://www.mediafire.com/myaccount/customize_avatar.php"><span>Change
								avatar</span><span id="uploadAvatar">Add an avatar</span></a></li>
					<li class="divider"></li>
					<li><a class="ico30settings maskedIcons settingsLink"
						href="https://www.mediafire.com/myaccount/">Account settings</a></li>
					<li><a class="ico30upgrade maskedIcons upGrade"
						href="https://www.mediafire.com/upgrade/">Upgrade <span>-
								<span>50% OFF</span>
						</span></a></li>
					<li><a class="ico30add maskedIcons"
						href="https://www.mediafire.com/earnspace/">Earn free space!</a></li>
					<li><a id="mfStats" class="ico30chart maskedIcons" href="#">View
							statistics</a></li>
					<li><a class="ico30mobile maskedIcons"
						href="https://www.mediafire.com/software/">Desktop &amp;
							Mobile</a></li>
					<li><a id="mfLogOut" class="ico30logout maskedIcons" href="#">Log
							out</a></li>
					<li class="divider"></li>
				</ul>
				<a href="http://www.mediafire.com/?ssl=off"
					title="Turn Secure Encryption Off" class="appSettingControl">
					SSL <span class="toggle-switch tsSmall on"> <span
						class="ts-slider"> <label class="ts-l">On</label> <span
							class="ts-knob"></span> <label class="ts-r">Off</label>
					</span>
				</span>
				</a>
			</div>
		</div>

		<label class="appGroupLabel">Apps</label>
		<ul id="appList" class="appGroup">
			<li><a class="appBtn appMyFiles appOn" href="#"
				data-app="myfiles"> Files <span class="tooltip point-left alt">Files</span>
			</a></li>
			<li><a class="appBtn appRecentChanges" href="#"
				data-app="recent-changes"> Recent <span
					class="tooltip point-left alt">Recent</span>
			</a></li>
			<li><a class="appBtn appFollowing" href="#"
				data-app="shared-items"> Following <span
					class="tooltip point-left alt">Following</span>
			</a></li>
		</ul>

		<div id="appBarFooter">

			<div class="smSpaceIndicator">
				<div class="smBarGray">
					<div class="smBarColor" style="width: 4%;"></div>
				</div>
				<div id="storageTooltip">
					<span id="storage_applicable" style=""><strong><span
							id="storage_used">1.8 GB</span> / <span id="storage_total">50
								GB</span></strong></span> <span style="display: none;" id="storage_not-applicable">N/A</span>
					<a class="smUpgradeSpace" href="https://www.mediafire.com/upgrade/"><span>Upgrade</span><em>50%
							OFF</em></a>
				</div>
			</div>
			<div id="helpfooter">
				<div class="appHelp gbtnAlt ico30" id="toggleHelpFooter">
					<span>Help</span>
				</div>
				<div class="appInfo gbtnAlt ico30" id="toggleAboutFooter">
					<span>About</span>
				</div>
			</div>
		</div>
	</div>
	<!-- Thah thai -->
	<div id="mfHeader" style="width: 1145px;">
		<div id="logo">
			<h2>Group SE06</h2>
		</div>

		<!-- Upload area -->
		<div id="uploadArea">
			<form id="UploadForm" action="UploadFile" method="post" enctype="multipart/form-data">
				<div id="btnUpload" class="bootstrap-filestyle input-group">
					<input id="myfile" name="myfile" class="filestyle" type="file"
						data-input="true"
						style="position: absolute; clip: rect(0px, 0px, 0px, 0px);"
						tabindex="-1" data-buttonText="Select File"
						data-buttonName="btn-primary"
						data-iconName="glyphicon glyphicon-open">

				</div>
				
				<div id="progressbox">
					<div id="progressbar"></div>
					<div id="percent">0%</div>
				</div>
				<div id="btnSubmit" >
					<button class="btn btn-success" id="submitUp">Upload</button>
				</div>
			</form>
		
			<!-- Search -->
			<div id="searchField">
				<form class="navbar-form" role="search" method="get"
					id="search-form" name="search-form">
					<div class="input-group">
						<input type="text" class="form-control" placeholder="Search..."
							id="query" name="query" value="">
						<div class="input-group-btn">
							<button type="submit" class="btn btn-success">
								<span class="glyphicon glyphicon-search"></span>
							</button>
						</div>
					</div>
				</form>
			</div>
		</div>

		<div id="message">
		</div>
	</div>


	<!-- div main -->
	<div id="mfMain">
		<table id="tbFile" class="table table-striped table-bordered">
			<tr>
				<th>ID</th>
				<th>File Name</th>
			</tr>
		</table>
	</div>

</body>
</html>