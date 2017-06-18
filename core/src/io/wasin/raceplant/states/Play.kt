package io.wasin.raceplant.states

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.controllers.mappings.Xbox
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapRenderer
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Sort
import com.badlogic.gdx.utils.viewport.ExtendViewport
import io.wasin.raceplant.Game
import io.wasin.raceplant.entities.*
import io.wasin.raceplant.handlers.BBInput
import io.wasin.raceplant.handlers.GameStateManager
import io.wasin.raceplant.handlers.Settings
import java.text.DecimalFormat

/**
 * Created by haxpor on 6/16/17.
 */
class Play(gsm: GameStateManager): GameState(gsm){

    private val tilemap: TiledMap
    private val plantTileLayer: TiledMapTileLayer
    private val stockpilesLayer: TiledMapTileLayer
    private val waterTilesLayer: TiledMapTileLayer
    private val tmr: TiledMapRenderer
    private val tileSize: Float
    private val mapNumTileWidth: Int
    private val mapNumTileHeight: Int
    private val playerSize: Float

    lateinit private var player1Cam: OrthographicCamera
    lateinit private var player1Viewport: ExtendViewport

    lateinit private var player2Cam: OrthographicCamera
    lateinit private var player2Viewport: ExtendViewport

    lateinit private var player1: Player
    lateinit private var player2: Player

    private var player1CamTargetPosition: Vector3 = Vector3(0f,0f,0f)   // we need lerping of camera to be taken into effect immediately, thus not use Vector3.Zero
    private var player2CamTargetPosition: Vector3 = Vector3(0f,0f,0f)   // we need lerping of camera to be taken into effect immediately, thus not use Vector3.Zero

    private var separatorTextureRegion: TextureRegion
    private var scorehudTextureRegion: TextureRegion
    private var matchTimeHudTextureRegion: TextureRegion
    private var playerCameraUpdateRate: Float = 0.15f

    private var seeds: ArrayList<Seed> = ArrayList()
    private var fruits: ArrayList<Fruit> = ArrayList()
    private var trees: ArrayList<Tree> = ArrayList()
    private var buckets: ArrayList<Bucket> = ArrayList()
    private var waterdrops: ArrayList<WaterDrop> = ArrayList()
    private var floatingTexts: ArrayList<FloatingText> = ArrayList()
    private var waterFilleds: ArrayList<WaterFilled> = ArrayList()

    private var font: BitmapFont = BitmapFont()
    private var player1ScoreGlyph: GlyphLayout = GlyphLayout()
    private var player2ScoreGlyph: GlyphLayout = GlyphLayout()

    private var neededSortEntities: ArrayList<SortSprite> = ArrayList()

    private var matchTimeout: Float = Settings.MATCH_TIME.toFloat()
    private var matchRemainingTimeGlyph: GlyphLayout = GlyphLayout()
    private var decimalFormat: DecimalFormat = DecimalFormat("#00")
    private var isMatchOver: Boolean = false

    private var matchResultLine1Glyph: GlyphLayout = GlyphLayout()
    private var matchResultLine2Glyph: GlyphLayout = GlyphLayout()

    companion object {
        const val PLAYER_MOVE_SPEED = 50.0f
        const val PLAYER_CAM_AHEAD_OFFSET = 30f  // ahead distance to move player's camera at
        const val CONTROLLER_DEADZONE_VALUE = 0.3f
    }

