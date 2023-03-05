### Start Listening for Connections on known port (give room id) ###
import random
### On Connection, if no room id exist, create new room and wait for 2 players ###

### on both players connect, create udp sockets and reply with their udp port to connect to ###

### handle events

import socket
from cmath import pi
from ssl import SOL_SOCKET
import threading
import time
import json
from enum import Enum
import uuid

# TODO: might need to handle rotation in collision code

HOST = "pc7-114-l.cs.st-andrews.ac.uk"
TCP_PORT = 21450
ENCODING = "utf-8"

rooms = {}  # id: {client_a: {}, client_b: {}}
rooms_lock = threading.Lock()

GAME_WIDTH = 1600
GAME_HEIGHT = 1000

PLAYER_HEIGHT = 10
PLAYER_WIDTH = 10

BULLET_HEIGHT = 5
BULLET_WIDTH = 5


# each player has a tcp socket, udp port, ip address
class Player:
    def __init__(self, tcp_socket, udp_port, ip_addr):
        self.tcp_socket = tcp_socket
        self.udp_port = udp_port
        self.ip_addr = ip_addr


class ObjectType(Enum):
    PLAYER = "PLAYER"
    BULLET = "BULLET"


class EventTypes(Enum):
    BULLET_SPAWN = "bullet_spawn"


class Object:
    def __init__(self, x, y, rot, o_type):
        self.x = x
        self.y = y
        self.rot = rot
        self.o_type = o_type


def rx_json(socket):
    while True:
        data = socket.recv(4096).decode(ENCODING).rstrip('\x00').rstrip('\n')
        if len(data) > 0:
            return json.loads(data)


def tx_json_tcp(socket, json_data):
    msg = bytes(json.dumps(json_data) + "\n", ENCODING)
    socket.sendall(msg)


def tx_json_udp(socket, address, port, json_data):
    msg = bytes(json.dumps(json_data) + "\n", ENCODING)
    socket.sendto(msg, (address, port))


def rx_json_udp(socket):
    data, addr = socket.recvfrom(4096)
    data = data.decode(ENCODING).rstrip('\x00').rstrip('\n')
    print(data)
    json_data = json.loads(data)
    json_data['addr'] = addr
    return json_data


def thread_on_new_client(client_socket, addr):
    # wait for room number
    data = rx_json(client_socket)
    room_id = data['room_id']

    # determine if room_id already exists
    rooms_lock.acquire()

    if room_id not in rooms:
        print("on_new_client: player 1 joined room_id " + str(room_id))
        rooms[room_id] = [{"socket": client_socket, "addr": addr}]
        room_thread = threading.Thread(target=thread_new_room, args=(room_id,))
        room_thread.start()
    else:
        # if room full don't allow it
        if len(rooms[room_id]) == 6:
            msg = "ERROR: woah cow person... room " + str(room_id) + " already full, idiot.\n"
            client_socket.send(msg.encode(ENCODING))
            client_socket.close()
        else:
            print("on_new_client: player 2 joined room_id " + str(room_id))
            rooms[room_id].append({"socket": client_socket, "addr": addr})

    rooms_lock.release()


