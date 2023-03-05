import org.json.JSONArray
import org.json.JSONObject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PConstants.P2D
import processing.core.PConstants.P3D
import processing.core.PVector
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.UUID
import kotlin.math.PI

// this runs the actual game
class GameManager(val app: App) {

    // vars to aid decoupling of logic and rendering
    var lag: Long = 0
    var previous: Long = System.currentTimeMillis()

    // we will divide the screen into vertical strips to aid placement
    var gridSize = 0f

    var gameObjects = mutableListOf<GameObject>()

    val booletPool = ObjectPool(app) {
        Boolet()
    }

    // TODO: use
    var uuidToObject = mutableMapOf<UUID, GameObject>()

    val bullets = mutableListOf<Boolet>()

    // use these lists where concurrent modification might get funky
    // objects to add after a trip through the game loop
    private var toAdd = mutableListOf<GameObject>()

    // objects to remove after a trip through the game loop
    private var toRemove = mutableListOf<GameObject>()

    val TIME_BETWEEN_PACKET_UPDATE = 100
    var timeLastPacket = System.currentTimeMillis()

    // run this at the beginning of the game
    fun setup() {

        // set some dimensions
        this.gridSize = app.displayWidth / 9f


        // add initial set of entities
//        with(gameObjects) {
//            clear()
//            add(
//                Player()
//            )
//        }

        app.textFont(app.createFont("Courier 10 Pitch", 28f))

        // play a tune
//        if (!app.audio.music.isPlaying)
//            app.audio.music()
    }

    fun addObject(obj: GameObject) {
        toAdd.add(obj)
    }

    fun removeObject(obj: GameObject) {
        // explosions go back into the pool
//        if (obj is Explosion) {
//            explosions.returnExplosion(obj)
//        }
        // other stuff is less costly to create, can be destroyed
//        toRemove.add(obj)
    }

    val input = Input()

    // handle key presses in here
    fun keyPressed() {
        val k = app.key.code
        input.keyPress(k)
    }

    fun keyReleased() {
        val k = app.key.code
        input.keyRelease(k)
    }

    // demonstration man tf2
    fun mousePressed() {
        when (app.mouseButton) {
//            PApplet.LEFT -> fire()
//            PApplet.RIGHT -> det()
        }
    }

    // delete stuff that goes offscreen
    private fun boundsCheck() {
        // this needs to purge shit
//        gameObjects.removeIf { it.pos.y>app.displayHeight }
    }

    private fun physics() {

        // forces
//        gameObjects.filterIsInstance<Physicsable>().forEach {
//            it.applyPhysics()
//        }
    }

    // add to the score
    fun addPoints(points: Int) {
//        score += waves.scoreMultiplier()*points
    }

    // check for and handle any collisions
    private fun handleCollisions() {

        // objects we need to check against for collisions

    }

    // the player entity
    val player = Player(pos = PVector(70f, 0f, 120f))
    val MVMT_SPEED = 0.06f

    // game over check
    fun gameOver(): Boolean = false

    fun handleAction(action: Action) {
        when (action) {
            Action.LOOK_LEFT -> {
                player.rotation -= 0.002f
            }

            Action.LOOK_RIGHT -> {
                player.rotation += 0.002f
            }

            // TODO: make this add a unit vector in each component, then normalise and mul by speed
            Action.FORWARD -> {
                player.pos.add(player.look.mult(MVMT_SPEED))
            }

            Action.BACKWARD -> {
                player.pos.sub(player.look.mult(MVMT_SPEED))
            }

            Action.RIGHT -> {
                player.pos.add(player.look.cross(PVector(0f, 1f, 0f)).mult(MVMT_SPEED))
            }

            Action.LEFT -> {
                player.pos.sub(player.look.cross(PVector(0f, 1f, 0f)).mult(MVMT_SPEED))
            }

            Action.PEW -> {
                val boolet = booletPool.getObject()
                boolet.pos.set(PVector.add(player.pos, player.look.mult(5f)))
                boolet.vel.set(player.look.mult(0.3f))
                boolet.expiresAt = System.currentTimeMillis() + BOOLET_LIFETIME
                bullets.add(boolet)
            }
        }
    }

