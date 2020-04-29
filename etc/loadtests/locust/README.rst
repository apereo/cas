============
Locust Files
============

--------------
CAS v6 Locust
--------------
These script files were created as a starting point for users to test the various protocols supported by
CAS with the Python Locust load testing framework.  Feel free to use and update to meet your needs.

The Python version used to create these script = Python 3.7.5

These are the example scripts available for your use.

- casLocust.py - Uses and updated Locus version of 0.13.2
- samlLocust.py - Script file for testing SAML protocol.
- bothLocust.py - Script file combining the CAS and SAML protocol files to test against a CAS instance.

To run these scripts enter the following at the root of the locust project:

.. code-block:: bash

    $ locust -f cas5/locustfile.py --host=https://cas.stage.lafayette.edu

Please reference the below sites if needed:

- `Locust <https://locust.io/>`_
- `CAS blog post <https://apereo.github.io/2019/11/26/cas62x-locust-load-testing/>`_
