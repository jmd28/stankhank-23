import com.jogamp.newt.opengl.GLWindow

import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics
import processing.core.PImage
import processing.event.KeyEvent

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
        game.keyPressed()
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