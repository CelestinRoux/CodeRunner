package com.example.endlessrunner

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

class MainMenuScreen(val game: MyGdxGame) : Screen {

    private lateinit var batch: SpriteBatch
    private lateinit var titleFont: BitmapFont
    private lateinit var buttonFont: BitmapFont
    private val glyphLayout = GlyphLayout()

    override fun show() {
        batch = SpriteBatch()

        val generator = FreeTypeFontGenerator(Gdx.files.internal("fancake/Fancake.ttf"))
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
        parameter.size = 100
        titleFont = generator.generateFont(parameter)
        parameter.size = 75
        buttonFont = generator.generateFont(parameter)

    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()

        val lineHeight = glyphLayout.height
        val boxX = Gdx.graphics.width / 2 - 200
        var boxY = Gdx.graphics.height - 800
        val boxWidth = 400
        val boxHeight = 200

        glyphLayout.setText(titleFont, "Endless Runner Game")
        var textX = boxX + (boxWidth - glyphLayout.width) / 2f
        var textY = boxY + boxHeight / 2f + lineHeight / 2f
        titleFont.draw(batch, glyphLayout, textX, textY)
        titleFont.draw(batch, glyphLayout, textX, textY)

        boxY = Gdx.graphics.height / 2 - 400
        glyphLayout.setText(buttonFont, "Start Game")
        textX = boxX + (boxWidth - glyphLayout.width) / 2f
        textY = boxY + boxHeight / 2f + lineHeight / 2f
        buttonFont.draw(batch, glyphLayout, textX, textY)
        buttonFont.draw(batch, glyphLayout, textX, textY)

        batch.end()

        if (Gdx.input.isTouched) {
            game.setScreen(GameScreen(game))
        }
    }

    override fun resize(width: Int, height: Int) {}

    override fun pause() {}

    override fun resume() {}

    override fun hide() {}

    override fun dispose() {
        batch.dispose()
        titleFont.dispose()
        buttonFont.dispose()
    }
}