    init {
        tilemap = TmxMapLoader().load("maps/mapA.tmx")
        plantTileLayer = tilemap.layers.get("plantslot") as TiledMapTileLayer
        stockpilesLayer = tilemap.layers.get("stockpiles") as TiledMapTileLayer
        waterTilesLayer = tilemap.layers.get("water") as TiledMapTileLayer
        tmr = OrthogonalTiledMapRenderer(tilemap)
        tileSize = tilemap.properties.get("tilewidth", Float::class.java)
        mapNumTileWidth = tilemap.properties.get("width", Int::class.java)
        mapNumTileHeight = tilemap.properties.get("height", Int::class.java)

        val tex = Game.res.getTexture("separator")!!
        separatorTextureRegion = TextureRegion(tex, tex.width, tex.height)

        val scorehudTex = Game.res.getTexture("scorehud")!!
        scorehudTextureRegion = TextureRegion(scorehudTex, scorehudTex.width, scorehudTex.height)

        val matchtimehudTex = Game.res.getTexture("matchui")!!
        matchTimeHudTextureRegion = TextureRegion(matchtimehudTex, matchtimehudTex.width, matchtimehudTex.height)

        setupPlayer1Camera()
        setupPlayer2Camera()

        setupPlayer1()
        setupPlayer2()

        setupGlyphs()

        // set player size, used for sort entities that need to be sorted for z-order
        playerSize = player1.height

        initalizeMatch()
    }

    private fun initalizeMatch() {
        player1.x = 50f
        player1.y = 50f
        player1CamTargetPosition.set(player1.x, player1.y, 0f)

        player2.x = 50f
        player2.y = 70f
        player2CamTargetPosition.set(player2.x, player2.y, 0f)

        seeds.add(Seed(Game.res.getTexture("seed")!!, 150f, 150f))
        seeds.add(Seed(Game.res.getTexture("seed")!!, 200f, 150f))

        //fruits.add(Fruit(Game.res.getTexture("damageball")!!, 200f, 200f))

        // TODO: Remove this mocking up of placing buckets for two player when we don't need it
        buckets.add(Bucket(Game.res.getTexture("bucket")!!, 70f, 220f, Bucket.State.EMPTY))
        buckets.add(Bucket(Game.res.getTexture("bucket")!!, 70f, 250f, Bucket.State.EMPTY))

        // set inittial camera position to be at the center of the map, before lerping takes place
        val centerTileMapX = tilemap.properties.get("width", Int::class.java) * tileSize / 2f
        val centerTileMapY = tilemap.properties.get("height", Int::class.java) * tileSize / 2f
        player1Cam.position.set(Vector3(centerTileMapX, centerTileMapY, 0f))
        player2Cam.position.set(Vector3(centerTileMapX, centerTileMapY, 0f))
    }

    private fun setupGlyphs() {
        player1ScoreGlyph.setText(font, "${player1.fruitCollected}")
        player2ScoreGlyph.setText(font, "${player2.fruitCollected}")

        matchRemainingTimeGlyph.setText(font, "" + convertSecondsToMinuteAndRemainingSeconds(matchTimeout.toInt()))
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
        handlePlayerInput(player1, dt)
        handlePlayerInput(player2, dt)
    }

    fun handleInputWhenMatchOver(dt: Float) {
        // give privillege to player 1 to rematch
        if (BBInput.isControllerPressed(0, BBInput.CONTROLLER_BUTTON_2)) {
            // TODO: Reset the match and start again
            restartMatch()
        }
    }

