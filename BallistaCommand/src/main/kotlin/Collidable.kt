import processing.core.PApplet
import processing.core.PVector
import processing.core.PVector.dist

// provides a radius, a collision handler
// + a bounding box for the quadtree
interface Collidable: GameObject {

    // size of bounding box
    val bounds: BoundingBox

//    // assume all hit-boxes are circular for now
//    val r: Float

    // a function to compute the bounding box
    val updateBounds: () -> Unit

    fun drawBoundingBox(app: PApplet) {
        app.stroke(255f,0f,0f)
        app.noFill()
        app.rect(bounds.x.toFloat(), bounds.y.toFloat(), bounds.w.toFloat(), bounds.h.toFloat())
    }

    // a simple circle circle mesh collision detector
    // we probably don't need any others
//    fun circleCircle(other: Collidable): Boolean {
//        return dist(this.pos, other.pos) < (this.r + other.r)
//    }

    // collisions handler
    fun onCollision(other: Collidable)
}
data class BoundingBox(var x: Float, var y: Float, var w: Float, var h: Float) {

    // empty constructor
    constructor(): this(0f,0f,0f,0f)

    fun update(pos: PVector, r: Float) {
        this.x = pos.x - r
        this.y = pos.y - r
        this.w = 2 * r
        this.h = 2 * r
    }

}