# handles a game
def thread_new_room(room_id):
    # wait 20s before starting game
    time.sleep(2)

    # room has array of sockets and ip addr, one for each player
    room = rooms[room_id]
    player_count = len(room)

    print("new_room: " + str(player_count) + " clients on room_id = " + str(room_id))

    # player_a = room[0]
    # player_b = room[1]
    player_sockets = []
    ips = []
    uuids = []
    for player in room:
        player_sockets.append(player["socket"])
        ips.append(player["addr"][0])
        u = uuid.uuid4()
        uuids.append(str(u))
    # socket_a = player_a["socket"]
    # ip_addr_a = player_a["addr"][0]
    # socket_b = player_b["socket"]
    # ip_addr_b = player_b["addr"][0]

    # start rx udp socket and send port to clients
    rx_udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    rx_udp_socket.bind((HOST, 0))
    port = rx_udp_socket.getsockname()[1]

    # send server UDP ports to clients
    for i, p_socket in enumerate(player_sockets):
        print(str(uuids[i]))
        data = {"server_port": port, "player": str(uuids[i])}
        tx_json_tcp(p_socket, data)

    # data_a = {"server_port": port,"player":1}
    # data_b = {"server_port": port,"player":2}

    # tx_json_tcp(socket_a, data_a)
    # tx_json_tcp(socket_b, data_b)

    # get tx UDP ports from clients
    # also get player object UUID, which we use for their entity and logical id for this server
    player_udp_ports = []
    for p_socket in player_sockets:
        r = rx_json(p_socket)
        player_udp_ports.append(r["port"])
    # player_a_udp_port = rx_json(socket_a)["port"]
    # player_b_udp_port = rx_json(socket_b)["port"]

    # now we have enough data to build player structures
    # use these to send packets to players
    players = {}
    for i in range(player_count):
        tcp_socket = player_sockets[i]
        udp_port = player_udp_ports[i]
        ip_addr = ips[i]
        players[uuids[i]] = Player(tcp_socket, udp_port, ip_addr)

    print("new_room: server_port = " + str(port))
    for i in range(player_count):
        print("new_room: client_a_udp = " + ips[i] + ":" + str(player_udp_ports[i]))

    # print("new_room: client_a_udp = " + ip_addr_a + ":" + str(player_a_udp_port))
    # print("new_room: client_b_udp = " + ip_addr_b + ":" + str(player_b_udp_port))

    # open tx sockets
    # NEW: actually this can just be one socket!
    tx_udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    # send initial state

    # state now includes player lives

    # server assigns initial position to players
    # UUID -> Object
    objects = {}
    events = []
    for p_uuid in players.keys():
        o = Object(
            x=random.randrange(0, GAME_WIDTH),
            y=random.randrange(0, GAME_HEIGHT),
            o_type=ObjectType.PLAYER,
            rot=random.randrange(0, int(2*pi))
        )
        objects[p_uuid] = o

    state = {
        "events": events,
        "objects": objects
    }

    """
    STATE
    {
        "events": [{"bulletSpawn": {"uuid": uuid, "object": object}}]
        "objects": {"id": {x, y, w, h} }
    }
    
    """

    # busy loop
    # player_a_state = {}
    # player_b_state = {}
    # player_a_update = False
    # player_b_update = False
    timeSinceLastCollision = time.time_ns()
    while True:
        if time.time_ns() > timeSinceLastCollision + 5e6: # 5 ,ms
            timeSinceLastCollision = time.time_ns()
            
        # read in packets every ms
        # every 5ms, compute and resolve collisions
        PACKET_COLLECT_DELAY_NS = 1e6
        # read in packets for like 1ms
        start_t = time.time_ns()  # ns
        while time.time_ns() < start_t + PACKET_COLLECT_DELAY_NS:
            packet_data = rx_json_udp(rx_udp_socket)
            # contains objects [], events []
            # update objects via uuid with given objects
            rx_os = packet_data["objects"]
            for o_uuid in rx_os.keys():
                objects[o_uuid] = rx_os[o_uuid]

            rx_es = packet_data["events"]
            for event in rx_es:
                for key, value in event.items():
                    if key == EventTypes.BULLET_SPAWN:
                        events.append(event)
                        # need to create new object
                        bullet_uuid = value['uuid']
                        o = value['object']
                        objects[bullet_uuid] = o
                        # event will also be sent to players such that they can spawn the bullet

        # send updated state to all players
        for player in players.values():
            tx_json_udp(tx_udp_socket, player.ip_addr, player.udp_port, state)
        events = []

        # print("handle rx: " + packet_data)
        # if packet_data['addr'][0] == ip_addr_a:
        #    player_a_update = True
        #    player_a_state = packet_data
        # else:
        #    player_b_update = True
        #    player_b_state = packet_data

        # if player_a_update and player_b_update:
        #    # send state to other player
        #    player_a_update = False
        #    player_b_update = False
        #    tx_json_udp(tx_udp_socket_a, ip_addr_a, player_a_udp_port, player_b_state)
        #    tx_json_udp(tx_udp_socket_b, ip_addr_b, player_b_udp_port, player_a_state)


def start_server():
    print("Starting Server...")
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((HOST, TCP_PORT))
        s.listen()

        while True:
            conn, addr = s.accept()
            thread = threading.Thread(target=thread_on_new_client, args=(conn, addr))
            thread.start()


if __name__ == "__main__":
    start_server()