    private fun handlePlayerInput(player: Player, dt: Float) {

        // check to assign and process against a correct controller
        var controller = BBInput.controller1
        var cindex = 0

        if (player == player1) {
            controller = BBInput.controller1
            cindex = 0
        }
        else if (player == player2) {
            controller = BBInput.controller2
            cindex = 1
        }
        else {
            return
        }

        var triggeredToMove = false
        if (BBInput.isControllerDown(cindex, BBInput.CONTROLLER_BUTTON_LEFT) ||
                (controller != null && controller.getAxis(Xbox.L_STICK_HORIZONTAL_AXIS) < -CONTROLLER_DEADZONE_VALUE)) {
            // update player position
            player.x -= PLAYER_MOVE_SPEED * dt
            // update camera position
            if (cindex == 0) {
                player1CamTargetPosition = calculateNewCameraPosition(player, Vector2(-1f, 0f))
            }
            else if (cindex == 1) {
                player2CamTargetPosition = calculateNewCameraPosition(player, Vector2(-1f, 0f))
            }
            // walk
            player.walk()
            // set facing direction
            player.faceLeft()
            triggeredToMove = true
        }

        if (BBInput.isControllerDown(cindex, BBInput.CONTROLLER_BUTTON_RIGHT) ||
                (controller != null && controller.getAxis(Xbox.L_STICK_HORIZONTAL_AXIS) > CONTROLLER_DEADZONE_VALUE)) {
            // update player position
            player.x += PLAYER_MOVE_SPEED * dt
            // update camera position
            if (cindex == 0) {
                player1CamTargetPosition = calculateNewCameraPosition(player, Vector2(1f, 0f))
            }
            else if (cindex == 1) {
                player2CamTargetPosition = calculateNewCameraPosition(player, Vector2(1f, 0f))
            }
            // walk
            player.walk()
            // set facing direction
            player.faceRight()
            triggeredToMove = true
        }

        if (BBInput.isControllerDown(cindex, BBInput.CONTROLLER_BUTTON_UP) ||
                (controller != null && controller.getAxis(Xbox.L_STICK_VERTICAL_AXIS) < -CONTROLLER_DEADZONE_VALUE)) {
            // update player position
            player.y += PLAYER_MOVE_SPEED * dt
            // update camera position
            if (cindex == 0) {
                player1CamTargetPosition = calculateNewCameraPosition(player, Vector2(0f, 1f))
            }
            else if (cindex == 1) {
                player2CamTargetPosition = calculateNewCameraPosition(player, Vector2(0f, 1f))
            }
            // walk
            player1.walk()
            triggeredToMove = true
        }

        if (BBInput.isControllerDown(cindex, BBInput.CONTROLLER_BUTTON_DOWN) ||
                (controller != null && controller.getAxis(Xbox.L_STICK_VERTICAL_AXIS) > CONTROLLER_DEADZONE_VALUE)) {
            // update player position
            player.y -= PLAYER_MOVE_SPEED * dt
            // update camera position
            if (cindex == 0) {
                player1CamTargetPosition = calculateNewCameraPosition(player, Vector2(0f, -1f))
            }
            else if (cindex == 1) {
                player2CamTargetPosition = calculateNewCameraPosition(player, Vector2(0f, -1f))
            }
            // walk
            player.walk()
            triggeredToMove = true
        }

        // place down seed if carrying
        if (BBInput.isControllerPressed(cindex, BBInput.CONTROLLER_BUTTON_1) && player.state == Player.State.CARRY_SEED) {

            // calculate position to place seed on tile
            // this position is used to check against plant slot tile
            val seedToPlacePos = if (cindex == 0) Vector2(player1CamTargetPosition.x, player1CamTargetPosition.y) else
                Vector2(player2CamTargetPosition.x, player2CamTargetPosition.y)

            // check seed against tile whether to add trees
            val (col, row) = convertPositionToTilePosition(seedToPlacePos)
            var cell = plantTileLayer.getCell(col, row)

            Gdx.app.log("Play", "seeds is placed down at $col,$row")

            // if seed collides with the plant-slot tile and there's no tree planted there yet
            // then remove that seed
            if (cell != null && cell.tile != null) {
                val plantSlotPos = convertTileIndexIntoPosition(col, row)

                // check if there's not any tree already planted
                if ( trees.filter { it.tileColIndex == col && it.tileRowIndex == row }.count() == 0) {
                    Gdx.app.log("Play", "Seeds planted at $col,$row")

                    val tree = Tree(Game.res.getTexture("tree")!!, font, plantSlotPos.x, plantSlotPos.y,
                            col, row,
                            {
                                // callback to give both fruit and seed
                                Gdx.app.log("Play", "Tree generated a new fruit and a seed")

                                // create a new fruit
                                val fruit = Fruit(Game.res.getTexture("damageball")!!,
                                        it.x + MathUtils.random(-it.width / 2f, it.width / 2),
                                        it.y - MathUtils.random(0f, it.width / 2f))
                                fruits.add(fruit)

                                // create a new seed
                                val seed = Seed(Game.res.getTexture("seed")!!,
                                        it.x + MathUtils.random(-it.width / 2f, it.width / 2),
                                        it.y - MathUtils.random(0f, it.width / 2f))
                                seeds.add(seed)
                            }
                    )
                    trees.add(tree)
                }
                // otherwise place the seed on the tile normally
                else {
                    // calculate position to place down seed
                    seeds.add(Seed(Game.res.getTexture("seed")!!, seedToPlacePos.x, seedToPlacePos.y))
                }
            }
                // otherwise place the seed on the tile normally
            else {
                // calculate position to place down seed
                seeds.add(Seed(Game.res.getTexture("seed")!!, seedToPlacePos.x, seedToPlacePos.y))
            }

            player.state = Player.State.IDLE
        }
        // place down fruit if carrying
        if (BBInput.isControllerPressed(cindex, BBInput.CONTROLLER_BUTTON_1) && player.state == Player.State.CARRY_FRUIT) {

            // calculate position to place fruit on tile
            // this position is used to check against stockpile tile
            val fruitToPlacePos = if (cindex == 0) Vector2(player1CamTargetPosition.x, player1CamTargetPosition.y) else
                Vector2(player2CamTargetPosition.x, player2CamTargetPosition.y)

            // check seed against tile whether to add trees
            val (col, row) = convertPositionToTilePosition(fruitToPlacePos)
            var cell = stockpilesLayer.getCell(col, row)

            Gdx.app.log("Play", "fruit is placed down at $col,$row")

            // if seed collides with the stockpile tile then ...
            // 1. increase score for that player
            // 2. make that fruit disappear
            // 3. show floating text for +1 score
            if (cell != null && cell.tile != null) {
                Gdx.app.log("Play", "fruit placed at $col,$row")

                val stockpileSlotPos = convertTileIndexIntoPosition(col, row)
                // 1.
                player.increaseCollectedFruit()
                if (player == player1) {
                    player1ScoreGlyph.setText(font, "${player.fruitCollected}")
                }
                else if (player == player2) {
                    player2ScoreGlyph.setText(font, "${player.fruitCollected}")
                }

                // 2.
                fruits.add(Fruit(Game.res.getTexture("damageball")!!, fruitToPlacePos.x, fruitToPlacePos.y, true))

                // 3.
                floatingTexts.add(FloatingText("+1", fruitToPlacePos.x, fruitToPlacePos.y))
            }
            // otherwise place the fruit on the tile normally
            else {
                // calculate position to place down fruit
                fruits.add(Fruit(Game.res.getTexture("damageball")!!, fruitToPlacePos.x, fruitToPlacePos.y))
            }

            player.state = Player.State.IDLE
        }
        // place down bucket if carrying full bucket
        if (BBInput.isControllerPressed(cindex, BBInput.CONTROLLER_BUTTON_1) &&
                player.state == Player.State.CARRY_FULLBUCKET) {

            // calculate position to place bucket on tile
            // this position is used to check against planted tree on tile
            val bucketToPlacePos = if (cindex == 0) Vector2(player1CamTargetPosition.x, player1CamTargetPosition.y) else
                Vector2(player2CamTargetPosition.x, player2CamTargetPosition.y)

            // check bucket against tile whether to add water-humid effect
            val (col, row) = convertPositionToTilePosition(bucketToPlacePos)

            // search for all trees whether which one is exactly that col,row
            var isFoundMatchingTree = false
            for (tree in trees) {
                val (colChk, rowChk) = convertPositionToTilePosition(Vector2(tree.x, tree.y))
                if (colChk == col && rowChk == row) {
                    waterdrops.add(WaterDrop(Game.res.getTexture("waterdrop")!!, tree.x, tree.y))

                    floatingTexts.add(FloatingText("-${Settings.WATER_REDUCE_TIME_DURATION}", tree.x, tree.y))
                    tree.waterIt()

                    // water is used then show empty bucket
                    buckets.add(Bucket(Game.res.getTexture("bucket")!!, tree.x, tree.y, Bucket.State.EMPTY))
                    isFoundMatchingTree = true
                    break
                }
            }

            // if not found matching tree then, add back original state of bucket to the world
            // check the original state from player (carrying)
            if (!isFoundMatchingTree) {
                buckets.add(Bucket(Game.res.getTexture("bucket")!!, bucketToPlacePos.x, bucketToPlacePos.y,
                        if (player.state == Player.State.CARRY_EMPTYBUCKET) Bucket.State.EMPTY else Bucket.State.FULL))
            }

            player.state = Player.State.IDLE
        }
        // place down bucket if carrying empty bucket
        if (BBInput.isControllerPressed(cindex, BBInput.CONTROLLER_BUTTON_1) &&
                player.state == Player.State.CARRY_EMPTYBUCKET) {

            // calculate position to place empty bucket on tile
            // this position is used to check against stockpile tile
            val bucketToPlacePos = if (cindex == 0) Vector2(player1CamTargetPosition.x, player1CamTargetPosition.y) else
                Vector2(player2CamTargetPosition.x, player2CamTargetPosition.y)

            // check bucket against water tile
            val (col, row) = convertPositionToTilePosition(bucketToPlacePos)
            var cell = waterTilesLayer.getCell(col, row)

            Gdx.app.log("Play", "empty bucket is placed down at $col,$row")

            // if empty bucket collides with the water tile tile then ...
            // 1. Add full bucket
            // 2. Add WaterFilled effect
            if (cell != null && cell.tile != null) {
                Gdx.app.log("Play", "empty bucket placed at $col,$row")

                // 1.
                buckets.add(Bucket(Game.res.getTexture("bucket")!!, bucketToPlacePos.x, bucketToPlacePos.y, Bucket.State.FULL))

                // 2.
                waterFilleds.add(WaterFilled(Game.res.getTexture("waterfilled")!!, bucketToPlacePos.x, bucketToPlacePos.y))

                // 3.
                floatingTexts.add(FloatingText("Filled", bucketToPlacePos.x - Bucket.SPRITE_SIZE/2, bucketToPlacePos.y + Bucket.SPRITE_SIZE/2))
            }
            // otherwise place the fruit on the tile normally
            else {
                // calculate position to place down fruit
                buckets.add(Bucket(Game.res.getTexture("bucket")!!, bucketToPlacePos.x, bucketToPlacePos.y, Bucket.State.EMPTY))
            }

            player.state = Player.State.IDLE
        }

        // check if user didn't trigger to walk, then back to normal
        if (!triggeredToMove && !player.isCarry()) {
            player.state = Player.State.IDLE
        }
    }

