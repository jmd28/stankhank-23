import processing.core.PVector
import java.util.UUID

const val BOOLET_LIFETIME = 5000L // ms

data class Boolet(
    override val selfGenerated: Boolean,
    override val pos: PVector = PVector(),
    override var rotation: Float = 0f,
    val vel: PVector = PVector(),
    val uuid: UUID = UUID.randomUUID(),
    var expiresAt: Long = BOOLET_LIFETIME,
    // where the thing is looking
//    var rotation: Float = 0f,

): GameObject {

//    fun reset() {
//    }
//    val look: PVector
//        get() {
//            val look = PVector.fromAngle(rotation)
//            // rotate it to point the right way
//            look.z = look.y
//            look.y = 0f
//            return look
//        }

}