package com.example.endlessrunner

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle

class GameScreen(val game: MyGdxGame) : Screen {
    private var batch: SpriteBatch? = null

    lateinit var shapeRenderer: ShapeRenderer
    lateinit var player: Rectangle

    lateinit var backgroundTexture: Texture
    var backgroundY1 = 0f
    var backgroundY2 = 0f

    private val obstacles = mutableListOf<Obstacle>()
    private var obstacleTimer = 0f
    private val obstacleSpawnTime = 2f

    private var isGameOver = false
    private lateinit var gameOverFont: BitmapFont
    private val glyphLayout = GlyphLayout()

    override fun show() {
        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()

        backgroundTexture = Texture(Gdx.files.internal("background.png"))

        player = Rectangle(Gdx.graphics.width / 2f - 37.5f, 200f, 75f, 175f)

        backgroundY1 = 0f
        backgroundY2 = backgroundTexture.height.toFloat()

        obstacleTimer = 0f

        val generator = FreeTypeFontGenerator(Gdx.files.internal("fancake/Fancake.ttf"))
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
        parameter.size = 50
        gameOverFont = generator.generateFont(parameter)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        if (!isGameOver) {
            backgroundY1 -= 300 * delta
            backgroundY2 -= 300 * delta

            if (backgroundY1 + backgroundTexture.height <= 0) {
                backgroundY1 = backgroundY2 + backgroundTexture.height
            }
            if (backgroundY2 + backgroundTexture.height <= 0) {
                backgroundY2 = backgroundY1 + backgroundTexture.height
            }

            batch?.begin()
            batch?.draw(backgroundTexture, 0f, backgroundY1, Gdx.graphics.width.toFloat(), backgroundTexture.height.toFloat())
            batch?.draw(backgroundTexture, 0f, backgroundY2, Gdx.graphics.width.toFloat(), backgroundTexture.height.toFloat())
            batch?.end()

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.setColor(Color.CYAN)
            shapeRenderer.rect(player.x, player.y, player.width, player.height)
            shapeRenderer.end()

            handlePlayerInput()

            obstacleTimer += delta
            if (obstacleTimer > obstacleSpawnTime) {
                spawnObstacle()
                obstacleTimer = 0f
            }

            updateObstacles()
            drawObstacles()

            checkCollisions()

        } else {
            displayGameOverMessage()

            if (Gdx.input.justTouched()) {
                restartGame()
            }
        }
    }

    override fun resize(width: Int, height: Int) {}

    override fun pause() {}

    override fun resume() {}

    override fun hide() {}

    private fun handlePlayerInput() {
        if (Gdx.input.isTouched()) {
            val touchX = Gdx.input.x.toFloat()

            player.x = touchX - player.width / 2

            player.x = MathUtils.clamp(player.x, 100f, Gdx.graphics.width - player.width - 100f)
        }
    }

    private fun spawnObstacle() {
        val width = 175f
        val height = 75f
        val x = MathUtils.random(100f, Gdx.graphics.width - width - 100f)
        val y = Gdx.graphics.height.toFloat()
        obstacles.add(Obstacle(x, y, width, height))
    }

    private fun updateObstacles() {
        for (obstacle in obstacles) {
            obstacle.y -= 300 * Gdx.graphics.deltaTime
        }

        obstacles.removeAll { it.y + it.height < 0 }
    }

    private fun drawObstacles() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.RED
        for (obstacle in obstacles) {
            shapeRenderer.rect(obstacle.x, obstacle.y, obstacle.width, obstacle.height)
        }
        shapeRenderer.end()
    }

    private fun checkCollisions() {
        for (obstacle in obstacles) {
            if (player.overlaps(obstacle.getRectangle())) {
                isGameOver = true
                println("Collision détectée ! Game Over.")
            }
        }
    }

    private fun displayGameOverMessage() {
        batch?.begin()

        val lineHeight = glyphLayout.height
        val boxX = Gdx.graphics.width / 2 - 200
        var boxY = Gdx.graphics.height / 2 - 100
        val boxWidth = 400
        val boxHeight = 200

        glyphLayout.setText(gameOverFont, "Game Over!")
        var textX = boxX + (boxWidth - glyphLayout.width) / 2f
        var textY = boxY + boxHeight / 2f + lineHeight / 2f
        gameOverFont.draw(batch, glyphLayout, textX, textY)

        boxY = Gdx.graphics.height / 2 - 200
        glyphLayout.setText(gameOverFont, "Tap to Restart")
        textX = boxX + (boxWidth - glyphLayout.width) / 2f
        textY = boxY + boxHeight / 2f + lineHeight / 2f
        gameOverFont.draw(batch, glyphLayout, textX, textY)

        batch?.end()
    }

    private fun restartGame() {
        isGameOver = false
        obstacles.clear()
        player.setPosition(Gdx.graphics.width / 2f - 37.5f, 200f)
        obstacleTimer = 0f
    }

    override fun dispose() {
        batch!!.dispose()
        backgroundTexture.dispose()
    }
}
