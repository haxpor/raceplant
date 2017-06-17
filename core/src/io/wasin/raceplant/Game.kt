package io.wasin.raceplant

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.wasin.raceplant.handlers.*

class Game : ApplicationAdapter() {

    lateinit var sb: SpriteBatch
        private set
    lateinit var gsm: GameStateManager
        private set
    lateinit var playerSaveFileManager: PlayerSaveFileManager
        private set

    companion object {
        const val TITLE = "Race Plant"
        const val V_WIDTH = 320f
        const val V_HEIGHT = 240f
        const val SCALE = 2

        var res: Content = Content()
            private set
    }

    override fun create() {

        Gdx.input.inputProcessor = BBInputProcessor()

        sb = SpriteBatch()

        gsm = GameStateManager(this)

        // create player's savefile manager with pre-set of savefile's path
        playerSaveFileManager = PlayerSaveFileManager(Settings.PLAYER_SAVEFILE_RELATIVE_PATH)

        // TODO: Load resource here...
        res.loadTexture("ui/separator.png", "separator")
        res.loadTexture("ui/player1.png", "player1")
        res.loadTexture("ui/player2.png", "player2")
        res.loadTexture("ui/seed.png", "seed")
        res.loadTexture("ui/scorehud.png", "scorehud")
        res.loadTexture("ui/damage-ball.png", "damageball")
        res.loadTexture("ui/tree.png", "tree")
        res.loadTexture("ui/bucket.png", "bucket")
        res.loadTexture("ui/waterdrop-effect.png", "waterdrop")

        // set up both controllers
        // WARNING: You have to have 2 controllers connected before the launch of the game
        // to be able to recognize both of controllers
        // if something went wrong just restart the game
        setupTwoControllers()

        // set to begin with Play state
        gsm.pushState(GameStateManager.PLAY)
    }

    private fun setupTwoControllers() {
        val ccount = Controllers.getControllers().count()
        if (ccount == 2) {
            val bbInputProcessor = Gdx.input.inputProcessor as BBInputProcessor

            val firstController = Controllers.getControllers().first()
            firstController.addListener(bbInputProcessor)
            BBInput.controller1 = firstController

            val secondController = Controllers.getControllers().last()
            secondController.addListener(bbInputProcessor)
            BBInput.controller2 = secondController
        }
        else if (ccount == 1) {
            val bbInputProcessor = Gdx.input.inputProcessor as BBInputProcessor

            val firstController = Controllers.getControllers().first()
            firstController.addListener(bbInputProcessor)
            BBInput.controller1 = firstController
        }
    }

    override fun render() {
        Gdx.graphics.setTitle(TITLE + " -- FPS: " + Gdx.graphics.framesPerSecond)
        gsm.update(Gdx.graphics.deltaTime)
        gsm.render()
        BBInput.update()
    }

    override fun dispose() {
    }

    override fun resize(width: Int, height: Int) {
        gsm.resize(width, height)
    }
}
