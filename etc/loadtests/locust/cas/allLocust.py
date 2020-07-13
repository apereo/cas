from __future__ import print_function
import csv
import os
import random
import re
import urllib3
from urllib.parse import parse_qs, urlparse, unquote, urlencode, quote
from locust import HttpLocust, TaskSet, task, TaskSequence, seq_task, between


# SAML IdP Host
HOST = "https://axman000.local:8443"

# Run weighting for each protocol
CAS_WEIGHT = 3
SAML_WEIGHT = 1

# Testing using locally generated cert, so turning off error messaging
IGNORE_SSL = True

# Search Patterns
EXECUTION_PAT = re.compile(r'<input type="hidden" name="execution" value="([^"]+)"')
EVENTID_PAT = re.compile(r'<input type="hidden" name="_eventId" value="([^"]+)"')
RELAY_STATE_PAT = re.compile(r'<input type="hidden" name="RelayState" value="([^"]+)"')
SAML_RESPONSE_PAT = re.compile(r'<input type="hidden" name="SAMLResponse" value="([^"]+)"')
SAML_SP_PAGE_PAT = re.compile(".*PHP Variables.*")

# Service Provider settings
CAS_SP = "https://castest.edu/"

# SAML Service Provider settings
SP = 'https://idptestbed'
SP_LOGIN = '/Shibboleth.sso/SAML2/POST'
SP_ENTITY_ID = 'https://sp.idptestbed/shibboleth'
SP_PROTECTED = '/php-shib-protected/'
sp = 'https://idptestbed'
sp_login = '/Shibboleth.sso/SAML2/POST'
sp_entity_id = 'https://sp.idptestbed/shibboleth'
sp_protected = '/php-shib-protected/'


