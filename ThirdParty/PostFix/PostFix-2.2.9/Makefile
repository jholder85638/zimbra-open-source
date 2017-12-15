POSTFIX_ROOT := $(shell pwd)

ZIMBRA_HOME ?= /opt/zimbra
POSTFIX_VERSION ?= 2.2.9

P4_ROOT ?= $(shell cd $(POSTFIX_ROOT)/../../..; pwd)

BUILD_PLATFORM ?= $(shell sh $(P4_ROOT)/ZimbraBuild/rpmconf/Build/get_plat_tag.sh)

PCRE_DEF := -DHAS_PCRE
PCRE_INCLUDE := -I/usr/include/pcre
PCRE_LIB := -lpcre
SASL_VERSION ?= 2.1.22.3
LDAP_VERSION ?= 2.3.40.7z
MYSQL_VERSION_STRING ?= 5.0.45
MYSQL_VERSION ?= standard-$(MYSQL_VERSION_STRING)-pc-linux-gnu-i686-glibc23
MYSQL_CLIENT_VERSION ?= standard-$(MYSQL_VERSION_STRING)-clientlibs
SYSTEM_LIB := /usr/lib

POSTFIX_TGZ_TARGET := \
	$(POSTFIX_ROOT)/builds/$(BUILD_PLATFORM)/postfix-$(POSTFIX_VERSION).tgz

ifeq ($(BUILD_PLATFORM), DEBIAN3.1)
	DBINC := 
	DBLIB := 
	LD_RUN_PATH := /opt/zimbra/mysql-$(MYSQL_VERSION)/lib:/opt/zimbra/mysql-$(MYSQL_VERSION)/lib/mysql:/opt/zimbra/openldap-$(LDAP_VERSION)/lib
else
	DBINC := -I/opt/zimbra/sleepycat-4.2.52.6/include
	DBLIB := -L/opt/zimbra/sleepycat-4.2.52.6/lib
	LD_RUN_PATH := /opt/zimbra/mysql-$(MYSQL_VERSION)/lib:/opt/zimbra/mysql-$(MYSQL_VERSION)/lib/mysql:/opt/zimbra/openldap-$(LDAP_VERSION)/lib
endif

ifeq ($(BUILD_PLATFORM), RHEL4_64)
	SYSTEM_LIB := /usr/lib64
endif

ifeq ($(BUILD_PLATFORM), MACOSX)
	MYSQL_VERSION := standard-$(MYSQL_VERSION_STRING)-apple-darwin8.2.0-powerpc
	MYSQL_CLIENT_VERSION := standard-$(MYSQL_VERSION_STRING)-apple-darwin8.2.0-powerpc
	PCRE_DEF := -DHAS_PCRE
	PCRE_INCLUDE := -I/opt/zimbra/lib/include
	PCRE_LIB := -L/opt/zimbra/lib -lpcre
endif

ifeq ($(BUILD_PLATFORM), MACOSXx86)
	MYSQL_VERSION := standard-$(MYSQL_VERSION_STRING)-apple-darwin8.2.0-i686
	MYSQL_CLIENT_VERSION := standard-$(MYSQL_VERSION_STRING)-apple-darwin8.2.0-i686
	PCRE_DEF := -DHAS_PCRE
	PCRE_INCLUDE := -I/opt/zimbra/lib/include
	PCRE_LIB := -L/opt/zimbra/lib -lpcre
endif

all: prep build install tar

prep:
	rm -rf $(ZIMBRA_HOME)/postfix-$(POSTFIX_VERSION)
	mkdir -p $(ZIMBRA_HOME)/postfix-$(POSTFIX_VERSION)/conf
	cp main.cf $(ZIMBRA_HOME)/postfix-$(POSTFIX_VERSION)/conf

build:
	(tar xzf postfix-$(POSTFIX_VERSION).tar.gz; cp makedefs.zimbra \
	postfix-$(POSTFIX_VERSION)/makedefs; \
	cd postfix-$(POSTFIX_VERSION); \
	make makefiles \
	OPT="-O2" \
	DEBUG="-g" \
	CCARGS='-DDEF_CONFIG_DIR=\"/opt/zimbra/postfix-$(POSTFIX_VERSION)/conf\" \
	-DDEF_SENDMAIL_PATH=\"/opt/zimbra/postfix-$(POSTFIX_VERSION)/sbin\" \
	-DUSE_SASL_AUTH \
	-DHAS_LDAP -DHAS_MYSQL -DUSE_TLS $(DBINC) $(PCRE_DEF) $(PCRE_INCLUDE) \
	-I/opt/zimbra/openldap-$(LDAP_VERSION)/include \
	-I/opt/zimbra/mysql-$(MYSQL_VERSION)/include \
	-I/opt/zimbra/mysql-$(MYSQL_VERSION)/include/mysql \
	-I/opt/zimbra/cyrus-sasl-$(SASL_VERSION)/include/sasl -I/usr/include -I/usr/kerberos/include' \
	AUXLIBS='-L/opt/zimbra/cyrus-sasl-$(SASL_VERSION)/lib \
	$(DBLIB) -ldb -L/opt/zimbra/openldap-$(LDAP_VERSION)/lib $(PCRE_LIB) \
	-lldap -llber -L/opt/zimbra/mysql-$(MYSQL_VERSION)/lib \
	-L/opt/zimbra/mysql-$(MYSQL_VERSION)/lib/mysql \
	-lmysqlclient -lz -lm -L$(SYSTEM_LIB) -lsasl2 -lpthread -lssl -lcrypto'; \
	env LD_RUN_PATH=$(LD_RUN_PATH) make;)

install:
	(cd postfix-$(POSTFIX_VERSION); \
	make upgrade)
	(cd $(ZIMBRA_HOME); ln -s $(ZIMBRA_HOME)/postfix-$(POSTFIX_VERSION) postfix)

tar:
	mkdir -p $(POSTFIX_ROOT)/builds/$(BUILD_PLATFORM)
	(cd $(ZIMBRA_HOME); tar czf $(POSTFIX_TGZ_TARGET) postfix-$(POSTFIX_VERSION))
	chmod -R a+w $(POSTFIX_ROOT)/builds/$(BUILD_PLATFORM)

p4edit: $(POSTFIX_TGZ_TARGET)
	p4 add $(POSTFIX_TGZ_TARGET)
	p4 edit $(POSTFIX_TGZ_TARGET)

clean:
	rm -rf postfix-$(POSTFIX_VERSION)

allclean: clean zimclean

zimclean:
	rm -rf $(ZIMBRA_HOME)/postfix-$(POSTFIX_VERSION) $(ZIMBRA_HOME)/postfix
