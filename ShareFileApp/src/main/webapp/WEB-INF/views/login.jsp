<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" 
           uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Login</title>
<link rel="stylesheet" href="<c:url value ="/resources/css/bootstrap.min.css"/>" type="text/css">
</head>
<body>
<!-- <form class = "form form-horizontal" action ="submitLogin" method="POST">
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
		<td> <input class="btn btn-primary" type = "submit" value = "Login"> 
		<input class="btn btn-default" type = "button" value = "Cancel"> </td>
	</tr>
</table>
</form>-->

<form class="form-horizontal" action ="submitLogin" method="POST" style="width: 400px; padding-top: 100px; margin: auto;">
  <div class="form-group">
    <label for="inputEmail3" class="col-sm-2 control-label">Username</label>
    <div class="col-sm-10">
      <input type="text" class="form-control" id="inputEmail3" placeholder="User name" name="userName">
    </div>
  </div>
  <div class="form-group">
    <label for="inputPassword3" class="col-sm-2 control-label">Password</label>
    <div class="col-sm-10">
      <input type="password" class="form-control" id="inputPassword3" placeholder="Password" name="pass" >
    </div>
  </div>
  <div class="form-group">
    <div class="col-sm-offset-2 col-sm-10">
      <div class="checkbox">
        <label>
          <input type="checkbox"> Remember me
        </label>
      </div>
    </div>
  </div>
  <div class="form-group">
    <div class="col-sm-offset-2 col-sm-10">
      <button type="submit" class="btn btn-default">Sign in</button>
    </div>
  </div>
</form>

</body>
</html>