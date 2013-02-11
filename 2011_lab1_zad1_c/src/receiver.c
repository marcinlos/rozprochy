#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <signal.h>
#include <errno.h>
#include <arpa/inet.h>
#include "net.h"
#include "service.h"

// Server socket's queue size
#define BACKLOG 5

// Maximal expected size of a textual represetation of TCP/IP address (ip:port)
#define MAX_ADDR_SIZE 64

// Synchronization variable
static volatile sig_atomic_t stop_execution = 0;

// Interrupt handler
static void handle_interrupt(int signo)
{
    stop_execution = 1;
}


// Prints usage instructions and exit with error code
static void arguments_error(void)
{
    fprintf(stderr, "Usage: receiver <port>\n");
    exit(-1);
}


static int choose_port(int argc, char* argv[])
{
    int port = DEFAULT_PORT;
    if (argc > 1)
    {
        int d;
        if ((d = sscanf(argv[1], "%d", &port)) != 1)
        {
            fprintf(stderr, "Invalid port number: `%s'\n", argv[1]);
            arguments_error();
        }
    }
    else
    {
        printf("Port number missing, using default port %d\n", port);
    }
    return port;
}


// Creates a socket and binds it to appropriate address:port
static int create_server_socket(int port)
{
    int fd = socket(AF_INET, SOCK_STREAM, 0);
    if (fd < 0)
    {
        perror("server socket creation has failed");
        exit(-1);
    }

    struct sockaddr_in sv_addr;
    memset(&sv_addr, 0, sizeof(sv_addr));

    sv_addr.sin_family = AF_INET;
    sv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    sv_addr.sin_port = htons(port);

    if (bind(fd, (struct sockaddr*) &sv_addr, sizeof(sv_addr)) < 0)
    {
        perror("bind() has failed");
        exit(-1);
    }
    if (listen(fd, BACKLOG) < 0)
    {
        perror("listen() has failed");
        exit(-1);
    }
    printf("Socket created, waiting for incoming connections...\n");
    return fd;
}

// Writes a textual representation of passed address to buffer
static void format_address(char* buffer, struct sockaddr_in* address)
{
    int port = address->sin_port;
    if (address->sin_family == AF_INET)
    {
        const char* ip = inet_ntoa(address->sin_addr);
        sprintf(buffer, "%s:%hu", ip, port);
    }
    else
    {
        sprintf(buffer, "[unknown]");
    }
}

// Displays a message about accepted connection
static void log_connection(struct sockaddr_in* address)
{
    char addr_buffer[MAX_ADDR_SIZE];
    format_address(addr_buffer, address);
    printf("Accepted connection from %s\n", addr_buffer);
}


// Performs some action and talks back to client
static void serve_client(int fd)
{
    char buffer[8];
    char type;

    recv_total(fd, &type, sizeof(type));
    if (type == 'b')
    {
        recv_total(fd, buffer, 1);
        uint8_t v = *buffer;
        uint8_t p = process_1b(v);
        send_total(fd, &p, sizeof(p));
    }
    else if (type == 's')
    {
        uint16_t v;
        recv_total(fd, buffer, sizeof(v));
        memcpy(&v, buffer, sizeof(v));
        uint16_t p = process_2b(ntohs(v));
        p = htons(p);
        send_total(fd, &p, sizeof(p));
    }
    else if (type == 'w')
    {
        uint32_t v;
        recv_total(fd, buffer, sizeof(v));
        memcpy(&v, buffer, sizeof(v));
        uint32_t p = process_4b(ntohl(v));
        p = htonl(p);
        send_total(fd, &p, sizeof(p));
    }
    else if (type == 'd')
    {
        uint64_t v;
        recv_total(fd, buffer, sizeof(v));
        memcpy(&v, buffer, sizeof(v));
        uint64_t p = process_8b(ntohll(v));
        p = htonll(p);
        send_total(fd, &p, sizeof(p));
    }
}


// Loop continuously accepting and serving client connections
static void accept_loop(int fd)
{
    struct sockaddr_in cl_addr;
    socklen_t len;

    while (! stop_execution)
    {
        int client_fd = accept(fd, (struct sockaddr*) &cl_addr, &len);
        if (client_fd < 0)
        {
            if (errno != EINTR)
            {
                perror("accept() has failed");
                exit(-1);
            }
        }
        else
        {
            log_connection(&cl_addr);
            serve_client(client_fd);
            close_socket(client_fd);
        }
    }
    printf("Shutting down...\n");
}


static void setup_interrupts(void)
{
    struct sigaction sa;
    sa.sa_handler = &handle_interrupt;
    sigemptyset(&sa.sa_mask);
    sigaction(SIGINT, &sa, NULL);
}


int main(int argc, char* argv[])
{
    setup_interrupts();
    int port = choose_port(argc, argv);
    int fd = create_server_socket(port);
    accept_loop(fd);
    close_socket(fd);
    printf("Done\n");
    return 0;
}

