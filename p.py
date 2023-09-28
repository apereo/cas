import socket

def get_host_ip():
    return socket.gethostbyname(socket.gethostname())

if __name__ == "__main__":
    host_ip = get_host_ip()
    print(f"Host IP Address: {host_ip}")
