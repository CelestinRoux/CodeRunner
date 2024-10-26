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
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import kotlin.math.max

class GameScreen(val game: MyGdxGame) : Screen {
    private var batch: SpriteBatch? = null

    lateinit var shapeRenderer: ShapeRenderer
    lateinit var player: Rectangle
    private var playerBatch: SpriteBatch? = null
    private lateinit var playerTexture: Texture
    private var score: Int = 0
    private var scoreBatch: SpriteBatch? = null
    private var scoreTimer: Float = 0f
    private val scoreIncrementTime: Float = 1f
    private lateinit var scoreFont: BitmapFont

    lateinit var backgroundTexture: Texture
    var backgroundY1 = 0f
    var backgroundY2 = 0f

    private var scrollSpeed = 400f
    private var scoreThresholds = listOf(10, 25, 50, 100, 200, 300, 400, 500)
    private var speedIncreaseAmount = 100f
    private var timeElapsed = 0f
    private val speedIncreaseInterval = 60f
    private var currentSpeedIndex = 0
    private var speedIncreaseTimer = 0f
    private lateinit var speedIncreaseFont: BitmapFont

    private val obstacles = mutableListOf<Obstacle>()
    private var obstacleBatch: SpriteBatch? = null
    private lateinit var obstacleTexture: Texture
    private var obstacleTimer = 0f
    private var obstacleSpawnTime = 1.5f

    private var isGameOver = false
    private lateinit var gameOverFont: BitmapFont
    private lateinit var buttonFont: BitmapFont
    private lateinit var gameOverLabel: Label
    private val glyphLayout = GlyphLayout()
    private lateinit var stage: Stage
    private var gameOverOverlay = Group()
    private var pauseOverlay = Group()
    private lateinit var restartButton: TextButton
    private lateinit var resumeButton: TextButton
    private lateinit var quitButtonPause: TextButton
    private lateinit var quitButtonGameOver: TextButton
    private lateinit var pauseButton: TextButton
    private var isPaused = false
    private var isResume = false

