# Remove containers in silbops-net
docker stop $(docker ps -a -q); docker rm $(docker ps -a -q)