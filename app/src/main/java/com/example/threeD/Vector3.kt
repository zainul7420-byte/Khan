package com.example.threeD

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    operator fun plus(other: Vector3): Vector3 = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3): Vector3 = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float): Vector3 = Vector3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float): Vector3 = if (scalar != 0f) Vector3(x / scalar, y / scalar, z / scalar) else this

    fun dot(other: Vector3): Float = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3): Vector3 = Vector3(
        x = y * other.z - z * other.y,
        y = z * other.x - x * other.z,
        z = x * other.y - y * other.x
    )

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalized(): Vector3 {
        val len = length()
        return if (len > 0.00001f) this / len else Vector3(0f, 0f, 0f)
    }

    fun rotateX(degrees: Float): Vector3 {
        val rad = Math.toRadians(degrees.toDouble()).toFloat()
        val cosA = cos(rad)
        val sinA = sin(rad)
        return Vector3(
            x = x,
            y = y * cosA - z * sinA,
            z = y * sinA + z * cosA
        )
    }

    fun rotateY(degrees: Float): Vector3 {
        val rad = Math.toRadians(degrees.toDouble()).toFloat()
        val cosA = cos(rad)
        val sinA = sin(rad)
        return Vector3(
            x = x * cosA + z * sinA,
            y = y,
            z = -x * sinA + z * cosA
        )
    }

    fun rotateZ(degrees: Float): Vector3 {
        val rad = Math.toRadians(degrees.toDouble()).toFloat()
        val cosA = cos(rad)
        val sinA = sin(rad)
        return Vector3(
            x = x * cosA - y * sinA,
            y = x * sinA + y * cosA,
            z = z
        )
    }

    companion object {
        val ZERO = Vector3(0f, 0f, 0f)
        val UP = Vector3(0f, 1f, 0f)
        val FORWARD = Vector3(0f, 0f, 1f)
        val RIGHT = Vector3(1f, 0f, 0f)
    }
}
