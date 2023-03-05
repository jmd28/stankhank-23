import processing.core.PVector

const val BOOLET_LIFETIME = 5000L // ms

data class Boolet(
    override val pos: PVector = PVector(),
    val vel: PVector = PVector(),
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