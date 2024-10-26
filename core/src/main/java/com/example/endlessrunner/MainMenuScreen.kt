package com.example.endlessrunner

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import javax.swing.plaf.basic.BasicSliderUI.ActionScroller

class MainMenuScreen(val game: MyGdxGame) : Screen {

    private lateinit var batch: SpriteBatch
    private lateinit var titleFont: BitmapFont
    private lateinit var buttonFont: BitmapFont
    private val glyphLayout = GlyphLayout()
    private lateinit var stage: Stage
    private lateinit var textButtonStyle: TextButtonStyle
    private lateinit var button: TextButton
    private lateinit var blinkAction : RepeatAction

    override fun show() {
        batch = SpriteBatch()

        val generator = FreeTypeFontGenerator(Gdx.files.internal("fancake/Fancake.ttf"))
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
        parameter.size = 100
        titleFont = generator.generateFont(parameter)
        parameter.size = 75
        buttonFont = generator.generateFont(parameter)



        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        textButtonStyle = TextButtonStyle().apply {
            font = buttonFont
            fontColor = Color.WHITE
        }

        button = TextButton("Start Game", textButtonStyle)

        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.setScreen(GameScreen(game))
            }
        })


        blinkAction = Actions.forever(
            Actions.sequence(
                Actions.run { button.style.fontColor = Color.YELLOW },
                Actions.delay(0.5f),
                Actions.run { button.style.fontColor = Color.WHITE },
                Actions.delay(0.5f)
            )
        )
        button.addAction(blinkAction)
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

        glyphLayout.setText(titleFont, "Code Runner")
        var textX = boxX + (boxWidth - glyphLayout.width) / 2f
        var textY = boxY + boxHeight / 2f + lineHeight / 2f
        titleFont.draw(batch, glyphLayout, textX, textY)
        glyphLayout.setText(titleFont, "Escape The Virus")
        textX = boxX + (boxWidth - glyphLayout.width) / 2f
        textY = boxY + boxHeight / 2f + lineHeight / 2f - 125f
        titleFont.draw(batch, glyphLayout, textX, textY)

        button.setPosition(Gdx.graphics.width / 2f - button.width / 2, Gdx.graphics.height / 2f - button.height / 2 - 300)
        stage.addActor(button)

        batch.end()

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun pause() {}

    override fun resume() {}

    override fun hide() {}

    override fun dispose() {
        batch.dispose()
        titleFont.dispose()
        stage.dispose()
    }
}
