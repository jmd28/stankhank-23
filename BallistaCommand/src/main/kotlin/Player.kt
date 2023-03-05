import processing.core.PVector

data class Player(
    override val pos: PVector,
    // where the thing is looking
    var rotation: Float = 0f,
    val controller: Input = Input(),

): GameObject {

    val look: PVector
        get() {
            val look = PVector.fromAngle(rotation)
            // rotate it to point the right way
            look.z = look.y
            look.y = 0f
            return look
        }

}