#!/bin/bash
para=$1
clean(){
	gcloud compute ssh instance-${i} --zone=us-central1-a --command="pkill -9 java" --strict-host-key-checking=no --ssh-key-file=~/.ssh/my_google_cloud_key 
}

Start(){
	gcloud compute ssh instance-${i} --zone=us-central1-a --command="java -cp /home/johnny/DoubleWC.jar yinzhe.test.OtherNodeentry.OtherNodeentry 9001 10.128.0.7 5001 100 30 50 7001" --strict-host-key-checking=no --ssh-key-file=~/.ssh/my_google_cloud_key
}

for ((i=1;i<para;i++)); 
do
	clean &
done

wait
echo "Done"

for ((i=1;i<para;i++)); 
do
	Start &
done

echo "Done"
