package io.wasin.raceplant.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array

/**
 * Created by haxpor on 6/17/17.
 */
class Tree(texture: Texture, x: Float, y: Float): Sprite(texture, SPRITE_WIDTH, SPRITE_HEIGHT) {

    companion object {
        const val SPRITE_WIDTH: Int = 32
        const val SPRITE_HEIGHT: Int = 96
    }

    // animation
    private var growStep1Animation: Animation<TextureRegion>
    private var growStep2Animation: Animation<TextureRegion>
    private var growStep3Animation: Animation<TextureRegion>
    private var animationTimer: Float = 0f

    private var state: State = State.GROW_STEP_1
    private var randomFlipX: Boolean = false

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

        // randomize to flipx for its whole life time
        randomFlipX = MathUtils.randomBoolean()
    }

    constructor(texture: Texture): this(texture, 0f, 0f) {}

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

        // update flip x-direction from randomization
        flip(false, isFlipY)
    }

    fun growIntoStep1() {
        state = State.GROW_STEP_1
    }

    fun growIntoStep2() {
        state = State.GROW_STEP_2
    }

    fun growIntoStep3() {
        state = State.GROW_STEP_3
    }
}