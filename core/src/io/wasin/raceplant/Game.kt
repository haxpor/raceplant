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

        // only this time to check for controller
        // if user plug in controller after this then they have to restart the game
        setupFirstActiveController()

        // set to begin with Play state
        gsm.pushState(GameStateManager.PLAY)
    }

    private fun setupFirstActiveController() {
        if (Controllers.getControllers().count() > 0) {
            val bbInputProcessor = Gdx.input.inputProcessor as BBInputProcessor
            val controller = Controllers.getControllers().first()
            controller.addListener(bbInputProcessor)
            bbInputProcessor.setActiveController(controller)
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
