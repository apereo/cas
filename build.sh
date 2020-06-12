#!/bin/bash

heroku container:push web --app cas-gradle-buildcache
heroku container:push release --app cas-gradle-buildcache
heroku open --app cas-gradle-buildcache