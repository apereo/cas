#! /usr/bin/env python

from __future__ import print_function
import csv
import datetime
import os
import random
import re
import string
# from urlparse import urlparse, parse_qs
from locust import HttpLocust, TaskSet, task
from locust.exception import StopLocust
import six
from six.moves.urllib.parse import urlparse, parse_qs


class CASTaskSet(TaskSet):
    execution_pat = re.compile(r'<input type="hidden" name="execution" value="([^"]+)"')
    eventid_pat = re.compile(r'<input type="hidden" name="_eventId" value="([^"]+)"')
    badpasswd_freq = 0.01
    logout_url_freq = 0.25

    def on_start(self):
        """
        Start a new session.
        """
        self.initialize()
        self.reset()

    def initialize(self):
        """
        Initialize parameters for locust that don't change when reset.
        """
        print("Initializing locust ...")
        self.is_authenticated = False
        lifetime_bins = []
        minute = 60
        hour = 3600
        lifetime_bins.extend([60] * 7)
        lifetime_bins.extend([5 * minute] * 2)
        lifetime_bins.extend([2 * hour] * 1)
        seconds = random.choice(lifetime_bins)
        self.base_lifetime = seconds

    def reset(self):
        self.is_authenticated = False
        self.login()
        wiggliness = random.uniform(0.85, 1.15)
        seconds = int(self.base_lifetime * wiggliness)
        self.expiration = datetime.datetime.now() + datetime.timedelta(seconds=seconds)
        print("Locust created.  Expires in {0} seconds.".format(seconds))

    def login(self):
        """
        Login, obtain TGT.
        Simulates a bad password `badpasswd_freq` percent of the time.
        Sets self.is_authenticated if TGT is obtained.
        """
        print("Authenticating ...")
        client = self.client
        with client.get("/cas/login", catch_response=True) as response:
            if response.status_code == 404:
                response.success()
            content = response.content.decode('utf-8')
        m = self.execution_pat.search(content)
        if m is None:
            return
        execution = m.groups()[0]
        m = self.eventid_pat.search(content)
        if m is None:
            return
        event_id = m.groups()[0]
        creds = random.choice(self.locust.creds)
        user = creds[0]
        passwd = creds[1]
        if self.badpasswd_freq >= random.random():
            passwd = passwd[:-1]
            self.simulate_badpasswd(user, passwd, execution, event_id)
            return
        data = {
            "username": user,
            "password": passwd,
            "execution": execution,
            "_eventId": event_id,
            "geolocation": "",
        }
        response = client.post("/cas/login", data=data)
        if 'TGC' in client.cookies.keys():
            self.is_authenticated = True

    def simulate_badpasswd(self, user, passwd, execution, event_id):
        """
        Simulate fat-fingering the password.
        """
        client = self.client
        data = {
            "username": user,
            "password": passwd,
            "execution": execution,
            "_eventId": event_id,
            "geolocation": "",
        }
        with client.post("/cas/login", catch_response=True, data=data) as response:
            if response.status_code == 401:
                response.success()
                print("Simulated password mis-entry.")
            else:
                print("Unexpected successful authentication!")
                response.failure("Got status code {0} when 401 was expected!".format(response.status_code))

    @task
    def authenticate_to_service(self):
        if not self.is_authenticated:
            print("Recycling locust ...")
            self.reset()
            return
        if datetime.datetime.now() >= self.expiration:
            print("Locust is expired.")
            self.expire()
            self.reset()
            return
        service = '''https://badges.stage.lafayette.edu/'''
        client = self.client
        print("Obtaining service ticket ...")
        with client.get("/cas/login", catch_response=True, params={'service': service},
                        allow_redirects=False) as response:
            if response.status_code in (301, 302):
                response.success()
                location = response.headers['Location']
            else:
                response.failure("Got status code {0} while obtaining ST.".format(response.status_code))
                return
        p = urlparse(location)
        q = p.query
        qs = parse_qs(q)
        ticket = qs['ticket']
        print("Validating service ticket ...")
        response = client.get(
            "/cas/serviceValidate",
            params={'service': service, 'ticket': ticket},
            name="/cas/serviceValidate?ticket=[ticket]")

    def expire(self):
        """
        Logout, or simulate just closing the browser window.
        """
        self.is_authenticated = False
        if self.logout_url_freq >= random.random():
            self.logout()
        else:
            self.client.cookies.clear()

    def logout(self):
        """
        Logout.
        This locust should be reset as a new hatchling."
        """
        client = self.client
        client.get("/cas/logout")
        print("Logged out of SSO.")


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


class CASLocust(HttpLocust):
    task_set = CASTaskSet
    host = 'https://cas.stage.lafayette.edu'
    second = 1000
    min_wait = 5 * second
    max_wait = 15 * second
    creds = load_creds()























