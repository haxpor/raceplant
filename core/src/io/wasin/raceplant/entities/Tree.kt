package io.wasin.raceplant.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import io.wasin.raceplant.Game
import io.wasin.raceplant.handlers.Settings
import java.text.DecimalFormat

/**
 * Created by haxpor on 6/17/17.
 */
class Tree(texture: Texture, font: BitmapFont, x: Float, y: Float, tileColIndex: Int, tileRowIndex: Int, fruitGeneratedCallback: ((Tree) -> Unit)?): Sprite(texture, SPRITE_WIDTH, SPRITE_HEIGHT) {

    companion object {
        const val SPRITE_WIDTH: Int = 32
        const val SPRITE_HEIGHT: Int = 96
    }

    // animation
    private var growStep1Animation: Animation<TextureRegion>
    private var growStep2Animation: Animation<TextureRegion>
    private var growStep3Animation: Animation<TextureRegion>
    private var animationTimer: Float = 0f

    var state: State = State.GROW_STEP_1
        private set
    private var randomFlipX: Boolean = false

    private var growthTimeout: Float = Settings.GROWTH_TO_LEVEL_2_DURATION.toFloat()

    private var font: BitmapFont = font
    private var timeleftGlyph: GlyphLayout = GlyphLayout()
    private var decimalFormat: DecimalFormat = DecimalFormat("#00")

    private var fruitGenerationTimeout: Float = Settings.FRUIT_GENERATION_COOLDOWN.toFloat()
    private var fruitGenerationCallback: ((Tree) -> Unit)? = fruitGeneratedCallback

    // tile index for tree that is planted on
    val tileColIndex: Int = tileColIndex
    val tileRowIndex: Int = tileRowIndex

    enum class State {
        GROW_STEP_1,
        GROW_STEP_2,
        GROW_STEP_3
    }

    init {
        // populate animations
        val tmpFrames = TextureRegion.split(texture, Tree.SPRITE_WIDTH, Tree.SPRITE_HEIGHT)

        // grow step 1
        val growStep1Frames = Array<TextureRegion>()
        for (col in 0..0) {
            growStep1Frames.add(tmpFrames[0][col])
        }
        growStep1Animation = Animation<TextureRegion>(1 / 7f, growStep1Frames, Animation.PlayMode.LOOP)

        // grow step 2
        val growStep2Frames = Array<TextureRegion>()
        for (col in 1..1) {
            growStep2Frames.add(tmpFrames[0][col])
        }
        growStep2Animation = Animation<TextureRegion>(1 / 7f, growStep2Frames, Animation.PlayMode.LOOP)

        // grow step 3
        val growStep3Frames = Array<TextureRegion>()
        for (col in 2..2) {
            growStep3Frames.add(tmpFrames[0][col])
        }
        growStep3Animation = Animation<TextureRegion>(1 / 7f, growStep3Frames, Animation.PlayMode.LOOP)

        // place on input position
        this.x = x
        this.y = y

        growIntoStep1()

        // randomize to flipx for its whole life time
        randomFlipX = MathUtils.randomBoolean()
    }
    
    fun update(dt: Float) {
        animationTimer += dt

        // set texture region
        var currentFrameRegion: TextureRegion? = null
        when {
            state == State.GROW_STEP_1 -> {
                currentFrameRegion = growStep1Animation.getKeyFrame(animationTimer)
            }
            state == State.GROW_STEP_2 -> {
                currentFrameRegion = growStep2Animation.getKeyFrame(animationTimer)
            }
            state == State.GROW_STEP_3 -> {
                currentFrameRegion = growStep3Animation.getKeyFrame(animationTimer)
            }
        }

        if (currentFrameRegion != null) {
            setRegion(currentFrameRegion!!)
        }

        // then draw time left until growing into next level text
        // - need to go to level 2
        if (state == State.GROW_STEP_1) {
            growthTimeout -= dt

            val (minutes, seconds) = convertSecondsToMinuteAndRemainingSeconds(growthTimeout.toInt())
            timeleftGlyph.setText(font, "$minutes:" + decimalFormat.format(seconds))

            if (growthTimeout <= 0f) {
                // change state to next level
                growIntoStep2()
            }
        }
        // - need to go to level 3
        else if (state == State.GROW_STEP_2) {
            growthTimeout -= dt

            val (minutes, seconds) = convertSecondsToMinuteAndRemainingSeconds(growthTimeout.toInt())
            timeleftGlyph.setText(font, "$minutes:" + decimalFormat.format(seconds))

            if (growthTimeout <= 0f) {
                // change state to next level
                growIntoStep3()
            }
        }
            // generate fruit periodically
        else if (state == State.GROW_STEP_3) {
            fruitGenerationTimeout -= dt

            val (minutes, seconds) = convertSecondsToMinuteAndRemainingSeconds(fruitGenerationTimeout.toInt())
            timeleftGlyph.setText(font, "$seconds")

            if (fruitGenerationTimeout <= 0f) {
                // restart generation again
                fruitGenerationTimeout = Settings.FRUIT_GENERATION_COOLDOWN.toFloat()

                // trigger callback
                fruitGenerationCallback?.invoke(this)
            }
        }

        // update flip x-direction from randomization
        flip(false, isFlipY)
    }

    fun growIntoStep1() {
        val hit = Game.res.getSound("tree-grow-step")!!
        val hitId = hit.play()
        hit.setVolume(hitId, 1.0f)

        state = State.GROW_STEP_1
        growthTimeout = Settings.GROWTH_TO_LEVEL_2_DURATION.toFloat()
    }

    fun growIntoStep2() {
        val hit = Game.res.getSound("tree-grow-step")!!
        val hitId = hit.play()
        hit.setVolume(hitId, 1.0f)

        state = State.GROW_STEP_2
        growthTimeout = Settings.GROWTH_TO_LEVEL_3_DURATION.toFloat()
    }

    fun growIntoStep3() {
        val hit = Game.res.getSound("tree-grow-step")!!
        val hitId = hit.play()
        hit.setVolume(hitId, 1.0f)

        state = State.GROW_STEP_3
    }

    override fun draw(batch: Batch?) {
        super.draw(batch)

        if (state == State.GROW_STEP_3) {
            font.draw(batch, timeleftGlyph, x + width/2 - 5f, y - 10f - timeleftGlyph.height / 2f)
        }
        else {
            font.draw(batch, timeleftGlyph, x, y - 10f - timeleftGlyph.height / 2f)
        }
    }

    private fun convertSecondsToMinuteAndRemainingSeconds(seconds: Int): Pair<Int, Int>  {
        val minutes = Math.floor(seconds.toDouble() / 60.0).toInt()
        val remainingSeconds = seconds.rem(60)
        return Pair(minutes, remainingSeconds)
    }

    fun waterIt() {
        growthTimeout -= Settings.WATER_REDUCE_TIME_DURATION
    }
}