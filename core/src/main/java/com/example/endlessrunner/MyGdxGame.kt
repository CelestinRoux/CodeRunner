package com.example.endlessrunner

import com.badlogic.gdx.Game

class MyGdxGame : Game() {
    override fun create() {
        setScreen(MainMenuScreen(this))
    }
}