class BasicTaskSet(TaskSet):

    def on_start(self):
        """
        LOCUST startup process
        """
        if IGNORE_SSL:
            urllib3.disable_warnings()
        print("Start Locust Run!")

    def on_stop(self):
        """
        LOCUST shutdown process
        """
        print("End of Locust Run")

    @task(CAS_WEIGHT)
    class CASTaskSet(TaskSequence):

        @seq_task(1)
        def login(self):
            """
            Main script used to log in via CAS protocol
            """
            print("CAS Login Process Starting ...")
            client = self.client

            cas_response = client.get("/cas/login",
                                      params={'service': CAS_SP},
                                      name="CAS 1. /cas/login - GET",
                                      verify=False)
            content = cas_response.text

            found_exec = EXECUTION_PAT.search(content)
            if found_exec is None:
                print("CAS No Execution field found on login form!")
                self.interrupt()
            execution = found_exec.groups()[0]

            found_eventid = EVENTID_PAT.search(content)
            if found_eventid is None:
                print("CAS No Event Id field found on login form!")
                self.interrupt()
            event_id = found_eventid.groups()[0]

            creds = random.choice(self.locust.creds)
            cas_user = creds[0]
            cas_passwd = creds[1]
            data = {
                "username": cas_user,
                "password": cas_passwd,
                "execution": execution,
                "_eventId": event_id,
                "geolocation": "",
            }

            print("CAS Logging in User")
            cas_login_response = client.post("/cas/login?service={}".format(CAS_SP),
                                             data=data,
                                             name="CAS 2. /cas/login - POST",
                                             verify=False,
                                             allow_redirects=False)

            cas_response_url = cas_login_response.next.url
            url_query = unquote(urlparse(cas_response_url).query)
            cas_parsed_url = parse_qs(url_query)
            if 'ticket' in cas_parsed_url:
                cas_ticket = cas_parsed_url['ticket'][0]
            else:
                print("CAS No Ticket found in returned form!")
                self.interrupt()

            print("Validating service ticket ...")
            ticket_response = client.get("/cas/serviceValidate",
                                         params={'service': CAS_SP, 'ticket': cas_ticket},
                                         name="CAS 3. /cas/serviceValidate - GET",
                                         verify=False)

            user_data = ticket_response.text
            if "<cas:authenticationSuccess>" in user_data:
                print("Succesful Run!")
            else:
                print("CAS No Event Id field found on login form!")
                self.interrupt()

            print("Validating service ticket ...")

        @seq_task(2)
        def logout(self):
            """
            CAS User logout
            """
            print("CAS Logged out of SSO.")
            self.client.get("/cas/logout",
                            verify=False,
                            name="CAS 4. /cas/logout - GET")
            self.interrupt()

    @task(SAML_WEIGHT)
    class SAMLTaskSet(TaskSequence):

        @seq_task(1)
        def login(self):
            """
            Main script used to log in via SAML protocol
            """
            client = self.client

            print("SAML Go to SP and redirect to CAS")
            sp_client = SP + SP_PROTECTED
            client_response = client.get(sp_client,
                                         verify=False,
                                         name="SAML 1. {} - GET".format(SP_PROTECTED))

            print("SAML Now at CAS Login page")
            response_url = client_response.url
            url_query = unquote(urlparse(response_url).query)
            parsed_url = parse_qs(url_query)

            print("SAML Grab data passed to CAS")
            if 'RelayState' in parsed_url:
                sp_relay_state = parsed_url['RelayState'][0]
            else:
                print("SAML No RelayState field found on login form!")
                self.interrupt()

            if 'SAMLRequest' in parsed_url:
                sp_saml_request = parsed_url['SAMLRequest'][0]
            else:
                print("SAML No SAMLRequest field found on login form!")
                self.interrupt()

            content = client_response.text
            found_exec = EXECUTION_PAT.search(content)
            if found_exec is None:
                print("SAML No Execution field found on login form!")
                self.interrupt()
            execution = found_exec.groups()[0]

            found_eventid = EVENTID_PAT.search(content)
            if found_eventid is None:
                print("SAML No Event Id field found on login form!")
                self.interrupt()
            event_id = found_eventid.groups()[0]

            print("SAML Get user login info")
            creds = random.choice(self.locust.creds)
            user = creds[0]
            passwd = creds[1]

            print("SAML Build Login parameters")
            params = {
                'SAMLRequest': sp_saml_request,
                'RelayState': sp_relay_state
            }

            data = {
                "username": user,
                "password": passwd,
                "execution": execution,
                "_eventId": event_id,
                "geolocation": '',
            }

            encoded_params = urlencode(params, quote_via=quote)
            encoded_entityid = quote(SP_ENTITY_ID, safe='')
            encoded_service = quote(
                '{}/cas/idp/profile/SAML2/Callback?entityId={}&{}'.format(HOST,
                                                                          encoded_entityid,
                                                                          encoded_params), safe='')

            print("SAML Submit User login credentials ...")
            login_response = client.post("/cas/login?service=" + encoded_service,
                                         data=data,
                                         verify=False,
                                         allow_redirects=True,
                                         name="SAML 2. /cas/login?service= - POST")

            login_content = login_response.text

            found_relay = RELAY_STATE_PAT.search(login_content)
            if found_relay is None:
                print("SAML No Relay State field found!")
                self.interrupt()
            # Having issues with the relay coming back with hex code, adding this call to convert.
            idp_relay_state = found_relay.groups()[0].replace('&#x3a;', ':')

            saml_response = SAML_RESPONSE_PAT.search(login_content)
            if saml_response is None:
                print("SAML No SAML Response field found!")
                self.interrupt()
            idp_saml_response = unquote(saml_response.groups()[0])

            sp_url = SP + SP_LOGIN
            data = {
                "RelayState": idp_relay_state,
                "SAMLResponse": idp_saml_response,
            }

            print("SAML Return call to SP with SAML info ...")
            sp_response = client.post(sp_url,
                                      data=data,
                                      verify=False,
                                      name="SAML 3. {} - POST".format(SP_LOGIN))

            assert SAML_SP_PAGE_PAT.search(sp_response.text) is not None, "Expected title has not been found!"
            print("SAML Successful Run!")

        @seq_task(2)
        def logout(self):
            """
            SAML User logout
            """
            print("SAML Logged out of SSO.")
            self.client.get("/cas/logout",
                            verify=False,
                            name="SAML 4. /cas/logout - GET")
            self.interrupt()


def load_creds():
    """
    Load test user credentials.
    """
    credpath = os.path.join(
        os.path.dirname(__file__),
        "credentials.csv")
    creds = []
    with open(credpath, "r") as f:
        reader = csv.reader(f)
        for row in reader:
            creds.append((row[0], row[1]))
    return creds


class BothLocust(HttpLocust):
    task_set = BasicTaskSet
    host = HOST
    wait_time = between(2, 15)
    creds = load_creds()
