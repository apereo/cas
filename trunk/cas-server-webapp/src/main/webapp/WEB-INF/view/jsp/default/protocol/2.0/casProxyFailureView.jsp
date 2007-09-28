<%@ page session="false" %><cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
	<cas:proxyFailure code='${code}'>
		${description}
	</cas:proxyFailure>
</cas:serviceResponse>