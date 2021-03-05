#!/bin/bash

DEFAULT_TESTUSER_PASSWORD=P@ssw0rd
echo "Resetting passwords of all users (in case tests changed them)"

docker exec samba bash -c "samba-tool user setpassword admin --newpassword=$DEFAULT_TESTUSER_PASSWORD"
docker exec samba bash -c "samba-tool user setpassword aburr --newpassword=$DEFAULT_TESTUSER_PASSWORD"
docker exec samba bash -c "samba-tool user setpassword aham --newpassword=$DEFAULT_TESTUSER_PASSWORD"
docker exec samba bash -c "samba-tool user setpassword expireduser --newpassword=$DEFAULT_TESTUSER_PASSWORD"
docker exec samba bash -c "samba-tool user setpassword disableduser --newpassword=$DEFAULT_TESTUSER_PASSWORD"
docker exec samba bash -c "samba-tool user setpassword changepassword --newpassword=$DEFAULT_TESTUSER_PASSWORD"
docker exec samba bash -c "samba-tool user setpassword changepasswordnoreset --newpassword=$DEFAULT_TESTUSER_PASSWORD"
docker exec samba bash -c "samba-tool user setpassword expirestomorrow --newpassword=$DEFAULT_TESTUSER_PASSWORD"

