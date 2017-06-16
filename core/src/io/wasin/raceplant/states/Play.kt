package io.wasin.raceplant.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.controllers.mappings.Xbox
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapRenderer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.wasin.raceplant.Game
import io.wasin.raceplant.entities.Player
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

    lateinit private var player1: Player
    lateinit private var player2: Player

    private var player1CamTargetPosition: Vector3 = Vector3.Zero
    private var player2CamTargetPosition: Vector3 = Vector3.Zero

    private var separatorTextureRegion: TextureRegion
    private var playerCameraUpdateRate: Float = 0.2f

    companion object {
        const val PLAYER_MOVE_SPEED = 50.0f
        const val PLAYER_CAM_AHEAD_OFFSET = 30f  // ahead distance to move player's camera at
        const val CONTROLLER_DEADZONE_VALUE = 0.3f
    }

    init {
        tilemap = TmxMapLoader().load("maps/mapA.tmx")
        tmr = OrthogonalTiledMapRenderer(tilemap)
        tileSize = tilemap.properties.get("tilewidth", Float::class.java)

        val tex = Game.res.getTexture("separator")!!
        separatorTextureRegion = TextureRegion(tex, tex.width, tex.height)

        setupPlayer1Camera()
        setupPlayer2Camera()

        setupPlayer1()
        setupPlayer2()

        // mocking up
        player1.x = 50f
        player1.y = 50f

        player1CamTargetPosition.set(player1.x, player1.y, 0f)

        player2.x = 100f
        player2.y = 100f
        player2CamTargetPosition.set(player2.x, player2.y, 0f)
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

    private fun setupPlayer1() {
        player1 = Player(1, Game.res.getTexture("player1")!!)
    }

    private fun setupPlayer2() {
        player2 = Player(2, Game.res.getTexture("player2")!!)
    }

    override fun handleInput(dt: Float) {
        handlePlayer1Input(dt)
        handlePlayer2Input(dt)
    }

    private fun handlePlayer1Input(dt: Float) {
        var triggeredToMove = false
        if (BBInput.isController1Down(BBInput.CONTROLLER_BUTTON_LEFT) ||
                (BBInput.controller1 != null && BBInput.controller1!!.getAxis(Xbox.L_STICK_HORIZONTAL_AXIS) < -CONTROLLER_DEADZONE_VALUE)) {
            // update player position
            player1.x -= PLAYER_MOVE_SPEED * dt
            // update camera position
            player1CamTargetPosition = calculateNewCameraPosition(player1, Vector2(-1f, 0f))
            // change to walk state
            player1.setState(Player.State.WALK)
            // set facing direction
            player1.faceLeft()
            triggeredToMove = true
        }

        if (BBInput.isController1Down(BBInput.CONTROLLER_BUTTON_RIGHT) ||
                (BBInput.controller1 != null && BBInput.controller1!!.getAxis(Xbox.L_STICK_HORIZONTAL_AXIS) > CONTROLLER_DEADZONE_VALUE)) {
            // update player position
            player1.x += PLAYER_MOVE_SPEED * dt
            // update camera position
            player1CamTargetPosition = calculateNewCameraPosition(player1, Vector2(1f, 0f))
            // change to walk state
            player1.setState(Player.State.WALK)
            // set facing direction
            player1.faceRight()
            triggeredToMove = true
        }

        if (BBInput.isController1Down(BBInput.CONTROLLER_BUTTON_UP) ||
                (BBInput.controller1 != null && BBInput.controller1!!.getAxis(Xbox.L_STICK_VERTICAL_AXIS) < -CONTROLLER_DEADZONE_VALUE)) {
            // update player position
            player1.y += PLAYER_MOVE_SPEED * dt
            // update camera position
            player1CamTargetPosition = calculateNewCameraPosition(player1, Vector2(0f, 1f))
            // change to walk state
            player1.setState(Player.State.WALK)
            triggeredToMove = true
        }

        if (BBInput.isController1Down(BBInput.CONTROLLER_BUTTON_DOWN) ||
                (BBInput.controller1 != null && BBInput.controller1!!.getAxis(Xbox.L_STICK_VERTICAL_AXIS) > CONTROLLER_DEADZONE_VALUE)) {
            // update player position
            player1.y -= PLAYER_MOVE_SPEED * dt
            // update camera position
            player1CamTargetPosition = calculateNewCameraPosition(player1, Vector2(0f, -1f))
            // change to walk state
            player1.setState(Player.State.WALK)
            triggeredToMove = true
        }

        // check if user didn't trigger to walk, then back to normal
        if (!triggeredToMove) {
            player1.setState(Player.State.IDLE)
        }
    }

    private fun handlePlayer2Input(dt: Float) {
        if (BBInput.isController2Down(BBInput.CONTROLLER_BUTTON_LEFT) ||
                (BBInput.controller2 != null && BBInput.controller2!!.getAxis(Xbox.L_STICK_HORIZONTAL_AXIS) < -CONTROLLER_DEADZONE_VALUE)) {
            // update player position
            player2.x -= PLAYER_MOVE_SPEED * dt
            // update camera position
            player2CamTargetPosition = calculateNewCameraPosition(player2, Vector2(-1f, 0f))
            // change to walk state
            player2.setState(Player.State.WALK)
        }

        if (BBInput.isController2Down(BBInput.CONTROLLER_BUTTON_RIGHT) ||
                (BBInput.controller2 != null && BBInput.controller2!!.getAxis(Xbox.L_STICK_HORIZONTAL_AXIS) > CONTROLLER_DEADZONE_VALUE)) {
            // update player position
            player2.x += PLAYER_MOVE_SPEED * dt
            // update camera position
            player2CamTargetPosition = calculateNewCameraPosition(player2, Vector2(1f, 0f))
            // change to walk state
            player2.setState(Player.State.WALK)
        }

        if (BBInput.isController2Down(BBInput.CONTROLLER_BUTTON_UP) ||
                (BBInput.controller2 != null && BBInput.controller2!!.getAxis(Xbox.L_STICK_VERTICAL_AXIS) < -CONTROLLER_DEADZONE_VALUE)) {
            // update player position
            player2.y += PLAYER_MOVE_SPEED * dt
            // update camera position
            player2CamTargetPosition = calculateNewCameraPosition(player2, Vector2(0f, 1f))
            // change to walk state
            player2.setState(Player.State.WALK)
        }

        if (BBInput.isController2Down(BBInput.CONTROLLER_BUTTON_DOWN) ||
                (BBInput.controller2 != null && BBInput.controller2!!.getAxis(Xbox.L_STICK_VERTICAL_AXIS) > CONTROLLER_DEADZONE_VALUE)) {
            // update player position
            player2.y -= PLAYER_MOVE_SPEED * dt
            // update camera position
            player2CamTargetPosition = calculateNewCameraPosition(player2, Vector2(0f, -1f))
            // change to walk state
            player2.setState(Player.State.WALK)
        }
    }

    override fun update(dt: Float) {
        handleInput(dt)

        // update players
        player1.update(dt)
        player2.update(dt)

        // update player 1 and 2 camera
        player1Cam.position.lerp(player1CamTargetPosition, playerCameraUpdateRate)
        player1Cam.update()

        player2Cam.position.lerp(player2CamTargetPosition, playerCameraUpdateRate)
        player2Cam.update()
    }

    override fun render() {
        // clear screen
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT)

        drawPlayer1Content()
        drawPlayer2Content()

        // draw separator
        sb.projectionMatrix = hudCam.combined
        // set viewport back to normal
        Gdx.gl.glViewport(0,0,Gdx.graphics.width, Gdx.graphics.height)
        sb.begin()
        sb.draw(separatorTextureRegion, hudCam.viewportWidth/2-separatorTextureRegion.regionWidth/2, 0f)
        sb.end()
    }

    private fun drawPlayer1Content() {
        sb.projectionMatrix = player1Cam.combined

        // -- draw section for player 1 --
        // draw tilemap for player 1
        sb.begin()
        Gdx.gl.glViewport(0,0,Gdx.graphics.width/2, Gdx.graphics.height)
        tmr.setView(player1Cam)
        tmr.render()
        sb.end()

        // draw anything else for player 1
        sb.begin()
        player1.draw(sb)

        // draw opponent
        player2.draw(sb)

        sb.end()
        // -- end of drawing section for player 1 --
    }

    private fun drawPlayer2Content() {
        sb.projectionMatrix = player2Cam.combined

        // -- draw section for player 2 --
        // draw tilemap for player 2
        sb.begin()
        Gdx.gl.glViewport(Gdx.graphics.width/2,0,Gdx.graphics.width/2, Gdx.graphics.height)
        tmr.setView(player2Cam)
        tmr.render()
        sb.end()

        // draw anything else for player 2
        sb.begin()
        player2.draw(sb)

        // draw opponent
        player1.draw(sb)

        sb.end()
        // -- end of drawing section for player 2 --
    }

    override fun dispose() {

    }

    override fun resize_user(width: Int, height: Int) {

    }

    // this function ignore if element value of direction vector is zero
    // it will operate on 2 dimension only and zero in 3rd dimension
    private fun calculateNewCameraPosition(player: Player, dir: Vector2): Vector3 {
        var newPosition = Vector3()

        // x direction
        if (dir.x > 0) {
            newPosition.x = player.x + PLAYER_CAM_AHEAD_OFFSET
        }
        else if (dir.x < 0) {
            newPosition.x = player.x - PLAYER_CAM_AHEAD_OFFSET
        }
        else {
            newPosition.x = player.x
        }

        // y direction
        if (dir.y > 0) {
            newPosition.y = player.y + PLAYER_CAM_AHEAD_OFFSET
        }
        else if (dir.y < 0) {
            newPosition.y = player.y - PLAYER_CAM_AHEAD_OFFSET
        }
        else {
            newPosition.y = player.y
        }

        return newPosition
    }
}