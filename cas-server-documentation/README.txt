
cas-server-documentation README.txt

*What this module is:*

This is the Spring Framework documentation-inspired CAS documentation, managed in source control as DocBook.

*How to build the documentation:*

In this directory (parallel to this README.txt) execute

  mvn site

This will build two HTML versions of the documentation (a single full-content-in-one-page HTML document and a "chunked" website with auto-generated navigation) as well as a PDF, all in the "target" directory that will be created or updated as a child of this directory.

(Executing mvn site one directory up, in the parent directory of this directory, will build a larger site including the formal documentation and many other pages.)

*Why is the documentation here in source control?*

All documentation approaches have tradeoffs.  The biggest advantage of managing the formal documentation in source control as a submodule of the CAS project is to naturally version the documentation with and alongside the source code.

*Shouldn't this documentation be available on the Web somewhere so I don't have to build it from source?*

Certainly.  Haven't gotten to that yet.