    // update game state each tick
    private fun update(dt: Long) {

        // gameover check
        if (gameOver()) {
            app.state = GAMESTATE.GAMEOVER
            // tear down previous game
            setup()
            return
        }


        // yeet off-screen stuff
        boundsCheck()

        //spawn meteorites
//        waves.spawnEnemies()

        // TODO: handle mouse better than I managed (use awt robot)
//        val mouseDelta = (app.mouseX - app.pmouseX)
//        val mouseDelta = app.deltaMouse
////        println(mouseDelta)
//        println(mouseDelta)
//        player.rotation += (mouseDelta*0.0008f)


        // handle input actions here, this should go in its own bit
        Action.values().forEach {
            if (input.actions[it.ordinal]) {
                // handle it
                println("handle $it")
                handleAction(it)
            }
        }

        // update bullets
        val toRemove = mutableListOf<Boolet>()
        bullets.forEach {
            it.pos.add(it.vel)
            it.pos.y = terrainHeight(it.pos.x, it.pos.z) - 40f
            if (System.currentTimeMillis() > it.expiresAt) {
                booletPool.returnObject(it)
                toRemove.add(it)
            }
        }

        bullets.removeAll(toRemove)

        // apply forces to everything that cares
//        physics()

        // check for and handle collisions
        handleCollisions()

        // ballista cooldown, explosions growing etc.
//        gameObjects.filterIsInstance<Updateable>().forEach { it.update() }

        // update entities list with any changes
//        gameObjects.removeAll(toRemove)
//        gameObjects.addAll(toAdd)

        // wipe entity lists
//        toAdd.clear()
//        toRemove.clear()

        // broadcast objects + events (if any) to server
        // get player pos
        // get bullet pos'
        // smash into json string


        if (System.currentTimeMillis() >= timeLastPacket + TIME_BETWEEN_PACKET_UPDATE && app.ENABLE_MULTIPLAYER) {
            timeLastPacket = System.currentTimeMillis()
            // tx state and rx state
            // tx
            //val tx_buffer = ("{" +
            //        "\"events\": [], \"objects\": {\"${app.player_uuid}\": {\"x\": ${player.pos.x}, \"y\": ${player.pos.y}," +
            //        "\"rot\": ${player.rotation}, \"o_type\": \"PLAYER\"" +
            //        "}}}").toByteArray()

            val tx_json = JSONObject(mapOf(
                "events" to JSONArray(),
                "objects" to mapOf(
                    app.player_uuid to mapOf(
                        "x" to player.pos.x,
                        "y" to player.pos.y,
                        "pos" to player.rotation
                    )
                )
            ))

            val tx_buffer = tx_json.toString().toByteArray()
            val tx_packet = DatagramPacket(tx_buffer, tx_buffer.size, InetAddress.getByName(app.HOST), app.server_udp_port)
            app.tx_udp_socket.send(tx_packet)
            // rx
            try {
                val rx_buffer = ByteArray(4096)
                val rx_packet = DatagramPacket(rx_buffer, rx_buffer.size)
                app.server_udp_socket.receive(rx_packet)
                val rx = JSONObject(String(rx_packet.data))
                // TODO: update other players + bullets positions
                // TODO: handle events (create bullets)

            } catch (_: SocketTimeoutException) {
            }
        }


    }

    val noiseCoeff = 0.01f
    fun terrainHeight(x: Float, y: Float) = -100 * app.noise(x * noiseCoeff, y * noiseCoeff)


    // draw all the things
    fun render() {

        with(app.gameView) {

            beginDraw()


//            noClip()
            noStroke()
            // draw bg
            background(0f, 200f, 255f)

            // world stuff goes here


//            noFill()
//            lights()f)

            val boxSize = 30f
            pushMatrix()

            player.pos.y = terrainHeight(player.pos.x, player.pos.z) - boxSize
            val cameraPos = player.pos.copy()
            // move up (in line with player head)
            cameraPos.y -= 75f
            val lookAt = PVector.add(cameraPos, player.look)

            camera(
                // camera location
                cameraPos.x, cameraPos.y, cameraPos.z,
                // where it's pointing
                lookAt.x, lookAt.y, lookAt.z,
                // which way is up
                0f, 1f, 0f
            )

//            clip(app.mouseX.toFloat(), app.mouseY.toFloat(), 100f, 100f)

//            lights()
            ambient(0.1f)
            directionalLight(255f, 255f, 255f, 0.5f, 1f, 0.2f)

            // draw a world
            val worldSize = 100

            // draw the world with boxes
//            (0..9999).forEach {
//                pushMatrix()
//                    val x = (it / worldSize) * boxSize
//                    val y = (it % worldSize) * boxSize
//                    translate(x , terrainHeight(x, y) - boxSize / 2, y)
//                    box(boxSize)
//                popMatrix()
//            }
            fun terrainVertex(x: Int, z: Int) {
                val x = x * 30f
                val z = z * 30f
                vertex(x, terrainHeight(x, z) - boxSize, z)
            }

            // draw the world as a mesh of triangles
            (0..30).forEach { i ->
                (0..30).forEach { j ->
                    beginShape()
                    terrainVertex(i, j)
                    terrainVertex(i + 1, j)
                    terrainVertex(i, j + 1)
                    endShape()

                    beginShape()
                    terrainVertex(i + 1, j)
                    terrainVertex(i + 1, j + 1)
                    terrainVertex(i, j + 1)
                    endShape()
                }
            }

            // draw projectiles
            bullets.forEach {
                push()
                translate(it.pos.x, it.pos.y, it.pos.z)
                fill(color(255f, 0f, 255f))
                sphere(10f)
                pop()
            }

            popMatrix()

            endDraw()
        }

        with(app.hudView) {
            beginDraw()

            // hud stuff goes here

            rect(0f, height * .9f, width.toFloat(), height * .1f)
            // xhair
            stroke(40)
            line(width / 2f, 0f, width / 2f, height.toFloat())
            line(0f, height / 2f, width.toFloat(), height / 2f)


            text("FPS ${app.frameRate}", 10f, 90f)

            endDraw()
        }

        with(app) {
            image(gameView, 0f, 0f)

            pushStyle()
            tint(255f, 255f, 255f, 100f)
            image(hudView, 0f, 0f)
            popStyle()
        }

    }

    // the illusion of control
    fun pause() {

    }

    fun resume() {
        previous = System.currentTimeMillis()
    }

    // game loop driver fun
    fun draw() {

        val current: Long = System.currentTimeMillis()
        val dt: Long = current - previous
        previous = current
        lag += dt

        // processInput()
        while (lag >= MS_PER_UPDATE) {
            // update model here
            update(dt)
            lag -= MS_PER_UPDATE
        }

        // render
        render()
    }

}

const val MS_PER_UPDATE = 1
