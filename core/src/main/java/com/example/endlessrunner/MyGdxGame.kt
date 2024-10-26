package com.example.endlessrunner

import com.badlogic.gdx.Game

class MyGdxGame : Game() {

    var gameScreen = GameScreen(this)

    override fun create() {
        setScreen(MainMenuScreen(this))
    }
}
