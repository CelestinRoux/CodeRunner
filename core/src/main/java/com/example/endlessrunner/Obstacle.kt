package com.example.endlessrunner

import com.badlogic.gdx.math.Rectangle

class Obstacle(var x: Float, var y: Float, val width: Float, val height: Float) {
    fun getRectangle(): Rectangle {
        return Rectangle(x, y, width, height)
    }
}
