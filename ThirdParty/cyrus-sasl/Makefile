CYRUS_ROOT := $(shell pwd)
P4_ROOT ?= $(shell cd $(CYRUS_ROOT)/../..; pwd)

BUILD_PLATFORM ?= $(shell sh $(P4_ROOT)/ZimbraBuild/rpmconf/Build/get_plat_tag.sh)

ifeq ($(BUILD_PLATFORM), )
	BUILD_PLATFORM := "UNKNOWN"
endif

ZIMBRA_HOME ?= /opt/zimbra

CYRUS_RELEASE ?= 2.1.22
CYRUS_PATCHLEVEL ?= 3
CYRUS_VERSION ?= $(CYRUS_RELEASE).$(CYRUS_PATCHLEVEL)
CYRUS_TGZ_TARGET := $(CYRUS_ROOT)/builds/$(BUILD_PLATFORM)/cyrus-sasl-$(CYRUS_VERSION).tgz

all: build 

build: 
	mkdir -p $(CYRUS_ROOT)/builds/$(BUILD_PLATFORM)
	_S_='~' ./zimbra-cyrus-sasl-build.sh
	(cd build/cyrus-sasl-$(CYRUS_VERSION); make install)
	(cd $(ZIMBRA_HOME); ln -s cyrus-sasl-$(CYRUS_VERSION) cyrus-sasl; \
	tar czf $(CYRUS_ROOT)/builds/$(BUILD_PLATFORM)/cyrus-sasl-$(CYRUS_VERSION).tgz \
	cyrus-sasl-$(CYRUS_VERSION))

/opt/zimbra/libxml2/lib/libxml2.a:
	make -C $(P4_ROOT)/ThirdParty/libxml2


p4edit: $(CYRUS_TGZ_TARGET)
	p4 add $(CYRUS_TGZ_TARGET)
	p4 edit $(CYRUS_TGZ_TARGET)

clean:
	rm -rf build

allclean: clean
	rm -rf $(ZIMBRA_HOME)/cyrus-sasl-$(CYRUS_VERSION)
	rm -rf $(ZIMBRA_HOME)/cyrus-sasl