    override fun update(dt: Float) {

        // update match timeout
        if (!isMatchOver) {
            matchTimeout -= dt

            val (mMiniutes, mSeconds) = convertSecondsToMinuteAndRemainingSeconds(matchTimeout.toInt())
            matchRemainingTimeGlyph.setText(font, decimalFormat.format(mMiniutes) + ":" + decimalFormat.format(mSeconds))

            if (matchTimeout < 0f) {
                isMatchOver = true

                // set text to show as result
                var textResult = ""
                if (player1.fruitCollected > player2.fruitCollected) {
                    textResult = "Player 1 Won!"
                }
                else if (player1.fruitCollected < player2.fruitCollected) {
                    textResult = "Player 2 Won!"
                }
                else {
                    textResult = "Match Tied"
                }
                matchResultLine1Glyph.setText(font, textResult)
                matchResultLine2Glyph.setText(font, "Press A to rematch")
            }
        }

        if (isMatchOver) {
            Gdx.app.log("Play", "Match is over")

            handleInputWhenMatchOver(dt)
        }
        else {
            handleInput(dt)

            // update seeds
            for (i in seeds.count() - 1 downTo 0) {
                seeds[i].update(dt)

                // check if player take the seed
                if (!player1.isCarry() && seeds[i].boundingRectangle.overlaps(player1.boundingRectangle)) {
                    player1.state = Player.State.CARRY_SEED
                    seeds.removeAt(i)
                } else if (!player2.isCarry() && seeds[i].boundingRectangle.overlaps(player2.boundingRectangle)) {
                    player2.state = Player.State.CARRY_SEED
                    seeds.removeAt(i)
                }
            }

            // update fruits
            for (i in fruits.count() - 1 downTo 0) {
                fruits[i].update(dt)

                // check if player take the seed
                // also check if player has picked up something already
                if (fruits[i].isAlive && !fruits[i].isMarkedAsCollected && !player1.isCarry() && fruits[i].boundingRectangle.overlaps(player1.boundingRectangle)) {
                    player1.state = Player.State.CARRY_FRUIT
                    fruits.removeAt(i)
                } else if (fruits[i].isAlive && !fruits[i].isMarkedAsCollected && !player2.isCarry() && fruits[i].boundingRectangle.overlaps(player2.boundingRectangle)) {
                    player2.state = Player.State.CARRY_FRUIT
                    fruits.removeAt(i)
                } else if (!fruits[i].isAlive) {
                    fruits.removeAt(i)
                }
            }

            // update buckets
            for (i in buckets.count() - 1 downTo 0) {
                buckets[i].update(dt)

                // check if player take the bucket
                if (!player1.isCarry() && buckets[i].boundingRectangle.overlaps(player1.boundingRectangle)) {

                    // it depends on the state of bucket
                    if (buckets[i].state == Bucket.State.EMPTY) {
                        player1.state = Player.State.CARRY_EMPTYBUCKET
                    } else if (buckets[i].state == Bucket.State.FULL) {
                        player1.state = Player.State.CARRY_FULLBUCKET
                    }

                    buckets.removeAt(i)
                } else if (!player2.isCarry() && buckets[i].boundingRectangle.overlaps(player2.boundingRectangle)) {

                    // it depends on the state of bucket
                    if (buckets[i].state == Bucket.State.EMPTY) {
                        player2.state = Player.State.CARRY_EMPTYBUCKET
                    } else if (buckets[i].state == Bucket.State.FULL) {
                        player2.state = Player.State.CARRY_FULLBUCKET
                    }

                    buckets.removeAt(i)
                }
            }

            // update waterdrops
            for (i in waterdrops.count() - 1 downTo 0) {
                waterdrops[i].update(dt)

                if (!waterdrops[i].isAlive) {
                    waterdrops.removeAt(i)
                }
            }

            // update waterfilleds
            for (i in waterFilleds.count() - 1 downTo 0) {
                waterFilleds[i].update(dt)

                if (!waterFilleds[i].isAlive) {
                    waterFilleds.removeAt(i)
                }
            }

            // update floating text
            for (i in floatingTexts.count() - 1 downTo 0) {
                floatingTexts[i].update(dt)

                if (!floatingTexts[i].isAlive) {
                    floatingTexts.removeAt(i)
                }
            }

            // update trees
            trees.forEach { it.update(dt) }

            // update players
            player1.update(dt)
            player2.update(dt)

            // update player 1 and 2 camera
            // also round the camera's position to avoid line bleeding problem of tilemap
            player1Cam.position.lerp(player1CamTargetPosition, playerCameraUpdateRate)
            boundCameraAndPlayerPosition(player1)
            player1Cam.position.x = MathUtils.round(10.5f * player1Cam.position.x) / 10.5f
            player1Cam.position.y = MathUtils.round(10.5f * player1Cam.position.y) / 10.5f
            player1Cam.update()

            player2Cam.position.lerp(player2CamTargetPosition, playerCameraUpdateRate)
            boundCameraAndPlayerPosition(player2)
            player2Cam.position.x = MathUtils.round(10.5f * player2Cam.position.x) / 10.5f
            player2Cam.position.y = MathUtils.round(10.5f * player2Cam.position.y) / 10.5f
            player2Cam.update()

            // bound position of other objects
            buckets.forEach { boundSpritePosition(it) }
            seeds.forEach { boundSpritePosition(it) }
            fruit.forEach { boundSpritePosition(it)}

            // add all entities that need sorting according to z-order (y-position in this case)
            // relavent is buckets, seeds, fruit, trees, and players
            neededSortEntities.clear()
            buckets.forEach { neededSortEntities.add(SortSprite(it, it.y - playerSize)) }
            seeds.forEach { neededSortEntities.add(SortSprite(it, it.y - playerSize)) }
            fruits.forEach { neededSortEntities.add(SortSprite(it, it.y - playerSize)) }
            trees.forEach { neededSortEntities.add(SortSprite(it, it.y - playerSize)) }
            neededSortEntities.add(SortSprite(player1, player1.y - playerSize))
            neededSortEntities.add(SortSprite(player2, player2.y - playerSize))
            neededSortEntities.sortByDescending { it.zOrder }
        }
    }

