import org.json.JSONObject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import processing.core.PVector
import processing.event.KeyEvent
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.*

/**
 * The driver defining the flow of the game,
 * handling input, render control, etc
 */
class App : PApplet() {

    lateinit var hudView: PGraphics
    lateinit var gameView: PGraphics

    // initialise app state
    var state = GAMESTATE.PREGAME

    // initialise game
    var game = GameManager(this)

    // multiplayer doodads

    val ENABLE_MULTIPLAYER = false
    var server_udp_port: Int = -1
    val server_udp_socket = DatagramSocket()
    val tx_udp_socket = DatagramSocket()
    val HOST = "pc7-114-l.cs.st-andrews.ac.uk"
    val HOST_TCP_PORT = 21450
    val ROOM_ID = 1337
    lateinit var server_tcp_socket: Socket
    lateinit var player_uuid: UUID

    lateinit var audio: Audio

    companion object Factory {
        // create an instance of the game and run it
        fun run() {
            val instance = App()
            instance.runSketch()
        }
    }

    override fun setup() {
        frameRate(120f)
        audio = Audio(this)
        textAlign(PConstants.LEFT)
        game.setup()

        // HUD layer
        hudView = createGraphics(width, height, P2D)
        // Game view
        gameView = createGraphics(width, height, P3D)

        if (ENABLE_MULTIPLAYER) {
            // multiplayer doooodads
            server_udp_socket.setSoTimeout(1)

            // init tcp connection to server
            // connect to magic number port and host over TCP
            server_tcp_socket = Socket(HOST, HOST_TCP_PORT)
            // send magic room number to server
            server_tcp_socket.outputStream.write(("{\"room_id\":$ROOM_ID}").toByteArray())
            // wait... get udp port number to open a tx socket to
            val scanner = Scanner(server_tcp_socket.getInputStream())
            var msg = ""
            while (scanner.hasNextLine()) {
                msg = scanner.nextLine()
                break
            }
            val json_msg = JSONObject(msg)
            server_udp_port = json_msg["server_port"] as Int
            player_uuid = UUID.fromString(json_msg["player"] as String)
            println(player_uuid)

            // open local udp socket and connect to remove server udp port
            val client_udp_port = server_udp_socket.localPort
            // send our port to server
            server_tcp_socket.outputStream.write(("{\"port\":$client_udp_port}").toByteArray())

            // read initial state and set players
            try {
                val rx_buffer = ByteArray(4096)
                val rx_packet = DatagramPacket(rx_buffer, rx_buffer.size)
                server_udp_socket.receive(rx_packet)
                val rx: JSONObject = JSONObject(String(rx_packet.data))

                val os: JSONObject = rx["objects"] as JSONObject
                val iter: Iterator<String> = os.keys()
                while (iter.hasNext()) {
                    val key = UUID.fromString(iter.next())
                    val value: JSONObject = os.get(key.toString()) as JSONObject

                    val x = value["x"].toString().toFloat()
                    val y = value["y"].toString().toFloat()
                    val rotation = value["rot"].toString().toFloat()

                    if (key == player_uuid) {
                        game.player.uuid = key
                        game.player.pos.x = x
                        game.player.pos.y = y
                        game.player.rotation = rotation
                    } else {
                        // create new players
                        game.otherPlayers.add(Player(
                            PVector(x, y),
                            rotation,
                            null,
                            key,
                            selfGenerated = false
                        ))
                    }
                }

                // TODO: update other players + bullets positions
                // TODO: handle events (create bullets)

            } catch (_: SocketTimeoutException) {
            }
        }

    }

    override fun settings() {
        fullScreen(P3D)
    }


//    override fun mousePressed() {
//        when (state) {
//            GAMESTATE.WAVE -> game.mousePressed()
//            else -> advanceGame()
//        }
//    }

    fun advanceGame() {
        state = state.next()

//        if (state == GAMESTATE.WAVE)
//            game.waves.beginWave()
    }

    override fun keyPressed(event: KeyEvent?) {
        try {
            game.keyPressed()
        } catch (_: Exception) {
        }
    }

    override fun keyReleased() {
        try {
            game.keyReleased()
        } catch (_: Exception) {
        }
    }

    // game loop driver fun
    override fun draw() {
        game.draw()
    }

    override fun stop() {
        audio.stop()
        super.stop()
    }

}