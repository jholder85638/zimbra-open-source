# Makefile for entire install tree, for RPM packages.

# EXECUTABLES

PERL_ROOT ?= $(shell pwd)
P4_ROOT ?= $(shell cd $(PERL_ROOT)/../..; pwd)
ZIMBRA_HOME ?= /opt/zimbra

LOGFILE ?= /tmp/ThirdParty-Perllibs.log

BUILD_PLATFORM ?= $(shell sh $(P4_ROOT)/ZimbraBuild/rpmconf/Build/get_plat_tag.sh)

PERL 	:= $(shell which perl)
GZIP    := $(shell which gzip)
BZIP2   := $(shell which bzip2)
CURL    := $(shell which curl)
TAR     := $(shell which tar)
FTP     := $(shell which ftp)
LYNX    := $(shell which lynx)
GPG     := $(shell which gpg)
LESS    := $(shell which less)
WGET    := $(shell which wget)
MAKE    := $(shell which make)

BDB_VERSION ?= 4.2.52.6
BDB_INC ?= $(ZIMBRA_HOME)/sleepycat-$(BDB_VERSION)/include
BDB_LIB ?= $(ZIMBRA_HOME)/sleepycat-$(BDB_VERSION)/lib

MYSQL_INCLUDE := $(ZIMBRA_HOME)/mysql/include

ifeq ($(BUILD_PLATFORM), MACOSX)
	MYSQL_INCLUDE := $(ZIMBRA_HOME)/mysql/include/mysql
endif
ifeq ($(BUILD_PLATFORM), MACOSXx86)
	MYSQL_INCLUDE := $(ZIMBRA_HOME)/mysql/include/mysql
endif

ifeq ($(BUILD_PLATFORM), MANDRIVA2006)
	DB_FILE_ARGS := DB_FILE_NAME="-ldb -lpthread"
	BDB_FILE_ARGS := BERKELEYDB_NAME="-ldb -lpthread"
endif

# DESTINATIONS

# Order is important here

DBD_PERL_LIBS 	:= \
	DBD::mysql \

PERL_LIBS 	:= \
	DBI \
	IO::Zlib \
	Bit::Vector \
	HTML::Tagset \
	HTML::Parser \
	URI \
	Net::HTTP \
	Net::SMTP \
	Proc::ProcessTable \
	HTTP::Parser \
	IO::Stringy \
	MIME::Lite \
	Mail::Mailer \
	DSKOLL/MIME-tools-5.420.tar.gz \
	XML::Parser \
	SOAP::Lite \
	Net::Telnet \
	Device::SerialPort \
	Date::Calc \
	Date::Manip \
	Date::Parse \
	Time::HiRes \
	Convert::ASN1 \
	Net::LDAP \
	Crypt::SSLeay \
	Net::Server \
	Unix::Syslog \
	IO::Compress::Base \
	IO::Compress::Zlib \
	Compress::Raw::Zlib \
	P/PM/PMQS/Compress-Zlib-1.42.tar.gz \
	Archive::Tar \
	Digest::SHA1 \
	Digest::HMAC \
	Digest::HMAC_MD5 \
	Convert::TNEF \
	Convert::UUlib \
	Net::IP \
	Net::DNS \
	File::Tail \
	File::Temp \
	Proc::ProcessTable \
	Archive::Zip 

DBFILE_PERL_LIBS := \
	DB_File

BDB_PERL_LIBS := \
	BerkeleyDB

SA_PERL_LIBS = \
	Mail::SpamAssassin

SWATCH = \
	swatch-3.1.1

PERL_MM_USE_DEFAULT	:= 1
export PERL_MM_USE_DEFAULT

JAVA_HOME		:= /usr/local/java
export JAVA_HOME

TMPDIR	:= tmp

PERL_LIB_SOURCE  := $(shell pwd)

P4_ROOT ?= $(shell cd $(PERL_LIB_SOURCE)/../..; pwd)

BUILD_PLATFORM ?= $(shell sh $(P4_ROOT)/ZimbraBuild/rpmconf/Build/get_plat_tag.sh)

PERL_TGZ_DEST_DIR := $(PERL_LIB_SOURCE)/builds/$(BUILD_PLATFORM)
PERL_TGZ_DEST	:= $(PERL_TGZ_DEST_DIR)/perllib.tgz

ifeq ($(BUILD_PLATFORM), )
        BUILD_PLATFORM := "UNKNOWN"
endif

DEST_DIR		:= $(PERL_LIB_SOURCE)/zimbramon
DEST_LIB_DIR	:= $(DEST_DIR)/lib

# TARGETS

CLEAN_TARGETS	=	\
		$(TMPDIR) \
		$(DEST_DIR) \
		$(PERL_TGZ_DEST)

