<%@ page session="false" %><cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
	<cas:authenticationFailure code='${code}'>
		${description}
	</cas:authenticationFailure>
</cas:serviceResponse>