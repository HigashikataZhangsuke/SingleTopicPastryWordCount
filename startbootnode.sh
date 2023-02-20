#!/bin/bash

gcloud compute ssh instance-0 --zone=us-central1-a --command="java -cp /home/johnny/DoubleWC.jar yinzhe.test.BootNodeentry.BootNodeentry 5001 10.128.0.7 5001 1 30 50 7001" --strict-host-key-checking=no --ssh-key-file=~/.ssh/my_google_cloud_key