    override fun show() {
        batch = SpriteBatch()
        scoreBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()

        backgroundTexture = Texture(Gdx.files.internal("background.png"))

        player = Rectangle(Gdx.graphics.width / 2f - 37.5f, 200f, 150f, 150f)
        playerTexture = Texture(Gdx.files.internal("Player.png"))
        playerBatch = SpriteBatch()

        backgroundY1 = 0f
        backgroundY2 = backgroundTexture.height.toFloat()

        obstacleTexture = Texture(Gdx.files.internal("Virus.png"))
        obstacleBatch = SpriteBatch()
        obstacleTimer = 0f

        val generator = FreeTypeFontGenerator(Gdx.files.internal("fancake/Fancake.ttf"))
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
        parameter.size = 75
        gameOverFont = generator.generateFont(parameter)
        scoreFont = generator.generateFont(parameter)
        parameter.size = 50
        buttonFont = generator.generateFont(parameter)
        parameter.size = 100
        speedIncreaseFont = generator.generateFont(parameter)

        stage = Stage(ScreenViewport())
        Gdx.input.inputProcessor = stage

        gameOverOverlay.isVisible = false
        pauseOverlay.isVisible = false
        val textButtonStyle = TextButton.TextButtonStyle().apply {
            font = buttonFont
            fontColor = Color.WHITE
        }

        val blinkAction = Actions.forever(
            Actions.sequence(
                Actions.run { textButtonStyle.fontColor = Color.YELLOW },
                Actions.delay(0.5f),
                Actions.run { textButtonStyle.fontColor = Color.WHITE },
                Actions.delay(0.5f)
            )
        )

        restartButton = TextButton("Restart", textButtonStyle)
        restartButton.setPosition(Gdx.graphics.width / 2f - restartButton.width / 2, Gdx.graphics.height / 2f)
        restartButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                restartGame()
            }
        })
        restartButton.addAction(blinkAction)

        quitButtonGameOver = TextButton("Quitter", textButtonStyle)
        quitButtonGameOver.setPosition(Gdx.graphics.width / 2f - quitButtonGameOver.width / 2, Gdx.graphics.height / 2f - 100)
        quitButtonGameOver.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.setScreen(MainMenuScreen(game))
            }
        })
        quitButtonGameOver.addAction(blinkAction)

        resumeButton = TextButton("Reprendre", textButtonStyle)
        resumeButton.setPosition(Gdx.graphics.width / 2f - resumeButton.width / 2, Gdx.graphics.height / 2f)
        resumeButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                isResume = true
                resume()
            }
        })
        resumeButton.addAction(blinkAction)

        quitButtonPause = TextButton("Quitter", textButtonStyle)
        quitButtonPause.setPosition(Gdx.graphics.width / 2f - quitButtonPause.width / 2, Gdx.graphics.height / 2f - 100)
        quitButtonPause.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.setScreen(MainMenuScreen(game))
            }
        })
        quitButtonPause.addAction(blinkAction)

        val gameOverLabelStyle = Label.LabelStyle().apply {
            font = gameOverFont
            fontColor = Color.WHITE
        }
        gameOverLabel = Label("Game Over!", gameOverLabelStyle)
        gameOverLabel.setPosition(Gdx.graphics.width / 2f - gameOverLabel.width / 2, Gdx.graphics.height - 600f)

        gameOverOverlay.addActor(gameOverLabel)
        gameOverOverlay.addActor(restartButton)
        gameOverOverlay.addActor(quitButtonGameOver)
        pauseOverlay.addActor(resumeButton)
        pauseOverlay.addActor(quitButtonPause)

        stage.addActor(pauseOverlay)
        stage.addActor(gameOverOverlay)

        val textButtonPauseStyle = TextButton.TextButtonStyle().apply {
            font = buttonFont
            fontColor = Color.WHITE
        }
        pauseButton = TextButton("Pause", textButtonPauseStyle)
        pauseButton.setPosition(Gdx.graphics.width - 200f, Gdx.graphics.height - 115f)
        pauseButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                pause()
                event?.stop()
            }
        })
        stage.addActor(pauseButton)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        if (!isGameOver && !isPaused) {
            backgroundY1 -= scrollSpeed * delta
            backgroundY2 -= scrollSpeed * delta

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

            playerBatch?.begin()
            playerBatch?.draw(playerTexture, player.x, player.y, player.width, player.height)

            handlePlayerInput()
            playerBatch?.end()

            obstacleTimer += delta
            if (obstacleTimer > obstacleSpawnTime) {
                spawnObstacle()
                obstacleTimer = 0f
            }

            updateObstacles()
            drawObstacles()

            checkCollisions()

            val scoreString = score.toString()
            val extraWidth = (scoreString.length - 1) * 35f + 300
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            Gdx.gl.glEnable(GL20.GL_BLEND)
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            shapeRenderer.color = Color(0f, 0f, 0f, 0.5f)
            shapeRenderer.rect(30f, Gdx.graphics.height - 130f, extraWidth, 100f)
            shapeRenderer.end()
            Gdx.gl.glDisable(GL20.GL_BLEND)

            scoreBatch?.begin()
            scoreFont.draw(scoreBatch, "Score: $score", 50f, Gdx.graphics.height - 50f)

            scoreTimer += delta
            if (scoreTimer >= scoreIncrementTime) {
                score += 1
                scoreTimer = 0f
            }

            val lineHeight = glyphLayout.height
            val boxX = Gdx.graphics.width / 2 - 200
            var boxY = Gdx.graphics.height - 300
            val boxWidth = 400
            val boxHeight = 200
            glyphLayout.setText(speedIncreaseFont, "Speed Increase!")
            var textX = boxX + (boxWidth - glyphLayout.width) / 2f
            var textY = boxY + boxHeight / 2f + lineHeight / 2f
            if (currentSpeedIndex < scoreThresholds.size && score == scoreThresholds[currentSpeedIndex]) {
                scrollSpeed += speedIncreaseAmount
                currentSpeedIndex++
                speedIncreaseFont.draw(scoreBatch, glyphLayout, textX, textY)
                speedIncreaseTimer = 2f
                if (currentSpeedIndex % 2 == 0){
                    obstacleSpawnTime = max(0.5f, obstacleSpawnTime - 0.5f)
                    println(obstacleSpawnTime)
                }
            }
            else{
                if (timeElapsed >= speedIncreaseInterval) {
                    scrollSpeed += speedIncreaseAmount
                    timeElapsed = 0f
                    speedIncreaseFont.draw(scoreBatch, glyphLayout, textX, textY)
                    speedIncreaseTimer = 2f
                }
            }
            if (speedIncreaseTimer > 0) {
                speedIncreaseTimer -= delta
                speedIncreaseFont.draw(scoreBatch, glyphLayout, textX, textY)
            }

            scoreBatch?.end()

        } else {
            if (isGameOver) {
                displayGameOverMessage()
            }
        }

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {}

    override fun pause() {
        if (!isPaused) {
            isPaused = true
            pauseOverlay.isVisible = true
        }
    }

    override fun resume() {
        if (isPaused && isResume) {
            isPaused = false
            pauseOverlay.isVisible = false
        }
    }

    override fun hide() {}

    private fun handlePlayerInput() {
        if (Gdx.input.isTouched()) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.input.y.toFloat()

            val screenY = Gdx.graphics.height - touchY
            if (screenY <= Gdx.graphics.height - 150f) {
                player.x = touchX - player.width / 2
                player.x = MathUtils.clamp(player.x, 100f, Gdx.graphics.width - player.width - 100f)
            }
        }
    }

    private fun spawnObstacle() {
        val width = 175f
        val height = 175f
        val x = MathUtils.random(100f, Gdx.graphics.width - width - 100f)
        val y = Gdx.graphics.height.toFloat()
        obstacles.add(Obstacle(x, y, width, height))
    }

    private fun updateObstacles() {
        for (obstacle in obstacles) {
            obstacle.y -= scrollSpeed * Gdx.graphics.deltaTime
        }
        obstacles.removeAll { it.y + it.height < 0 }
    }

    private fun drawObstacles() {
        playerBatch?.begin()
        for (obstacle in obstacles) {
            playerBatch?.draw(obstacleTexture, obstacle.x, obstacle.y, obstacle.width, obstacle.height)
        }
        playerBatch?.end()
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
        if (isGameOver) {
            gameOverOverlay.isVisible = true
            pauseButton.isVisible = false
        }
    }

    private fun restartGame() {
        isGameOver = false
        gameOverOverlay.isVisible = false
        pauseButton.isVisible = true
        obstacles.clear()
        player.setPosition(Gdx.graphics.width / 2f - 37.5f, 200f)
        obstacleTimer = 0f
        obstacleSpawnTime = 1.5f
        score = 0
        scoreTimer = 0f
        scrollSpeed = 400f
        currentSpeedIndex = 0
    }

    override fun dispose() {
        batch!!.dispose()
        backgroundTexture.dispose()
    }
}
