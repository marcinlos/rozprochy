SHELL = /bin/sh

prefix = .
exec_prefix = ${prefix}
srcdir = ${prefix}/src
icedir = ${prefix}/slice
gendir = ${prefix}/generated
bindir = ${exec_prefix}/bin

vpath %.cpp ${srcdir} 
vpath %.cpp ${gendir}
vpath %.h ${gendir}
vpath %.ice ${icedir}

SLICE = slice2cpp



# Insert your program definition(s) here
# Example:
#    PROGRAMS = foo
#    foo_SRCS = fooer.c error_msg.c
#    all_LDFLAGS = -pthread

PROGRAMS = client

client_SRCS = main.cpp Client.cpp Pinger.cpp

all_LDFLAGS = -L${ICE_HOME}/lib64 -lIce -lIceUtil -lpthread
SLICES = Bank.ice

CXXFLAGS=-I${ICE_HOME}/include -g

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
	${CXX} ${CPPFLAGS} ${CXXFLAGS} -c -o $@ $^
	
${ICE_OBJS}: ${bindir}/%.o: ${gendir}/%.cpp | ${bindir}
	${CXX} ${CPPFLAGS} ${CXXFLAGS} -I${gendir} -c -o $@ $^
	
${ICE_SRCS}: ${gendir}/%.cpp: ${icedir}/%.ice | ${gendir}
	${SLICE} --output-dir ${gendir} $^

${bindir}:
	mkdir ${bindir}
	
${gendir}:
	mkdir ${gendir}
  
.PHONY: clean
clean:
	${RM} ${EXECS} ${OBJS} ${ICE_OBJS} ${gendir}/*
