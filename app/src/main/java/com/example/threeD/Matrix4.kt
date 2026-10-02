package com.example.threeD

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class Matrix4 {
    val m = FloatArray(16)

    init {
        identity()
    }

    fun identity(): Matrix4 {
        for (i in 0 until 16) m[i] = 0f
        m[0] = 1f
        m[5] = 1f
        m[10] = 1f
        m[15] = 1f
        return this
    }

    fun multiply(other: Matrix4): Matrix4 {
        val result = FloatArray(16)
        for (row in 0 until 4) {
            for (col in 0 until 4) {
                var sum = 0f
                for (k in 0 until 4) {
                    sum += this.m[row * 4 + k] * other.m[k * 4 + col]
                }
                result[row * 4 + col] = sum
            }
        }
        System.arraycopy(result, 0, this.m, 0, 16)
        return this
    }

    fun transform(v: Vector3): Vector3 {
        val x = v.x * m[0] + v.y * m[1] + v.z * m[2] + m[3]
        val y = v.x * m[4] + v.y * m[5] + v.z * m[6] + m[7]
        val z = v.x * m[8] + v.y * m[9] + v.z * m[10] + m[11]
        val w = v.x * m[12] + v.y * m[13] + v.z * m[14] + m[15]
        return if (w != 0f && w != 1f) {
            Vector3(x / w, y / w, z / w)
        } else {
            Vector3(x, y, z)
        }
    }

    fun transformDirection(v: Vector3): Vector3 {
        val x = v.x * m[0] + v.y * m[1] + v.z * m[2]
        val y = v.x * m[4] + v.y * m[5] + v.z * m[6]
        val z = v.x * m[8] + v.y * m[9] + v.z * m[10]
        return Vector3(x, y, z)
    }

    companion object {
        fun translation(tx: Float, ty: Float, tz: Float): Matrix4 {
            val mat = Matrix4()
            mat.m[3] = tx
            mat.m[7] = ty
            mat.m[11] = tz
            return mat
        }

        fun rotationX(degrees: Float): Matrix4 {
            val mat = Matrix4()
            val rad = Math.toRadians(degrees.toDouble()).toFloat()
            val c = cos(rad)
            val s = sin(rad)
            mat.m[5] = c
            mat.m[6] = -s
            mat.m[9] = s
            mat.m[10] = c
            return mat
        }

        fun rotationY(degrees: Float): Matrix4 {
            val mat = Matrix4()
            val rad = Math.toRadians(degrees.toDouble()).toFloat()
            val c = cos(rad)
            val s = sin(rad)
            mat.m[0] = c
            mat.m[2] = s
            mat.m[8] = -s
            mat.m[10] = c
            return mat
        }

        fun rotationZ(degrees: Float): Matrix4 {
            val mat = Matrix4()
            val rad = Math.toRadians(degrees.toDouble()).toFloat()
            val c = cos(rad)
            val s = sin(rad)
            mat.m[0] = c
            mat.m[1] = -s
            mat.m[4] = s
            mat.m[5] = c
            return mat
        }

        fun lookAt(eye: Vector3, center: Vector3, up: Vector3): Matrix4 {
            val f = (center - eye).normalized()
            val s = f.cross(up).normalized()
            val u = s.cross(f)

            val mat = Matrix4()
            mat.m[0] = s.x
            mat.m[1] = s.y
            mat.m[2] = s.z
            mat.m[3] = -s.dot(eye)

            mat.m[4] = u.x
            mat.m[5] = u.y
            mat.m[6] = u.z
            mat.m[7] = -u.dot(eye)

            mat.m[8] = -f.x
            mat.m[9] = -f.y
            mat.m[10] = -f.z
            mat.m[11] = f.dot(eye)

            mat.m[12] = 0f
            mat.m[13] = 0f
            mat.m[14] = 0f
            mat.m[15] = 1f
            return mat
        }

        fun perspective(fovDegrees: Float, aspect: Float, near: Float, far: Float): Matrix4 {
            val mat = Matrix4()
            val fovRad = Math.toRadians(fovDegrees.toDouble()).toFloat()
            val tanHalfFov = tan(fovRad / 2f)

            mat.m[0] = 1f / (aspect * tanHalfFov)
            mat.m[5] = 1f / tanHalfFov
            mat.m[10] = -(far + near) / (far - near)
            mat.m[11] = -(2f * far * near) / (far - near)
            mat.m[14] = -1f
            mat.m[15] = 0f
            return mat
        }
    }
}
