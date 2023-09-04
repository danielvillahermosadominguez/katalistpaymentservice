#!/bin/bash

  
# Start the second process
java -jar ./katalistpaymentservice-0.0.1-SNAPSHOT.jar "$@"

  
# Wait for any process to exit
wait -n
  
# Exit with status of process that exited first
exit $?