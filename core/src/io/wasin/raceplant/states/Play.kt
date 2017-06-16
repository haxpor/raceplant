package io.wasin.raceplant.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.TextureRegion
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

    lateinit private var player2Cam: OrthographicCamera
    lateinit private var player2Viewport: ExtendViewport

    private var separatorTextureRegion: TextureRegion

    init {
        tilemap = TmxMapLoader().load("maps/mapA.tmx")
        tmr = OrthogonalTiledMapRenderer(tilemap)
        tileSize = tilemap.properties.get("tilewidth", Float::class.java)

        val tex = Game.res.getTexture("separator")!!
        separatorTextureRegion = TextureRegion(tex, tex.width, tex.height)

        setupPlayer1Camera()
        setupPlayer2Camera()
    }

    private fun setupPlayer1Camera() {
        player1Cam = OrthographicCamera()
        player1Cam.setToOrtho(false, Game.V_WIDTH/2, Game.V_HEIGHT)
        player1Cam.update()

        player1Viewport = ExtendViewport(Game.V_WIDTH/2, Game.V_HEIGHT, player1Cam)
    }

    private fun setupPlayer2Camera() {
        player2Cam = OrthographicCamera()
        player2Cam.setToOrtho(false, Game.V_WIDTH/2, Game.V_HEIGHT)
        player2Cam.update()

        player2Viewport = ExtendViewport(Game.V_WIDTH/2, Game.V_HEIGHT, player2Cam)
    }

    override fun handleInput() {
    }

    override fun update(dt: Float) {
        handleInput()

    }

    override fun render() {
        // clear screen
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT)

        sb.projectionMatrix = player1Cam.combined
        sb.begin()

        // draw player 1
        Gdx.gl.glViewport(0,0,Gdx.graphics.width/2, Gdx.graphics.height)
        tmr.setView(player1Cam)
        tmr.render()

        // draw player 2
        Gdx.gl.glViewport(Gdx.graphics.width/2,0,Gdx.graphics.width/2, Gdx.graphics.height)
        tmr.setView(player2Cam)
        tmr.render()

        sb.end()

        // draw separator
        sb.projectionMatrix = hudCam.combined
        // set viewport back to normal
        Gdx.gl.glViewport(0,0,Gdx.graphics.width, Gdx.graphics.height)
        sb.begin()
        sb.draw(separatorTextureRegion, hudCam.viewportWidth/2-separatorTextureRegion.regionWidth/2, 0f)
        sb.end()
    }

    override fun dispose() {

    }

    override fun resize_user(width: Int, height: Int) {

    }
}