#!/bin/bash

docker-compose up&

sleep 7

PGPASSWORD=admin docker exec -it reservation-postgres psql -U postgres postgres -c"CREATE ROLE reservation WITH LOGIN PASSWORD 'reservation';"