    private fun boundCameraAndPlayerPosition(player: Player) {
        val mapWidth = tileSize * mapNumTileWidth
        val mapHeight = tileSize * mapNumTileHeight

        // select the proper camera to work with
        val cam: OrthographicCamera = if (player == player1) player1Cam else player2Cam

        // bound camera
        // x
        if (cam.position.x < cam.viewportWidth/2) {
            cam.position.x = cam.viewportWidth/2

            if (player.x < 0f) {
                player.x = 0f
            }
        }
        if (cam.position.x + cam.viewportWidth/2 > tileSize * mapNumTileWidth) {
            cam.position.x = mapWidth - cam.viewportWidth/2

            if (player.x + player.width> mapWidth) {
                player.x = mapWidth - player.width
            }
        }

        // y
        if (cam.position.y < cam.viewportHeight/2) {
            cam.position.y = cam.viewportHeight/2

            if (player.y < 0f) {
                player.y = 0f
            }
        }
        if (cam.position.y + cam.viewportHeight/2 > tileSize * mapNumTileHeight) {
            cam.position.y = mapHeight - cam.viewportHeight/2

            if (player.y + player.width> mapHeight) {
                player.y = mapHeight - player.width
            }
        }
    }

    private fun boundSpritePosition(sprite: Sprite) {
        val mapWidth = tileSize * mapNumTileWidth
        val mapHeight = tileSize * mapNumTileHeight

        // x
        if (sprite.x < 0f) {
            sprite.x = 0f
        }
        if (sprite.x + sprite.width > mapWidth) {
            sprite.x = mapWidth - sprite.width
        }

        // y
        if (sprite.y < 0f) {
            sprite.y = 0f
        }
        if (sprite.y + sprite.height > mapHeight) {
            sprite.y = mapHeight - sprite.height
        }
    }

