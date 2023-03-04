import processing.core.PApplet

/**
 * Quadtree implementation translated from this example:
 * https://gamedevelopment.tutsplus.com/tutorials/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space--gamedev-374
 *
 * accessed 27/01/2022
 */


class Quadtree(private val level: Int, pBounds: BoundingBox) {

    private val MAX_OBJECTS = 4
    private val MAX_LEVELS = 5
    private val objects: MutableList<Collidable>
    private val bounds: BoundingBox
    private val nodes: Array<Quadtree?>

    /*
     * Constructor
     */
    init {
        objects = ArrayList()
        bounds = pBounds
        nodes = arrayOfNulls(4)
    }

    /*
     * Clears the quadtree
     */
    fun clear() {
        objects.clear()
        for (i in nodes.indices) {
            if (nodes[i] != null) {
                nodes[i]?.clear()
                nodes[i] = null
            }
        }
    }

    /*
     * Splits the node into 4 subnodes
     */
    private fun split() {
        val subWidth = (bounds.w / 2)
        val subHeight = (bounds.h / 2)
        val x = bounds.x
        val y = bounds.y
        nodes[0] = Quadtree(level + 1, BoundingBox(x + subWidth, y, subWidth, subHeight))
        nodes[1] = Quadtree(level + 1, BoundingBox(x, y, subWidth, subHeight))
        nodes[2] = Quadtree(level + 1, BoundingBox(x, y + subHeight, subWidth, subHeight))
        nodes[3] = Quadtree(level + 1, BoundingBox(x + subWidth, y + subHeight, subWidth, subHeight))
    }

    /*
     * Determine which node the object belongs to. -1 means
     * object cannot completely fit within a child node and is part
     * of the parent node
     */
    private fun getIndex(obj: Collidable): Int {

        var index = -1
        val verticalMidpoint: Double = bounds.x + (bounds.w / 2.0)
        val horizontalMidpoint: Double = bounds.y + (bounds.h / 2.0)

        // Object can completely fit within the top quadrants
        val topQuadrant = obj.bounds.y < horizontalMidpoint && obj.bounds.y + obj.bounds.h < horizontalMidpoint
        // Object can completely fit within the bottom quadrants
        val bottomQuadrant: Boolean = obj.bounds.y > horizontalMidpoint

        // Object can completely fit within the left quadrants
        if (obj.bounds.x < verticalMidpoint && obj.bounds.x + obj.bounds.w < verticalMidpoint) {
            if (topQuadrant) {
                index = 1
            } else if (bottomQuadrant) {
                index = 2
            }
        } else if (obj.bounds.x > verticalMidpoint) {
            if (topQuadrant) {
                index = 0
            } else if (bottomQuadrant) {
                index = 3
            }
        }
        return index
    }


    /*
     * Insert the object into the quadtree. If the node
     * exceeds the capacity, it will split and add all
     * objects to their corresponding nodes.
     */
    fun insert(obj: Collidable) {
        if (nodes[0] != null) {
            val index = getIndex(obj)
            if (index != -1) {
                nodes[index]!!.insert(obj)
                return
            }
        }
        objects.add(obj)
        if (objects.size > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes[0] == null) {
                split()
            }
            var i = 0
            while (i < objects.size) {
                val index = getIndex(objects[i])
                if (index != -1) {
                    nodes[index]!!.insert(objects.removeAt(i))
                } else {
                    i++
                }
            }
        }
    }

    /*
     * Return all objects that could collide with the given object
     */
    fun retrieve(returnObjects: MutableList<Collidable>, obj: Collidable): List<*> {
        val index = getIndex(obj)
        if (index != -1 && nodes[0] != null) {
            nodes[index]?.retrieve(returnObjects, obj)
        }
        returnObjects.addAll(objects)
        return returnObjects
    }

    fun draw(app: PApplet) {
        app.stroke(255)
        app.noFill()
        app.rect(bounds.x.toFloat(), bounds.y.toFloat(), bounds.w.toFloat(), bounds.h.toFloat())
        nodes.forEach { it?.draw(app) }
    }
}