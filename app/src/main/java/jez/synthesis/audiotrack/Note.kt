package jez.synthesis.audiotrack

import kotlin.math.pow

enum class Note(private val stepsFromA: Int) {
    C(-9),
    CSharp(-8),
    D(-7),
    DSharp(-6),
    E(-5),
    F(-4),
    FSharp(-3),
    G(-2),
    GSharp(-1),
    A(0),
    ASharp(1),
    B(2),
    ;

    /**
     * Defaults to middle octave (middle-C etc)
     *
     * -4 is applied to the octave as middle A is the fourth octave
     */
    fun frequency(octave: Int, additionalSteps: Int = 0): Double =
        A4 * PitchConstant.pow(StepsPerOctave * (octave - 4) + stepsFromA + additionalSteps)

    companion object {
        private const val StepsPerOctave = 12
        private const val A4 = 432.0
        private val PitchConstant = 2.0.pow(1.0 / 12.0)
    }
}
