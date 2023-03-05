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

    val ENABLE_MULTIPLAYER = true
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