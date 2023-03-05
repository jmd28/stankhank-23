import com.jogamp.newt.opengl.GLWindow

import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import processing.core.PImage
import processing.data.JSONObject
import processing.event.KeyEvent
import java.lang.Exception
import java.net.DatagramSocket
import java.net.Socket
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

    // initialise end wave screen handler
//    val endWave = EndWave(this)

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
            val json_msg = org.json.JSONObject(msg)
            server_udp_port = json_msg["server_port"] as Int
            player_uuid = UUID.fromString(json_msg["player"] as String)
            println(player_uuid)

            // open local udp socket and connect to remove server udp port
            val client_udp_port = server_udp_socket.localPort
            // send our port to server
            server_tcp_socket.outputStream.write(("{\"port\":$client_udp_port}").toByteArray())
        }

    }

    override fun settings() {
        fullScreen(P3D)
    }


    override fun mousePressed() {
        when (state) {
            GAMESTATE.WAVE -> game.mousePressed()
            else -> advanceGame()
        }
    }

    fun advanceGame() {
        state = state.next()

//        if (state == GAMESTATE.WAVE)
//            game.waves.beginWave()
    }

//    private fun landingScreen() {
//        background(0f,0f,0f)
//        fill(255)
//        text("${TITLE}\n\npress anything", displayWidth/2f, displayHeight/2f)
//    }
//
//    private fun prewaveScreen() {
//        background(0f,0f,0f)
//        fill(255)
////        text("wave ${game.waves.wave}", displayWidth/2f, displayHeight/2f)
//    }
//
//    private fun gameoverScreen() {
//        background(0f,0f,0f)
//        fill(255)
//        text("the end", displayWidth/2f, displayHeight/2f)
//        text("press anything", displayWidth/2f, displayHeight/5f*3)
//    }

    override fun keyPressed(event: KeyEvent?) {
        try {
            game.keyPressed()
        } catch (e: Exception) {
        }
    }

    override fun keyReleased() {
        game.keyReleased()
    }

//    var prevMouseX = 0
//    var mouseDelta = 0f
//
////    var mouseD2elta = 0f
////    var wouldBeNewX = 0
////    var wouldBeOldX = 0
//
//    override fun mouseMoved() {
//        prevMouseX = mouseX
//        super.mouseMoved()
//    }


    var oldMouseX = 0
    var deltaMouse = 0
    var mouseReset = 0
    // game loop driver fun
    override fun draw() {
//        val r = surface.native as GLWindow
//        r.confinePointer(true)
//        r.isPointerVisible = false

        game.draw()
        // calculate mouse X delta
//        deltaMouse = mouseX - oldMouseX
//        oldMouseX = mouseX
//
//        mouseReset++
//        // move mouse back to centre
//        if (mouseReset == 5) {
//            r.warpPointer(width / 2, height / 2)
//            mouseReset = 0
//            oldMouseX = width / 2
//        }

    }

    override fun stop() {
        audio.stop()
        super.stop()
    }

}