<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Login</title>
</head>
<body>
<form class = "form" action ="submitLogin" method="POST">
<table>
	<tr>
		<td>User name </td> 
		<td> <input type = "text" name="userName"> </td>
	</tr>
	<tr>
		<td>Pass </td> 
		<td> <input type = "password" name="pass"> </td>
	</tr>
	<tr>
		<td>  </td>
		<td> <input type = "submit" value = "Login"> <input type = "button" value = "Cancel"> </td>
	</tr>
</table>
</form>
</body>
</html>