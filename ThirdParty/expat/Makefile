EXPAT_ROOT := $(shell pwd)
P4_ROOT ?= $(shell cd $(EXPAT_ROOT)/../..; pwd)

BUILD_PLATFORM ?= $(shell sh $(P4_ROOT)/ZimbraBuild/rpmconf/Build/get_plat_tag.sh)

EXPAT_TGZ_TARGET := $(EXPAT_ROOT)/builds/$(BUILD_PLATFORM)/expat.tgz

ZIMBRA_HOME ?= /opt/zimbra

all: build_$(BUILD_PLATFORM) 

build_MACOSXx86:
	mkdir -p $(ZIMBRA_HOME)
	(cd $(ZIMBRA_HOME); tar xzf $(EXPAT_TGZ_TARGET))

build_MACOSX:
	mkdir -p $(ZIMBRA_HOME)
	(cd $(ZIMBRA_HOME); tar xzf $(EXPAT_TGZ_TARGET))

build_F7: ;

build_FC5: ;

build_FC4: ;

build_FC3: ;

build_RHEL4: ;

build_RHEL4_64: ;

build_RHEL5: ;

build_RHEL5_64: ;

build_DEBIAN4.0: ;

build_DEBIAN3.1: ;

build_SuSEES9: ;

build_SuSEES10: ;

build_SuSE10: ;

build_SLES10_64: ;

build_openSUSE_10.2: ;

build_UBUNTU6: ;

build_MANDRIVA2006: ;

build_RPL1: ;

p4edit: $(EXPAT_TGZ_TARGET)
	p4 edit $(EXPAT_TGZ_TARGET)

clean: ;

allclean: clean
