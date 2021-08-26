#! /usr/bin/env python

from __future__ import print_function
import csv
import os
import random
import re
import urllib3
from urllib.parse import parse_qs, urlparse, unquote
from locust import HttpUser, between, SequentialTaskSet, task
from locust.exception import InterruptTaskSet

# IdP Host
HOST = "https://localhost:8443"

# Testing using locally generated cert, so turning off error messaging
IGNORE_SSL = True

# Search Patterns
EXECUTION_PAT = re.compile(r'<input type="hidden" name="execution" value="([^"]+)"')
EVENTID_PAT = re.compile(r'<input type="hidden" name="_eventId" value="([^"]+)"')

# Service Provider settings
CAS_SP = "https://httpbin.org/get"

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


class CASLocust(HttpUser):
    """
    LOCUST startup
    """
    host = HOST
    wait_time = between(2, 15)
    creds = load_creds()
    
    def on_start(self):
        """
        LOCUST startup process
        """
        if IGNORE_SSL:
            urllib3.disable_warnings()
        print("CAS Start Locust Run!")

    @task(1)
    def login(self):
        """
        Main script used to log in via CAS protocol
        """
        print("CAS Login Process Starting ...")
        client = self.client

        cas_response = client.get("/cas/login",
                                  params={'service': CAS_SP},
                                  name="1. /cas/login - GET",
                                  verify=False)
        content = cas_response.text

        found_exec = EXECUTION_PAT.search(content)
        if found_exec is None:
            print("CAS No Execution field found on login form!")
            print(content)
            raise InterruptTaskSet()
        execution = found_exec.groups()[0]

        found_eventid = EVENTID_PAT.search(content)
        if found_eventid is None:
            print("CAS No Event Id field found on login form!")
            raise InterruptTaskSet()
        event_id = found_eventid.groups()[0]

        creds = random.choice(self.creds)
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
                                         name="2. /cas/login - POST",
                                         verify=False,
                                         allow_redirects=False)

        cas_response_url = cas_login_response.next.url
        url_query = unquote(urlparse(cas_response_url).query)
        cas_parsed_url = parse_qs(url_query)

        if 'ticket' in cas_parsed_url:
            cas_ticket = cas_parsed_url['ticket'][0]
        else:
            print("CAS No Ticket found in returned form!")
            raise InterruptTaskSet()

        print("CAS Validating service ticket ...")
        ticket_response = client.get("/cas/serviceValidate",
                                     params={'service': CAS_SP, 'ticket': cas_ticket},
                                     name="3. /cas/serviceValidate - GET",
                                     verify=False)

        ticket_status = ticket_response.status_code
        assert ticket_status is 200, "CAS Ticket response code of: ".format(ticket_status)

        user_data = ticket_response.text
        if "<cas:authenticationSuccess>" in user_data:
            print("CAS Successful Run!")
        else:
            print("CAS No Event Id field found on login form!")
            raise InterruptTaskSet()

        print("CAS Validating service ticket ...")

    @task(2)
    def logout(self):
        """
        CAS User logout
        """
        print("CAS Logged out of SSO.")
        self.client.get("/cas/logout",
                        verify=False,
                        name="4. /cas/logout - GET")

    def on_stop(self):
        """
        LOCUST end process
        """
        print("CAS End of Locust Run")
