import org.json.JSONArray
import org.json.JSONObject
import processing.core.PApplet
import processing.core.PVector
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

// this runs the actual game
class GameManager(val app: App) {

    // vars to aid decoupling of logic and rendering
    var lag: Long = 0
    var previous: Long = System.currentTimeMillis()

    // we will divide the screen into vertical strips to aid placement
    var gridSize = 0f

    var gameObjects = mutableListOf<GameObject>()

    val booletPool = ObjectPool(app) {
        Boolet(selfGenerated = true)
    }

    // TODO: use
    var uuidToObject = mutableMapOf<UUID, GameObject>()

    val bullets = mutableListOf<Boolet>()
    val otherPlayers = mutableListOf<Player>()

    val TIME_BETWEEN_PACKET_UPDATE = 2
    var timeLastPacket = System.currentTimeMillis()

    var bulletsToNetwork: Boolet? = null

    // run this at the beginning of the game
    fun setup() {

        // set some dimensions
        this.gridSize = app.displayWidth / 9f

        // // create some players
        // repeat(5) {
        //     val pos = PVector(app.random(1f), app.random(1f)).mult(900f)
        //     otherPlayers.add(Player(selfGenerated=true, pos = PVector(pos.x, terrainHeight(pos.x, pos.y), pos.y)))
        // }

        app.textFont(app.createFont("Courier 10 Pitch", 28f))

        // play a tune
//        if (!app.audio.music.isPlaying)
//            app.audio.music()
    }

    // handle key presses in here
    fun keyPressed() {
        val k = app.key.code
        player.controller?.keyPress(k)
    }

    fun keyReleased() {
        val k = app.key.code
        player.controller?.keyRelease(k)
    }

    // delete stuff that goes offscreen
    private fun boundsCheck() {
        // this needs to purge shit
//        gameObjects.removeIf { it.pos.y>app.displayHeight }
    }

    // add to the score
    fun addPoints(points: Int) {
//        score += waves.scoreMultiplier()*points
    }

    var collisions: MutableList<List<UUID>> = mutableListOf()

    // check for and handle any collisions
    private fun handleCollisions() {
        collisions.forEach { ids ->
            // handle each ent in here
            val isDeath = ids.any { val obj = uuidToObject[it]; obj is Boolet }

            if (isDeath) {
                println("isDeath happpppened")
                ids.forEach {
                    val obj = uuidToObject[it];
                    when (obj) {
                        is Boolet -> {
                            bullets.remove(obj)
                            booletPool.returnObject(obj)
                        }

                        is Player -> {
                            println("oof ouch owie player")
                            obj.lives--
                            if (obj.lives == 0) {
                                otherPlayers.remove(obj)
                                if (obj == player) exitProcess(0)
                            }
                        }
                    }
                }
            }
//             it.forEach { id ->
//                 val obj = uuidToObject[id]
//                 if (obj is Player) {
//                     println("a player")
//                 }
//                 if (obj is Boolet) {
//                     println("a boolet")
//                 }
//             }
        }
        collisions.clear()
    }

    // the player entity
    val player = Player(selfGenerated = true, pos = PVector(70f, 0f, 120f))
    val MVMT_SPEED = 0.06f

    // game over check
    fun gameOver(): Boolean = false

    fun Player.handleActions() {
        if (controller == null) {
            return
        }
        Action.values().forEach {
            if (controller.actions[it.ordinal]) {
                handleAction(it)
            }
        }
    }

    // how
    fun Player.handleAction(action: Action) {
//        println("actio $action")
        when (action) {
            Action.LOOK_LEFT -> {
                rotation -= 0.001f
            }

            Action.LOOK_RIGHT -> {
                rotation += 0.001f
            }

            // TODO: make this add a unit vector in each component, then normalise and mul by speed
            Action.FORWARD -> {
                pos.add(look.mult(MVMT_SPEED))
            }

            Action.BACKWARD -> {
                pos.sub(look.mult(MVMT_SPEED))
            }

            Action.RIGHT -> {
                pos.add(look.cross(PVector(0f, 1f, 0f)).mult(MVMT_SPEED))
            }

            Action.LEFT -> {
                pos.sub(look.cross(PVector(0f, 1f, 0f)).mult(MVMT_SPEED))
            }


            Action.PEW -> {
                if (isOnCooldown) return

                val boolet = booletPool.getObject().also {
                    it.pos.set(PVector.add(pos, look.mult(50f)))
                    it.vel.set(look.mult(0.3f))
                    it.expiresAt = System.currentTimeMillis() + BOOLET_LIFETIME
                    uuidToObject[it.uuid] = it
                }

                bullets.add(boolet)
                bulletsToNetwork = boolet
                cooldownEndsAt = System.currentTimeMillis() + BOOLET_COOLDOWN
            }
        }
    }

