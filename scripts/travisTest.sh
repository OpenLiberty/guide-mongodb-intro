#!/bin/bash
set -euxo pipefail

##############################################################################
##
##  Travis CI test script
##
##############################################################################

# LMP 3.0+ goals are listed here: https://github.com/OpenLiberty/ci.maven#goals

## Rebuild the application
#       package                   - Take the compiled code and package it in its distributable format.
#       liberty:create            - Create a Liberty server.
#       liberty:install-feature   - Install a feature packaged as a Subsystem Archive (esa) to the Liberty runtime.
#       liberty:deploy            - Copy applications to the Liberty server's dropins or apps directory. 

mvn -q clean package liberty:create liberty:install-feature liberty:deploy


## Run the tests
#       liberty:start             - Start a Liberty server in the background.
#       failsafe:integration-test - Runs the integration tests of an application.
#       liberty:stop              - Stop a Liberty server.
#       failsafe:verify           - Verifies that the integration tests of an application passed.

mvn liberty:start
mvn failsafe:integration-test liberty:stop
mvn failsafe:verify
