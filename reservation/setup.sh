#!/bin/bash

docker-compose up&

PGPASSWORD=admin docker exec -it reservation-postgres psql -U postgres postgres -c"CREATE ROLE reservation WITH LOGIN PASSWORD 'reservation';"