    override fun render() {
        // clear screen
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT)

        drawPlayer1Content()
        drawPlayer2Content()

        drawHud()

        // if match is over then draw result over the game content
        if (isMatchOver) {
            drawMatchResult()
        }
    }

    private fun drawHud() {
        sb.projectionMatrix = hudCam.combined
        // set viewport back to normal
        Gdx.gl.glViewport(0,0,Gdx.graphics.width, Gdx.graphics.height)

        sb.begin()
        // score hud
        sb.projectionMatrix = hudCam.combined
        // score hud - player 1
        sb.draw(scorehudTextureRegion, 0f, hudCam.viewportHeight - scorehudTextureRegion.regionHeight, hudCam.viewportWidth/2f, scorehudTextureRegion.regionHeight.toFloat())
        // score hud - player 1's score
        font.draw(sb, player1ScoreGlyph, hudCam.viewportWidth/4 - player1ScoreGlyph.width/2, hudCam.viewportHeight - player1ScoreGlyph.height/2)
        // score hud - player 2
        sb.draw(scorehudTextureRegion, hudCam.viewportWidth/2 + 2f, hudCam.viewportHeight - scorehudTextureRegion.regionHeight, hudCam.viewportWidth/2f, scorehudTextureRegion.regionHeight.toFloat())
        // score hud - player 2's score
        font.draw(sb, player2ScoreGlyph, hudCam.viewportWidth/4*3 - player2ScoreGlyph.width/2, hudCam.viewportHeight - player2ScoreGlyph.height/2)
        // draw separator
        sb.draw(separatorTextureRegion, hudCam.viewportWidth/2-separatorTextureRegion.regionWidth/2, 0f)

        drawMatchTimeRemainingHud()

        sb.end()
    }

    private fun drawMatchTimeRemainingHud() {
        sb.draw(matchTimeHudTextureRegion, hudCam.viewportWidth/2-matchTimeHudTextureRegion.regionWidth/2,
                hudCam.viewportHeight - matchTimeHudTextureRegion.regionHeight)
        font.draw(sb, matchRemainingTimeGlyph, hudCam.viewportWidth/2 - matchRemainingTimeGlyph.width/2,
                hudCam.viewportHeight - matchTimeHudTextureRegion.regionHeight/2 + matchRemainingTimeGlyph.height/2)
    }

    private fun drawMatchResult() {
        sb.projectionMatrix = hudCam.combined
        // set viewport back to normal
        Gdx.gl.glViewport(0,0,Gdx.graphics.width, Gdx.graphics.height)

        sb.begin()

        font.draw(sb, matchResultLine1Glyph, hudCam.viewportWidth/2 - matchResultLine1Glyph.width/2,
                hudCam.viewportHeight/2 - matchResultLine1Glyph.height/2)
        font.draw(sb, matchResultLine2Glyph, hudCam.viewportWidth/2 - matchResultLine2Glyph.width/2,
                hudCam.viewportHeight/2 - matchResultLine1Glyph.height - 6f - matchResultLine2Glyph.height/2)

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

        // trees, seeds, fruits, buckets, and players are drew by this
        neededSortEntities.forEach { it.sprite.draw(sb) }
        // waterfilled
        waterFilleds.forEach { it.draw(sb) }
        // waterdrops
        waterdrops.forEach { it.draw(sb) }

        // floating text
        floatingTexts.forEach { it.render(sb) }

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

        // trees, seeds, fruits, buckets, and players are drew by this
        neededSortEntities.forEach { it.sprite.draw(sb) }
        // waterfilled
        waterFilleds.forEach { it.draw(sb) }
        // waterdrops
        waterdrops.forEach { it.draw(sb) }

        // floating text
        floatingTexts.forEach { it.render(sb) }

        sb.end()
        // -- end of drawing section for player 2 --
    }

    override fun dispose() {

    }

    override fun resize_user(width: Int, height: Int) {
        player1Viewport.update(width/2, height)
        player2Viewport.update(width/2, height)
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

    private fun convertPositionToTilePosition(pos: Vector2): Pair<Int, Int> {
        val col = Math.floor( pos.x.toDouble() / tileSize).toInt()
        val row = Math.floor( pos.y.toDouble() / tileSize).toInt()

        // TODO: I know, this line smells quite a lot, should be better way to avoid creating a new object every return...
        return Pair(col, row)
    }

    private fun convertTileIndexIntoPosition(col: Int, row: Int): Vector2 {
        return Vector2(col * tileSize, row * tileSize + tileSize/2.3f)
    }

    private fun convertSecondsToMinuteAndRemainingSeconds(seconds: Int): Pair<Int, Int>  {
        val minutes = Math.floor(seconds.toDouble() / 60.0).toInt()
        val remainingSeconds = seconds.rem(60)
        return Pair(minutes, remainingSeconds)
    }

    private fun restartMatch() {
        matchTimeout = Settings.MATCH_TIME.toFloat()
        isMatchOver = false

        // remove all entities from array list
        seeds.clear()
        fruits.clear()
        trees.clear()
        buckets.clear()
        waterFilleds.clear()
        waterdrops.clear()
        floatingTexts.clear()
        neededSortEntities.clear()

        player1.resetState()
        player2.resetState()

        player1ScoreGlyph.setText(font, "${player1.fruitCollected}")
        player2ScoreGlyph.setText(font, "${player2.fruitCollected}")

        initalizeMatch()
    }
}