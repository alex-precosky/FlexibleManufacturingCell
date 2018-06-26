LIBTOOL_DEPS = @LIBTOOL_DEPS@
libtool: $(LIBTOOL_DEPS)
	$(SHELL) ./config.status libtool

CFLAGS = -g3 -O0

bin_PROGRAMS = testControl
testControl_SOURCES = testControl.c
testControl_LDADD = ./libflexInterface.la
testControl_LDFLAGS = -lpthread

lib_LTLIBRARIES = libflexInterface.la
libflexInterface_la_SOURCES = flexInterface.c flexTime.c 
libflexInterface_la_LDFLAGS = -lpthread