all: setup $(DEST_LIB_DIR) $(PERL_TGZ_DEST_DIR)
	mkdir -p $(TMPDIR)
	@echo "Logging to $(LOGFILE)"
	@for lib in $(PERL_LIBS); do \
		echo -n "Compiling perl lib $$lib ..."; \
		LDFLAGS="-fPIC" CFLAGS="-fPIC" BERKELEYDB_INCLUDE=$(BDB_INC) BERKELEYDB_LIB=$(BDB_LIB) $(PERL) -I$(DEST_LIB_DIR) -MCPAN -e "$$CPAN::Config->{make_arg}.=' CCCDLFLAGS="-fPIC" CFLAGS="-fPIC" LDFLAGS="-fPIC"'; $$CPAN::Config->{makepl_arg}.=' CCCDLFLAGS="-fPIC" CFLAGS="-fPIC" LDFLAGS="-fPIC"'; force (\"install\", \"$$lib\")" >> $(LOGFILE) 2>&1; \
		if [ $$? -ne 0 ]; then  echo "FAILED.";  else  echo "done.";  fi \
	done

	@for lib in $(BDB_PERL_LIBS); do \
		echo -n "Compiling perl lib $$lib ..."; \
		BERKELEYDB_INCLUDE=$(BDB_INC) BERKELEYDB_LIB=$(BDB_LIB) $(BDB_FILE_ARGS) $(PERL) -I$(DEST_LIB_DIR) -MCPAN -e "force (\"install\", \"$$lib\")" >> $(LOGFILE) 2>&1; \
		if [ $$? -ne 0 ]; then  echo "FAILED.";  else  echo "done.";  fi \
	done

	@for lib in $(DBFILE_PERL_LIBS); do \
		echo -n "Compiling perl lib $$lib ..."; \
		DB_FILE_INCLUDE=$(BDB_INC) DB_FILE_LIB=$(BDB_LIB) $(DB_FILE_ARGS) $(PERL) -I$(DEST_LIB_DIR) -MCPAN -e "force (\"install\", \"$$lib\")" >> $(LOGFILE) 2>&1; \
		if [ $$? -ne 0 ]; then  echo "FAILED.";  else  echo "done.";  fi \
	done

	@echo -n "Compiling perl lib DBD::mysql ..."; \
	$(PERL) -I$(DEST_LIB_DIR) -MCPAN -e 'get DBI; $$CPAN::Config->{makepl_arg}.=" --nocatchstderr --mysql_config=\"/opt/zimbra/mysql/bin/mysql_config\" --libs=\"-L/opt/zimbra/mysql/lib -lmysqlclient -lz -lm\" --cflags=\" -I$(MYSQL_INCLUDE) -Os -fno-common\" "; $$CPAN::Config->{make_arg} .=" LD_RUN_PATH=/opt/zimbra/mysql/lib"; force ("install", "DBD::mysql")' >> $(LOGFILE) 2>&1; \
	if [ $$? -ne 0 ]; then  echo "FAILED.";  else  echo "done.";  fi \

	@for lib in $(SA_PERL_LIBS); do \
		echo -n "Compiling perl lib $$lib" ...; \
		$(PERL) -I$(DEST_LIB_DIR) -MCPAN -e 'get "Mail::SpamAssassin"; $$CPAN::Config->{makepl_arg}.=" DATADIR=/opt/zimbra/conf/spamassassin"; force ("install", "Mail::SpamAssassin")' >> $(LOGFILE) 2>&1; \
	if [ $$? -ne 0 ]; then  echo "FAILED.";  else  echo "done.";  fi \
	done	

	@for lib in $(SWATCH); do \
		echo -n "Compiling perl lib $$lib ..."; \
		cp $(PERL_LIB_SOURCE)/$$lib.tar.gz $(TMPDIR); \
		(cd $(TMPDIR); tar xzf $$lib.tar.gz; cd $$lib; \
			BERKELEYDB_INCLUDE=$(BDB_INC) BERKELEYDB_LIB=$(BDB_LIB) \
			$(PERL) -I$(DEST_LIB_DIR) Makefile.PL PREFIX=$(DEST_DIR) LIB=$(DEST_LIB_DIR); \
			$(MAKE); $(MAKE) install;) >> $(LOGFILE) 2>&1; \
			if [ $$? -ne 0 ]; then  echo "FAILED.";  else  echo "done.";  fi \
	done	
	@echo -n "Creating $(PERL_TGZ_DEST)..."; \
	(cd $(DEST_LIB_DIR); rm -f i386-linux-thread-multi/goferperf.pl; \
	tar czf $(PERL_TGZ_DEST) .); \
	if [ $$? -ne 0 ]; then  echo "FAILED.";  else  echo "done.";  fi
	chmod -R a+w $(PERL_TGZ_DEST_DIR)

tarball:
	@echo -n "Creating $(PERL_TGZ_DEST)..."; \
	(cd $(DEST_LIB_DIR); tar czf $(PERL_TGZ_DEST) .; \
	if [ $$? -ne 0 ]; then  echo "FAILED.";  else  echo "done.";  fi)
	chmod -R a+w $(PERL_TGZ_DEST_DIR)

