import ddf.minim.AudioPlayer
import processing.core.PApplet

import ddf.minim.Minim

enum class SFX {
    EXPLOSION,
    BALLISTA_FIRE,
    BOMBER_APPEAR,
    BALLISTA_DIE,
    CITY_DIE,
    BEGIN_WAVE,
    END_WAVE
}

// store all the audio so we don't keep reading from disk
class Audio (val app: PApplet) {

    val minim = Minim(app)

//    val explosion = minim.loadFile("assets/retrosfx/expl.wav")
//    val ballistaFire = minim.loadFile("assets/retrosfx/ballista.wav")
//    val bomberAppear = minim.loadFile("assets/retrosfx/alert1.wav")
//    val ballistaDead = minim.loadFile("assets/sfx/ono.wav")
//    val beginsfx = minim.loadFile("assets/sfx/newwave.wav")
//    val endsfx = minim.loadFile("assets/sfx/donewave.wav")
//    val cityDie = minim.loadFile("assets/sfx/screaming.wav")
//    val music = minim.loadFile( "assets/mus/mingus.wav")

    init {
//        explosion.gain = -5f
    }

    // kills minim
    fun stop() {
//        explosion.close()
//        ballistaFire.close()
//        bomberAppear.close()
//        ballistaDead.close()
//        beginsfx.close()
//        endsfx.close()
//        cityDie.close()
//        music.close()
        minim.stop()
    }

    fun play(sfx: SFX) {
//        val player = when (sfx) {
//            SFX.EXPLOSION -> explosion
//            SFX.BALLISTA_FIRE -> ballistaFire
//            SFX.BOMBER_APPEAR -> bomberAppear
//            SFX.BALLISTA_DIE -> ballistaDead
//            SFX.CITY_DIE -> cityDie
//            SFX.BEGIN_WAVE -> beginsfx
//            SFX.END_WAVE -> endsfx
//        }
//
//        player.play()
//        player.rewind()
    }

    fun music() {
//        music.amp(1f)
//        music.loop()
    }


}