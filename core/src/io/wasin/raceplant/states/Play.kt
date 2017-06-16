package io.wasin.raceplant.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapRenderer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.wasin.raceplant.Game
import io.wasin.raceplant.handlers.BBInput
import io.wasin.raceplant.handlers.GameStateManager

/**
 * Created by haxpor on 6/16/17.
 */
class Play(gsm: GameStateManager): GameState(gsm){

    private var tilemap: TiledMap
    private var tmr: TiledMapRenderer
    private var tileSize: Float

    lateinit private var player1Cam: OrthographicCamera
    lateinit private var player1Viewport: ExtendViewport

    init {
        tilemap = TmxMapLoader().load("maps/mapA.tmx")
        tmr = OrthogonalTiledMapRenderer(tilemap)
        tileSize = tilemap.properties.get("tilewidth", Float::class.java)

        setupPlayer1Camera()
        setupPlayer2Camera()
    }

    private fun setupPlayer1Camera() {
        player1Cam = OrthographicCamera()
        player1Cam.setToOrtho(false, Game.V_WIDTH/2, Game.V_HEIGHT)
        player1Cam.translate(tileSize/2, 0f)
        player1Cam.update()

        player1Viewport = ExtendViewport(Game.V_WIDTH/2, Game.V_HEIGHT, player1Cam)
    }

    private fun setupPlayer2Camera() {

    }

    override fun handleInput() {
    }

    override fun update(dt: Float) {
        handleInput()

    }

    override fun render() {
        // clear screen
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // draw player 1
        sb.projectionMatrix = player1Cam.combined
        sb.begin()

        Gdx.gl.glViewport( 0,0,Gdx.graphics.width/2, Gdx.graphics.height );
        tmr.setView(player1Cam)
        tmr.render()

        sb.end()

        // draw player 2
    }

    override fun dispose() {

    }

    override fun resize_user(width: Int, height: Int) {

    }
}