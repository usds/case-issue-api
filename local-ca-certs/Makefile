# All the rules we need to create local Certificate Authority, Server, and Client certificates

KEY_SIZE=4096
BASE_DN=/C=US/ST=DC/L=Washington/O=Fake\ CA
CA_NAME=Fake\ Certificate\ Authority
# this is traditional:
KEYSTORE_PASSWORD?=changeit
TRUSTSTORE_PASSWORD?=yololol
CLIENT_PASSWORD?=justdoit

# Variables to allow customization of certificate DNs.
CLIENT_RDN ?= CN=$(CLIENT_CN)+UID=$(CLIENT_UID)
CLIENT_UID ?= $(shell echo -n $* | md5)
CLIENT_CN ?= $*
SERVER_NAME ?= localhost

.PRECIOUS: %.key %.password

default: truststore.jks keystore.p12

# "clean" would be the normal name for this target, but wanted to make it a little
# harder to nuke everything
realclean:
	rm *.{csr,key,password,crt,jks,p12}

# this could be rendered obsolete by just using the '-nodes' ("No DES") option to genrsa
%.password:
	openssl rand -base64 32 -out $@

%.key: %.password
	openssl genrsa -aes256 -out $@ -passout file:$< $(KEY_SIZE)

%.csr: %.key
	openssl req -new -key $< -out $@ -subj $(BASE_DN)/$(CLIENT_RDN) -passin file:$*.password -multivalue-rdn

%.crt: %.csr ca.crt
	openssl x509 -req -in $< -CA ca.crt -CAkey ca.key -out $@ -passin file:ca.password -CAcreateserial

%.p12: %.crt
	openssl pkcs12 -export -in $< -inkey $*.key -out $@ -name $* -passin file:$*.password -passout pass:$(CLIENT_PASSWORD)

server.csr: server.key
	openssl req -new -key $< -out $@ -subj $(BASE_DN)/CN=$(SERVER_NAME) -passin file:server.password

ca.crt: ca.key
	openssl req -x509 -new -key $< -out $@ -subj $(BASE_DN)/CN=$(CA_NAME) -passin file:ca.password -multivalue-rdn -days 3650

keystore.p12: server.crt
	openssl pkcs12 -export -in server.crt -inkey server.key -out $@ -name localhost -passin file:server.password -passout pass:$(KEYSTORE_PASSWORD)

truststore.jks: ca.crt
	keytool -import -keystore $@ -file ./ca.crt -alias localCA -storepass $(TRUSTSTORE_PASSWORD) -noprompt
