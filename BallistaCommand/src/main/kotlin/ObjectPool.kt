import java.util.ArrayDeque

/*
 * reuse explosion objects to make performance less horrible
 *
 * could abstract this into ObjectPool<T> but not necessary yet
 */
class ObjectPool<T>(val app: App, val size: Int = 20, val initialiser: () -> T) {

    val pool = ArrayDeque<T>(size)

    init {
        repeat(size) { pool.add(
            initialiser()
        ) }
    }

    fun getObject(): T {
        println(pool.size)
        return pool.pollFirst() ?: initialiser()
    }

//    fun getExplosion(pos: PVector): Explosion {
//        val it = getExplosion()
//        it.pos.x = pos.x
//        it.pos.y = pos.y
//        it.boom()
//        return it
//    }

    fun returnObject(e: T) {
//        e.reset()
        pool.add(e)
    }
}