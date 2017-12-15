MYSQL_ROOT := $(shell pwd)
P4_ROOT ?= $(shell cd $(MYSQL_ROOT)/../..; pwd)

BUILD_PLATFORM ?= $(shell sh $(P4_ROOT)/ZimbraBuild/rpmconf/Build/get_plat_tag.sh)

ifeq ($(BUILD_PLATFORM), )
	BUILD_PLATFORM := "UNKNOWN"
endif

ZIMBRA_HOME ?= /opt/zimbra

MYSQL_VERSION_STRING ?= 5.0.45
MYSQL_VERSION ?= standard-$(MYSQL_VERSION_STRING)-pc-linux-gnu-i686-glibc23
MYSQL_CLIENT_VERSION ?= standard-$(MYSQL_VERSION_STRING)-clientlibs

ifeq ($(BUILD_PLATFORM), MACOSXx86)
	MYSQL_VERSION := standard-$(MYSQL_VERSION_STRING)-apple-darwin8.2.0-i686
	MYSQL_CLIENT_VERSION := standard-$(MYSQL_VERSION_STRING)-clientlibs
endif

ifeq ($(BUILD_PLATFORM), MACOSX)
	MYSQL_VERSION := standard-$(MYSQL_VERSION_STRING)-apple-darwin8.2.0-powerpc
	MYSQL_CLIENT_VERSION := standard-$(MYSQL_VERSION_STRING)-clientlibs
endif

MYSQL_TGZ_TARGET := \
	$(MYSQL_ROOT)/$(BUILD_PLATFORM)/mysql-$(MYSQL_VERSION).tar.gz

MYSQL_CLIENT_TGZ_TARGET := \
	$(MYSQL_ROOT)/$(BUILD_PLATFORM)/mysql-$(MYSQL_CLIENT_VERSION).tgz

all: clientlibs install symlink

install:
	mkdir -p $(ZIMBRA_HOME)
	(cd $(ZIMBRA_HOME); tar xzf $(MYSQL_TGZ_TARGET))
	-(cd $(ZIMBRA_HOME)/mysql-$(MYSQL_VERSION)/lib; \
	tar xzf $(MYSQL_ROOT)/$(BUILD_PLATFORM)/mysql-$(MYSQL_CLIENT_VERSION).tgz)
	cp -f $(ZIMBRA_HOME)/mysql-$(MYSQL_VERSION)/include/mysql/* $(ZIMBRA_HOME)/mysql-$(MYSQL_VERSION)/include

symlink:
	(cd $(ZIMBRA_HOME); rm -f mysql; ln -s mysql-$(MYSQL_VERSION) mysql)
	-(rm $(ZIMBRA_HOME)/mysql-$(MYSQL_VERSION)/lib/mysql)
	-(ln -s $(ZIMBRA_HOME)/mysql-$(MYSQL_VERSION)/lib $(ZIMBRA_HOME)/mysql-$(MYSQL_VERSION)/lib/mysql)

clean: ;

clientlibs: builddynamic
	-(cd $(ZIMBRA_HOME)/mysql-$(MYSQL_VERSION)/lib/mysql ; tar czvf $(MYSQL_ROOT)/$(BUILD_PLATFORM)/mysql-$(MYSQL_CLIENT_VERSION).tgz libmysqlclient.*{so,dylib}*)
#	rm -rf mysql-$(MYSQL_VERSION_STRING)
#	tar xzf src/mysql-$(MYSQL_VERSION_STRING).tar.gz
#	(cd mysql-$(MYSQL_VERSION_STRING); ./configure --without-server --libdir=/opt/zimbra/mysql-$(MYSQL_VERSION)/lib --prefix=/opt/zimbra/mysql; make; make install)

buildstatic:
	rm -rf mysql-$(MYSQL_VERSION_STRING)
	tar xzf src/mysql-$(MYSQL_VERSION_STRING).tar.gz
	(cd mysql-$(MYSQL_VERSION_STRING); CFLAGS="-g -O3" CXX=gcc CXXFLAGS="-g -O3 -felide-constructors -fno-exceptions -fno-rtti" ./configure --enable-local-infile --enable-assembler --with-extra-charsets=complex --enable-thread-safe-client --with-readline --with-mysqld-ldflags=-all-static --libdir=/opt/zimbra/mysql-$(MYSQL_VERSION)/lib --prefix=/opt/zimbra/mysql-$(MYSQL_VERSION) ; make; make install)
	-mkdir -p $(shell dirname $(MYSQL_TGZ_TARGET))
	(tar czvf $(MYSQL_TGZ_TARGET) -C /opt/zimbra/ mysql-$(MYSQL_VERSION)/)

builddynamic:
	rm -rf mysql-$(MYSQL_VERSION_STRING)
	tar xzf src/mysql-$(MYSQL_VERSION_STRING).tar.gz
	(cd mysql-$(MYSQL_VERSION_STRING); CFLAGS="-g -O3" CXX=gcc CXXFLAGS="-g -O3 -felide-constructors -fno-exceptions -fno-rtti" ./configure --enable-local-infile --enable-assembler --with-extra-charsets=complex --enable-thread-safe-client --with-readline --libdir=/opt/zimbra/mysql-$(MYSQL_VERSION)/lib --prefix=/opt/zimbra/mysql-$(MYSQL_VERSION) ; make; make install)
	-mkdir -p $(shell dirname $(MYSQL_TGZ_TARGET))
	(tar czvf $(MYSQL_TGZ_TARGET) -C /opt/zimbra/ mysql-$(MYSQL_VERSION)/)

p4edit: $(MYSQL_TGZ_TARGET) 
	p4 add $(MYSQL_TGZ_TARGET) $(MYSQL_CLIENT_TGZ_TARGET)
	p4 edit $(MYSQL_TGZ_TARGET) $(MYSQL_CLIENT_TGZ_TARGET)

allclean: clean
	rm -rf $(ZIMBRA_HOME)/mysql-$(MYSQL_VERSION)
	rm -rf $(ZIMBRA_HOME)/mysql
