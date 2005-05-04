<%@ page session="false" %><?xml version="1.0" encoding="UTF-8"?>
<cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas">
	<cas:proxyFailure code="${code}">
		${description}
	</cas:proxyFailure>
</cas:serviceResponse>