package com.example.myapplication3.location.kmpluscalc.data

data class Envelope(
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double
) {
    override fun toString(): String = "<Envelope> {minX: $minX, minY: $minY, maxX: $maxX, maxY: $maxY}"

    /**
     * Returns `true` if this `Envelope` is a "null"
     * envelope.
     *
     * @return    `true` if this `Envelope` is uninitialized
     * or is the envelope of the empty geometry.
     */
    fun isNull(): Boolean {
        return maxX < minX
    }

    /**
     * Tests if the region defined by `other`
     * intersects the region of this `Envelope`.
     *
     * @param  other  the `Envelope` which this `Envelope` is
     * being checked for intersecting
     * @return        `true` if the `Envelope`s intersect
     */
    fun intersects(other: Envelope): Boolean {
        return if (isNull() || other.isNull()) {
            false
        } else !(other.minX > maxX || other.maxX < minX || other.minY > maxY || other.maxY < minY)
    }
}
