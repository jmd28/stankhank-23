/**
 * this represents the state of the app
 * a finite state machine of sorts,
 * defining the next state given a previous state
 */
enum class GAMESTATE {
    PREGAME,
    PREWAVE,
    WAVE,
    ENDWAVE,
    GAMEOVER;

    // a progression between states
    fun next() = when (this) {
        PREGAME -> PREWAVE
        PREWAVE -> WAVE
        WAVE -> ENDWAVE
        ENDWAVE -> PREWAVE
        GAMEOVER -> PREGAME
    }
}