FROM postgres

COPY create-db.sh /docker-entrypoint-initdb.d/01-user-schema-creation.sh
# Be compatible with broken Docker setups
RUN chmod a+r /docker-entrypoint-initdb.d/*