dbfile:
	@(for lib in $(DBFILE_PERL_LIBS); do \
		echo -n "Compiling perl lib $$lib"; \
		(DB_FILE_INCLUDE=$(BDB_INC) DB_FILE_LIB=$(BDB_LIB) $(PERL) -I$(DEST_LIB_DIR) -MCPAN -e "force (\"install\", \"$$lib\")" >> $(LOGFILE) 2>&1; \
		if [ $$? -ne 0 ]; then  echo "FAILED.";  else  echo "SUCCEEDED.";  fi \
	done)

berkeleydb:
	echo -n "Compiling perl lib BerkeleyDB ..."; \
		LDFLAGS="-fPIC -lpthread" LDDFLAGS="-lpthread" CFLAGS="-fPIC" BERKELEYDB_INCLUDE=$(BDB_INC) BERKELEYDB_LIB=$(BDB_LIB) DB_FILE_INCLUDE=$(BDB_INC) DB_FILE_LIB=$(BDB_LIB) $(PERL) -I$(DEST_LIB_DIR) -MCPAN -e "$$CPAN::Config->{make_arg}.=' CCCDLFLAGS=\"-lpthread -fPIC\" CFLAGS=\"-lpthread -fPIC\" LDFLAGS=\"-lpthread -fPIC\"'; $$CPAN::Config->{makepl_arg}.=' CCCDLFLAGS=\"-lpthread -fPIC\" CFLAGS="-fPIC" LDDLFLAGS=\"-lpthread -fPIC -shared -L/usr/local/lib\" LDFLAGS=\"-lpthread -fPIC\"'; force (\"install\", \"DB_File\")"; \
	if [ $$? -ne 0 ]; then  echo "FAILED.";  else  echo "done.";  fi \

sa:
	@for lib in $(SA_PERL_LIBS); do \
		echo "Compiling perl lib $$lib"; \
		$(PERL) -I$(DEST_LIB_DIR) -MCPAN -e 'get "Mail::SpamAssassin"; $$CPAN::Config->{makepl_arg}.=" DATADIR=/opt/zimbra/conf/spamassassin"; force ("install", "Mail::SpamAssassin")' ; \
	done	

swatch:
	@for lib in $(SWATCH); do \
		echo -n "Compiling perl lib $$lib ..."; \
		cp $(PERL_LIB_SOURCE)/$$lib.tar.gz $(TMPDIR); \
		(cd $(TMPDIR); tar xzf $$lib.tar.gz; cd $$lib; \
			BERKELEYDB_INCLUDE=$(BDB_INC) BERKELEYDB_LIB=$(BDB_LIB) \
			$(PERL) -I$(DEST_LIB_DIR) Makefile.PL PREFIX=$(DEST_DIR) LIB=$(DEST_LIB_DIR); \
			$(MAKE); $(MAKE) install;) >> $(LOGFILE) 2>&1; \
			if [ $$? -ne 0 ]; then  echo "FAILED.";  else  echo "done.";  fi \
	done	

$(PERL_TGZ_DEST_DIR):
	mkdir -p $@

$(DEST_LIB_DIR):
	mkdir -p $@

DBD:
	echo "Compiling perl lib DBD::mysql"; \
	$(PERL) -I$(DEST_LIB_DIR) -MCPAN -e 'get DBI; $$CPAN::Config->{makepl_arg}.=" --nocatchstderr --mysql_config=\"/opt/zimbra/mysql/bin/mysql_config\" --libs=\"-L/opt/zimbra/mysql/lib -lmysqlclient -lz -lm\" --cflags=\" -I$(MYSQL_INCLUDE) -Os -fno-common\" "; $$CPAN::Config->{make_arg} .=" LD_RUN_PATH=/opt/zimbra/mysql/lib"; force ("install", "DBD::mysql")' ; \


setup:
	mkdir -p ${HOME}/.cpan/CPAN
	sed -e "s|@@LYNX@@|$(LYNX)|g" -e "s|@@WGET@@|$(WGET)|g" -e "s|@@LESS@@|$(LESS)|g" -e "s|@@GPG@@|$(GPG)|g" -e "s|@@UNZIP@@|$(UNZIP)|g" -e "s|@@CURL@@|$(CURL)|g" -e "s|@@BZIP2@@|$(BZIP2)|g" -e "s|@@MAKE@@|$(MAKE)|g" -e "s|@@FTP@@|$(FTP)|g" -e "s|@@TAR@@|$(TAR)|g" -e "s|@@GZIP@@|$(GZIP)|g" -e "s|@@BUILD_ROOT@@|$(PERL_ROOT)|g" MyConfig.pm.template > ${HOME}/.cpan/CPAN/MyConfig.pm 

p4edit: $(PERL_TGZ_DEST) 
	p4 add $(PERL_TGZ_DEST)
	p4 edit $(PERL_TGZ_DEST)

allclean:
	rm -rf $(CLEAN_TARGETS)
	rm -f $(LOGFILE)

clean:
	rm -rf $(CLEAN_TARGETS)
