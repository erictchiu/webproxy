<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
<head>
	<title>ProxyServer Test Center</title>
</head>

<body style="font-family:verdana;font-size:11px">
We need to prep the cookies as if .wellsfargo.com. <br/>
Simply posting the information below will allow you to control<br/>
the session to test code.

<form action="/webproxy/add">


Set your <strong>Cookies</strong> then come back and make a request to the ProxyServlet. <br/> Cookies will live for 10 mimnutes.<br/>

<table style="font-family:verdana;font-size:11px">
<tr>
	<td><input type="checkbox" name="wwwstate_box" value="yes"></td>
	<td><strong>WWWSTATE</strong>: </td>
	<td><input type="text" name="WWWSTATE" value="CA" size="50"><br/></td>
	<td><a href="/webproxy/add?delete=WWWSTATE"><strong>DELETE</strong></a></td>
</tr>
<tr>
    <td><input type="checkbox" name="wfacookie_box" value="yes"></td>
	<td><strong>wfacookie</strong>: </td>
	<td><input type="text" name="wfacookie" size="50"value="B-200808011600444289797766"><br/></td>
<td><a href="/webproxy/add?delete=wfacookie"><strong>DELETE</strong></a></td>
</tr>
<tr>
    <td><input type="checkbox" name="SIMSCookie_box" value="yes"></td>
	<td><strong>SIMSCookie</strong>: </td>
	<td><input type="text" size="50" name="SIMSCookie" value="613806_1ec6e91847142e34bacada3a1d0e87b0"><br/></td>
<td><a href="/webproxy/add?delete=SIMSCookie"><strong>DELETE</strong></a></td>
</tr>
<tr>
    <td><input type="checkbox" name="hp_box" value="yes"></td>
	<td><strong>hp</strong>: </td>
	<td><input type="text" name="hp" size="50"value="12000000000000|RID=REGBKGSF"><br/></td>
	<td><a href="/webproxy/add?delete=hp"><strong>DELETE</strong></a></td>
</tr>
<tr>
    <td><input type="checkbox" name="TCID_box" value="yes"></td>
	<td><strong>TCID</strong>: </td>
	<td><input type="text" size="50" name="TCID" value="0007a3b4-fbe5-715c-ba50-b77c0000003c;"><br/></td>
<td><a href="/webproxy/add?delete=TCID"><strong>DELETE</strong></a></td>
</tr>
<tr>
    <td><input type="checkbox" name="nSC_XfmmtGbshp_box" value="yes"></td>
	<td><strong>NSC_XfmmtGbshp</strong>: </td>
	<td><input type="text" size="50" name="NSC_XfmmtGbshp" value="445b326f7852"><br/><br/></td>
<td><a href="/webproxy/add?delete=NSC_XfmmtGbshp"><strong>DELETE</strong></a></td>
</tr>
<tr>
    <td></td>
	<td><input type="submit" value="SUBMIT"><br/></td>
	<td></td>
</tr>
</table>
</form>
<br/>
<form  method="post" action="/webproxy/AdServlet">
<table style="font-family:verdana;font-size:11px">
<tr>
	<td colspan="3"><br/><br/>Choose your <strong>request type</strong>:<br/></td>
</tr>
<tr>
    <td></td>
	<td></td>
	<td></td>
</tr>
<tr>
    <td><input type="checkbox" name="requestType_box" value="yes"></td>
	<td><strong>RequestType</strong>: </td>
	<td>c-request:&nbsp;&nbsp;&nbsp;&nbsp;<input type="radio" name="requestType" value="c"><br/>
		&nbsp;&nbsp;i-request:&nbsp; <input type="radio" name="requestType" value="i"><br/></td>
</tr>
<tr>
	<td colspan="3"><br/><br/>Choose your the parameters and value you want to send to Omniture.<br/></td>
</tr>
<tr>
    <td></td>
	<td><br/><br/><strong>Paramaters</strong>:<br/></td>
	<td></td>
</tr>
<tr>
    <td><input type="checkbox" name="siteID_box" value="yes"></td>
	<td><strong>siteID</strong>: </td>
	<td><input type="text" size="50" name="siteID" value="487"><br/></td>
</tr>
<tr>
    <td><input type="checkbox" name="ccID_box" value="yes"></td>
	<td><strong>ccID</strong>: </td>
	<td><input type="text" size="50" name="ccID" value="WF_CON_HP_PRIMARY_BNR"><br/></td>
</tr>
<tr>
    <td><input type="checkbox" name="ccID_box" value="yes"></td>
	<td><strong>ccID</strong>: </td>
	<td><input type="text" size="50" name="ccID" value="WF_CON_HP_SECONDARY_A_BNR"><br/></td>
</tr>
<tr>
    <td><input type="checkbox" name="ccID_box" value="yes"></td>
	<td><strong>ccID</strong>: </td>
	<td><input type="text" size="50" name="ccID" value="WF_CON_HP_SECONDARY_C_BNR"><br/></td>
</tr>
<tr>
    <td><input type="checkbox" name="ccID_box" value="yes"></td>
	<td><strong>ccID</strong>: </td>
	<td><input type="text" size="50" name="ccID" value="WF_CON_PROD_SECONDARY_TOP"><br/></td>
</tr>
<tr>
    <td><input type="checkbox" name="location_box" value="yes"></td>
	<td><strong>location</strong>: </td>
	<td><input type="text" size="100" name="location" value="https%3a%2f%2fwww.wellsfargo.com%2findex.jsp%3FparamName1%3DparamValue1%26paramName2%3DparamValue2"><br/></td>
</tr>
<tr>
    <td></td>
	<td><input type="submit" value="SUBMIT"><br/></td>
	<td></td>
</tr>
</table>




</form>

<br/>
<br/>


</body></html>
