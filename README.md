# api.gov.au Key Manager 


Runs on localhost:5003/keys/producer

This is a prototype of registration management for API keys used to manage api.gov.au content


# Bootstraping the initial key

1. add this environment variable, with the user:pass combination of your choosing.
Eg: 'BootstrapCredentials=abcd:1234' 

2. run the service

3. request a new key using the bootstraped credential. Something like:
$ curl http://localhost:5000/keys/producer/api/new\?email\=admin@localhost\&spaces\=admin --user abcd:1234
 

