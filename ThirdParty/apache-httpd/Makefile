HTTPD_ROOT := $(shell pwd)
P4_ROOT ?= $(shell cd $(HTTPD_ROOT)/../..; pwd)

BUILD_PLATFORM ?= $(shell sh $(P4_ROOT)/ZimbraBuild/rpmconf/Build/get_plat_tag.sh)

HTTPD_VERSION ?= 2.0.54

HTTPD_TGZ_TARGET := \
	$(HTTPD_ROOT)/builds/$(BUILD_PLATFORM)/httpd-$(HTTPD_VERSION).tgz

ZIMBRA_HOME ?= /opt/zimbra

ifeq ($(BUILD_PLATFORM), RHEL5_64)
	ENVMOD := env LIBS="-L/usr/lib64" LDFLAGS="-L/usr/lib64"
endif

all: build

build:
	tar xzf src/httpd-$(HTTPD_VERSION).tar.gz
	(cd httpd-$(HTTPD_VERSION); CFLAGS="-g -O2" $(ENVMOD) ./configure --prefix=/opt/zimbra/httpd-$(HTTPD_VERSION) --enable-so; make; make install)

tar:
	mkdir -p $(HTTPD_ROOT)/builds/$(BUILD_PLATFORM)
	(cd $(ZIMBRA_HOME); tar czf $(HTTPD_TGZ_TARGET) httpd-$(HTTPD_VERSION))
	chmod -R a+w $(HTTPD_TGZ_TARGET)

p4edit: $(HTTPD_TGZ_TARGET)
	p4 add $(HTTPD_TGZ_TARGET)
	p4 edit $(HTTPD_TGZ_TARGET)

clean:
	rm -rf httpd-$(HTTPD_VERSION)

allclean: clean
	rm -rf $(ZIMBRA_HOME)/httpd-$(HTTPD_VERSION)
	rm -rf $(ZIMBRA_HOME)/httpd