    fun Player.ai() {
        controller?.setAction(Action.FORWARD, true)

        val rng = app.random(1f)
//        println(rng)
        when {
            (rng < 0.001f) -> {
                controller?.setAction(Action.LOOK_LEFT, true)
                controller?.setAction(Action.LOOK_RIGHT, false)
            }

            (rng > 0.999f) -> {
                controller?.setAction(Action.LOOK_RIGHT, true)
                controller?.setAction(Action.LOOK_LEFT, false)
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


        // yeet off-screen stuff (todo: if arsed)
        boundsCheck()

        // AI!
        otherPlayers.forEach {
            it.ai()
        }

        player.handleActions()
        otherPlayers.forEach {
            it.handleActions()
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

        // check for and handle collisions
        handleCollisions()

        if (System.currentTimeMillis() >= timeLastPacket + TIME_BETWEEN_PACKET_UPDATE && app.ENABLE_MULTIPLAYER) {
            timeLastPacket = System.currentTimeMillis()
            // tx state and rx state
            // tx
            //val tx_buffer = ("{" +
            //        "\"events\": [], \"objects\": {\"${app.player_uuid}\": {\"x\": ${player.pos.x}, \"y\": ${player.pos.y}," +
            //        "\"rot\": ${player.rotation}, \"o_type\": \"PLAYER\"" +
            //        "}}}").toByteArray()

            // this is wack


            var events = if (bulletsToNetwork != null)
                "[{\"bullet_spawn\": {\"uuid\": \"${bulletsToNetwork!!.uuid}\" }}]"
            else "[]"
            bulletsToNetwork = null

            val objectData = mutableMapOf<UUID, Map<String, Float>>()
            objectData.putAll(bullets.map {
                it.uuid to mapOf(
                    "x" to it.pos.x,
                    "y" to it.pos.z,
                    "rot" to 1f,
                    "o_type" to 1f
                )
            })
            objectData.put(
                app.player_uuid, mapOf(
                    "x" to player.pos.x,
                    "y" to player.pos.z,
                    "rot" to player.rotation,
                    "o_type" to 0f
                )
            )
            val tx_json = JSONObject(
                mapOf(
                    "events" to events,
                    "objects" to objectData
                )
            )

            val tx_buffer = tx_json.toString().toByteArray()
            val tx_packet =
                DatagramPacket(tx_buffer, tx_buffer.size, InetAddress.getByName(app.HOST), app.server_udp_port)
            app.tx_udp_socket.send(tx_packet)
            // rx
            try {
                val rx_buffer = ByteArray(4096)
                val rx_packet = DatagramPacket(rx_buffer, rx_buffer.size)
                app.server_udp_socket.receive(rx_packet)
                val rx  = JSONObject(String(rx_packet.data))

                // THISSSS is for events
                val rx_events: JSONArray = rx["events"] as JSONArray

                println("rx $rx_events")
                // for each event
                rx_events.forEach {
                    it as JSONObject

                    if (it.keySet().contains("bullet_spawn")) {
                        println("bullet Spawn!!!")
                        val bullet_spawn: JSONObject = it.get("bullet_spawn") as JSONObject
                        val bullet_uuid = UUID.fromString(bullet_spawn.get("uuid").toString())
                        val b = Boolet(
                            false, PVector(1231f, 0f, 123f), 123f, PVector(), uuid = bullet_uuid,
                            expiresAt = System.currentTimeMillis() + BOOLET_LIFETIME
                        )
                        bullets.add(b)
                        uuidToObject[bullet_uuid] = b
                    }
                    if (it.keySet().contains("collision")) {
                        // get list
                        val collision_data: JSONArray = it.get("collision") as JSONArray
                        collisions = collision_data.map {
                            (it as JSONArray).map { UUID.fromString((it.toString())) }.toList()
                        }.toMutableList()
                    }
                }

                // THIS is for objects
                val os: JSONObject = rx["objects"] as JSONObject
                val iter: Iterator<String> = os.keys()
                while (iter.hasNext()) {
                    val key = UUID.fromString(iter.next())
                    val value: JSONObject = os.get(key.toString()) as JSONObject

                    val x = value["x"].toString().toFloat()
                    val y = value["y"].toString().toFloat()
                    val rotation = value["rot"].toString().toFloat()

                    val p: GameObject = uuidToObject[key] ?: continue
                    if (!p.selfGenerated) {
                        p.pos.x = x
                        p.pos.z = y
                        p.rotation = rotation
                    }
                }

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
            noStroke()
            // draw bg
            background(0f, 200f, 255f)

            // world stuff goes here
            val boxSize = 30f
            pushMatrix()

            player.pos.y = terrainHeight(player.pos.x, player.pos.z) - boxSize
            otherPlayers.forEach { it.pos.y = terrainHeight(it.pos.x, it.pos.z) - boxSize }

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


            directionalLight(200f, 200f, 200f, 0.5f, 1f, 0.2f)
            ambientLight(55f, 55f, 55f)

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

            // draw other players
            otherPlayers.forEach {
                push()
                translate(it.pos.x, it.pos.y - boxSize / 2f, it.pos.z)
                rotateY(-it.rotation)
                fill(color(255f, 255f, 0f))
                box(35f)
                pop()
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
            noStroke()
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

    // game loop driver fun
    var setup = true
    fun draw() {

        if (app.ENABLE_MULTIPLAYER && setup) {
            // multiplayer doooodads
            app.server_udp_socket.setSoTimeout(1)

            // init tcp connection to server
            // connect to magic number port and host over TCP
            app.server_tcp_socket = Socket(app.HOST, app.HOST_TCP_PORT)
            // send magic room number to server
            app.server_tcp_socket.outputStream.write(("{\"room_id\":${app.ROOM_ID}}").toByteArray())
            // wait... get udp port number to open a tx socket to
            val scanner = Scanner(app.server_tcp_socket.getInputStream())
            var msg = ""
            while (scanner.hasNextLine()) {
                msg = scanner.nextLine()
                break
            }
            val json_msg = JSONObject(msg)
            app.server_udp_port = json_msg["server_port"] as Int
            app.player_uuid = UUID.fromString(json_msg["player"] as String)

            // open local udp socket and connect to remove server udp port
            val client_udp_port = app.server_udp_socket.localPort
            // send our port to server
            app.server_tcp_socket.outputStream.write(("{\"port\":$client_udp_port}").toByteArray())

            // read initial state and set players
            while (setup) {

                try {
                    val rx_buffer = ByteArray(4096)
                    val rx_packet = DatagramPacket(rx_buffer, rx_buffer.size)
                    app.server_udp_socket.receive(rx_packet)
                    val rx = JSONObject(String(rx_packet.data))

                    if (!rx.isEmpty) {
                        setup = false
                    }
                    val os: JSONObject = rx["objects"] as JSONObject
                    val iter: Iterator<String> = os.keys()
                    while (iter.hasNext()) {
                        val key = UUID.fromString(iter.next())
                        val value: JSONObject = os.get(key.toString()) as JSONObject

                        val x = value["x"].toString().toFloat()
                        val y = value["y"].toString().toFloat()
                        val rotation = value["rot"].toString().toFloat()

                        if (key == app.player_uuid) {
                            player.uuid = key
                            player.pos.x = x
                            player.pos.z = y
                            player.rotation = rotation
                        } else {
                            // create new players
                            val p = Player(
                                PVector(x, 0f, y),
                                rotation,
                                null,
                                key,
                                selfGenerated = false
                            )
                            otherPlayers.add(p)
                            uuidToObject[key] = p
                        }
                    }

                    // TODO: update other players + bullets positions
                    // TODO: handle events (create bullets)

                } catch (_: SocketTimeoutException) {
                }
            }
        } else {

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
}

const val MS_PER_UPDATE = 1
