#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <arpa/inet.h>
#include "net.h"
#include "service.h"


#define BACKLOG 5

#define MAX_ADDR_SIZE 64


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
            fprintf(stderr, "Kurwa, no %d\n", d);
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

static void log_connection(struct sockaddr_in* address)
{
    char addr_buffer[MAX_ADDR_SIZE];
    format_address(addr_buffer, address);
    printf("Accepted connection from %s\n", addr_buffer);
}


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


static void accept_loop(int fd)
{
    struct sockaddr_in cl_addr;
    socklen_t len;

    for (;"ever";)
    {
        int client_fd = accept(fd, (struct sockaddr*) &cl_addr, &len);
        if (client_fd < 0)
        {
            perror("accept() has failed");
            exit(-1);
        }
        log_connection(&cl_addr);
        serve_client(client_fd);
        close_socket(client_fd);
    }
}


int main(int argc, char* argv[])
{
    int port = choose_port(argc, argv);
    int fd = create_server_socket(port);
    accept_loop(fd);
    close_socket(fd);
    return 0;
}

