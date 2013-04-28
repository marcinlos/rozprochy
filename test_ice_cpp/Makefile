SHELL = /bin/sh

prefix = .
exec_prefix = ${prefix}
srcdir = ${prefix}/src
icedir = ${srcdir}/slice
gendir = ${srcdir}/generated
bindir = ${exec_prefix}/bin

vpath %.cpp ${srcdir} 
vpath %.cpp ${gendir}
vpath %.h ${gendir}
vpath %.ice ${icedir}

SLICE = slice2cpp

# OH MY GOD, SO MUCH BLOOD!
# Hack so ugly it fuckin' hurts :/
# Ice still doesn't work with gcc 4.7
# Don't have time to take a closer look rigth now
#CXX = g++-4.6


# Insert your program definition(s) here
# Example:
#    PROGRAMS = foo
#    foo_SRCS = fooer.c error_msg.c
#    all_LDFLAGS = -pthread

PROGRAMS = server client

server_SRCS = server.cpp
client_SRCS = client.cpp

CPPFLAGS = -I${ICE_HOME}/include 
all_LDFLAGS = -L${ICE_HOME}/lib64 -lIce -lIceUtil -lpthread
SLICES = Printer.ice

# End of program definitions 

define name_to_CPPS
${1}_CPPS = $${${1}_SLICES:%.ice=$${gendir}/%.cpp}
endef
ICE_SRCS = ${SLICES:%.ice=${gendir}/%.cpp}
SRCS = ${foreach prog,${PROGRAMS},${${prog}_SRCS}} ${ICE_SRCS} 

define name_to_OBJS
${1}_OBJS = $${${1}_SRCS:%.cpp=$${bindir}/%.o}
endef


${foreach prog,${PROGRAMS},${eval ${call name_to_OBJS,${prog}}}}
OBJS = ${sort ${foreach prog,${PROGRAMS},${${prog}_OBJS}}}
ICE_OBJS = ${ICE_SRCS:${gendir}/%.cpp=${bindir}/%.o}

EXECS = ${addprefix ${bindir}/,${PROGRAMS}}


CFLAGS = -Wall -g

.PHONY: all
all: ${EXECS}
	echo ${CXX}

.SECONDEXPANSION:
${EXECS}: ${bindir}/%: $${$$*_OBJS} ${ICE_OBJS}
	${CXX} -o $@ $^ ${all_LDFLAGS} ${$*_LDFLAGS}

${OBJS}: ${bindir}/%.o: %.cpp | ${bindir} ${ICE_SRCS}
	${CXX} ${CPPFLAGS} -c -o $@ $^
	
${ICE_OBJS}: ${bindir}/%.o: ${gendir}/%.cpp | ${bindir}
	${CXX} ${CPPFLAGS} -I${gendir} -c -o $@ $^
	
${ICE_SRCS}: ${gendir}/%.cpp: ${icedir}/%.ice | ${gendir}
	${SLICE} --output-dir ${gendir} $^

${bindir}:
	mkdir ${bindir}
	
${gendir}:
	mkdir ${gendir
  
.PHONY: clean
clean:
	${RM} ${EXECS} ${OBJS} ${ICE_OBJS} ${gendir}/*
