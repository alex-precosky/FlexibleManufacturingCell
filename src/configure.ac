#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_INIT([assign6], [1.0.0])
AC_CONFIG_SRCDIR([ControlSystem/main.c])
AM_INIT_AUTOMAKE
# Checks for programs.
AC_PROG_CC
AC_PROG_INSTALL
LT_INIT
AC_SUBST([LIBTOOL_DEPS])

AC_CONFIG_MACRO_DIRS([m4])

# Checks for libraries.

# Checks for header files.
AC_HEADER_STDC
AC_CHECK_HEADERS([float.h limits.h sys/tim.h unistd.h])

# Checks for typedefs, structures, and compiler characteristics.
AC_C_CONST
AC_HEADER_TIME

# Checks for library functions.

AC_OUTPUT([Makefile ControlSystem/Makefile FlexInterfaceC/Makefile])
