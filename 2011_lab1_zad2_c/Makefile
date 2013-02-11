SHELL = /bin/sh

prefix = .
exec_prefix = ${prefix}
srcdir = ${prefix}/src
bindir = ${exec_prefix}/bin

vpath %.c ${srcdir} 

# Insert your program definition(s) here
# Example:
#    PROGRAMS = foo
#    foo_SRCS = fooer.c error_msg.c
#    all_LDFLAGS = -pthread

PROGRAMS = client

client_SRCS = client.c

all_LDFLAGS =

# End of program definitions

SRCS = ${foreach prog,${PROGRAMS},${${prog}_SRCS}}

define name_to_OBJS
${1}_OBJS = $${${1}_SRCS:%.c=$${bindir}/%.o}
endef

${foreach prog,${PROGRAMS},${eval ${call name_to_OBJS,${prog}}}}
OBJS = ${sort ${foreach prog,${PROGRAMS},${${prog}_OBJS}}}
EXECS = ${addprefix ${bindir}/,${PROGRAMS}}

CFLAGS = -Wall -g

.PHONY: all
all: ${EXECS}

.SECONDEXPANSION:
${EXECS}: ${bindir}/%: $${$$*_OBJS}
	${CC} ${all_LDFLAGS} ${$*_LDFLAGS} -o $@ $^

${OBJS}: ${bindir}/%.o: %.c | ${bindir}
	${CC} ${CFLAGS} -c -o $@ $^

${bindir}:
	mkdir ${bindir}
  
.PHONY: clean
clean:
	${RM} ${EXECS} ${OBJS}
