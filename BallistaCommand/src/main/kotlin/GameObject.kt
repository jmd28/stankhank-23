import processing.core.PVector

interface GameObject {

    // the centre of the object
    val pos: PVector
    var rotation: Float
    val selfGenerated: Boolean

//    fun draw()

}