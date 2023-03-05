import processing.core.PVector
import java.util.UUID

// these behave badly because they're const
// should probs be static val instead
const val MAX_LIVES = 3
const val BOOLET_COOLDOWN = 500 // ms

data class Player(
    override val pos: PVector,
    // where the thing is looking
    var rotation: Float = 0f,
    val controller: Input = Input(),
    val uuid: UUID = UUID.randomUUID(),
    var lives: Int = MAX_LIVES,
    // time after which next boolet allowed to fire
    var cooldownEndsAt: Long = 0,

): GameObject {

    val isOnCooldown: Boolean
        get() = System.currentTimeMillis() < cooldownEndsAt

    val look: PVector
        get() {
            val look = PVector.fromAngle(rotation)
            // rotate it to point the right way
            look.z = look.y
            look.y = 0f
            return look
        }

}