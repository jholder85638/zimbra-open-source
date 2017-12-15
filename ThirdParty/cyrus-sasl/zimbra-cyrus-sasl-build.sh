#!/bin/bash -x

#
# Builds a saslauthd with Zimbra authentication support.  We modify
# configure.in which means we have to run autoconf.  I have borrowed
# the steps to run autoconf from the rpm spec.  The cyrus CVS tree's
# SMakefile is probably the source for the rpm spec.
#
release=cyrus-sasl-2.1.22
patchlevel=3
src=${release}.${patchlevel}
platform=`uname -s`

grep "Fedora release 7" /etc/redhat-release >/dev/null 2>&1
if [ $? -eq 0 ]; then
    fedoraseven=1
else
    fedoraseven=0
fi

grep "4.0" /etc/debian_version >/dev/null 2>&1
if [ $? -eq 0 ]; then
    etch=1
else
    etch=0
fi

xml2_version=2.6.29

rm -fr build
mkdir build
cd build
tar xfz ../src/cyrus-sasl-2.1.22.tar.gz  -C .
chmod -R +w ${release}
mv ${release} ${src}

cd ${src}
patch -g0 -p1 < ../../sasl-link-order.patch
patch -g0 -p1 < ../../sasl-darwin.patch
patch -g0 -p1 < ../../sasl-auth-zimbra.patch
rm config/ltconfig config/libtool.m4
if [ -x /usr/bin/libtoolize ]; then
	LIBTOOLIZE=/usr/bin/libtoolize
else
	if [ -x /opt/local/bin/glibtoolize ]; then
		export CPPFLAGS=-DDARWIN
		LIBTOOLIZE=/opt/local/bin/glibtoolize
	else
		echo "Where is libtoolize?"
		exit 1
	fi
fi
$LIBTOOLIZE -f -c
aclocal -I config -I cmulocal
automake -a -c -f
autoheader
autoconf -f

cd saslauthd
rm config/ltconfig
$LIBTOOLIZE -f -c
aclocal -I config -I ../cmulocal -I ../config
automake -a -c -f
autoheader
autoconf -f

cd ..
sed -i.bak 's/-lRSAglue //' configure
if [ $platform = "Darwin" ]; then
LIBS="/opt/zimbra/libxml2/lib/libxml2.a" CFLAGS="-D_REENTRANT -g -O2 -I/opt/zimbra/libxml2/include/libxml2" ./configure --enable-zimbra --prefix=/opt/zimbra/${src} \
            --with-saslauthd=/opt/zimbra/${src}/state \
            --with-plugindir=/opt/zimbra/${src}/lib/sasl2 \
            --enable-static=no \
            --enable-shared \
            --with-dblib=no \
            --with-libxml2=/opt/zimbra/libxml2-${xml2_version}/bin/xml2-config \
            --enable-login
else
LIBS="/opt/zimbra/libxml2/lib/libxml2.a" CFLAGS="-D_REENTRANT -g -O2" ./configure --enable-zimbra --prefix=/opt/zimbra/${src} \
            --with-saslauthd=/opt/zimbra/${src}/state \
            --with-plugindir=/opt/zimbra/${src}/lib/sasl2 \
            --with-dblib=no \
            --with-libxml2=/opt/zimbra/libxml2-${xml2_version}/bin/xml2-config \
            --enable-login
fi
if [ $platform = "Darwin" ]; then
     sed -i .bak -e 's/\_la_LDFLAGS)/_la_LDFLAGS) $(AM_LDFLAGS)/' plugins/Makefile
     sed -i .bak -e "s|-L/opt/zimbra/libxml2-${xml2_version}/lib -lxml2||" saslauthd/Makefile
elif [  $fedoraseven -eq 1 ]; then
     sed -i.bak -e 's/\_la_LDFLAGS)/_la_LDFLAGS) $(AM_LDFLAGS)/' plugins/Makefile
elif [ $etch -eq 1 ]; then
     sed -i.bak -e 's/\_la_LDFLAGS)/_la_LDFLAGS) $(AM_LDFLAGS)/' plugins/Makefile
fi
make
