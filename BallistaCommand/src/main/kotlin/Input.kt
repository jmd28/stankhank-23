data class Input(
    val keysDown: BooleanArray = BooleanArray(256) { false },
    val bindings: Map<Int, Action> = mapOf(
        'q'.code to Action.LOOK_LEFT,
        'e'.code to Action.LOOK_RIGHT,
        'w'.code to Action.FORWARD,
        's'.code to Action.BACKWARD,
        'a'.code to Action.LEFT,
        'd'.code to Action.RIGHT,
        'm'.code to Action.PEW,
    ),
    val actions: BooleanArray = BooleanArray(Action.values().size) { false }
) {

    private fun getAction(keyCode: Int) = bindings[keyCode]

    fun keyPress(k: Int) {
        println(bindings.keys)
        println(k)
        keysDown[k] = true
        val action = bindings[k]
        println("fire action $action")
        if (action != null) {
            actions[action.ordinal] = true
        }
    }

    fun keyRelease(k: Int) {
        keysDown[k] = false
        val action = getAction(k)
        println("end action $action")
        if (action != null) {
            actions[action.ordinal] = false
        }
    }

}

// actions go in here
enum class Action {
    LOOK_LEFT,
    LOOK_RIGHT,
    FORWARD,
    BACKWARD,
    LEFT,
    RIGHT,
    PEW,
}