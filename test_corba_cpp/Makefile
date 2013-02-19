SHELL = /bin/sh

prefix = .
exec_prefix = ${prefix}
srcdir = ${prefix}/src
idldir = ${srcdir}/idl
gendir = ${srcdir}/generated
bindir = ${exec_prefix}/bin

vpath %.cpp ${srcdir} 
vpath %.cpp ${gendir}
vpath %.idl ${idldir}

# Insert your program definition(s) here
# Example:
#    PROGRAMS = foo
#    foo_SRCS = fooer.c error_msg.c
#    all_LDFLAGS = -pthread

PROGRAMS = server client

server_SRCS = server.cpp Greeter_impl.cpp greeterSK.cpp
client_SRCS = client.cpp greeterSK.cpp

all_LDFLAGS = -lomnithread -lomniORB4 

# End of program definitions 

SRCS = ${foreach prog,${PROGRAMS},${${prog}_SRCS}}

define name_to_OBJS
${1}_OBJS = $${${1}_SRCS:%.cpp=$${bindir}/%.o}
endef

${foreach prog,${PROGRAMS},${eval ${call name_to_OBJS,${prog}}}}
OBJS = ${sort ${foreach prog,${PROGRAMS},${${prog}_OBJS}}}
EXECS = ${addprefix ${bindir}/,${PROGRAMS}}

CFLAGS = -Wall -g

.PHONY: all
all: ${EXECS}

.SECONDEXPANSION:
${EXECS}: ${bindir}/%: $${$$*_OBJS}
	${CXX} -o $@ $^ ${all_LDFLAGS} ${$*_LDFLAGS}

${OBJS}: ${bindir}/%.o: %.cpp | ${bindir}
	${CXX} ${CFLAGS} -c -o $@ $^

${bindir}:
	mkdir ${bindir}
  
.PHONY: clean
clean:
	${RM} ${EXECS} ${OBJS}
