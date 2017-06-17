package io.wasin.raceplant.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array

/**
 * Created by haxpor on 6/17/17.
 */
class Bucket(texture: Texture, x: Float, y: Float, state: State): Sprite(texture, SPRITE_SIZE, SPRITE_SIZE) {

    companion object {
        const val SPRITE_SIZE: Int = 16
    }

    enum class State {
        EMPTY,
        FULL
    }

    // animation
    private var emptyAnimation: Animation<TextureRegion>
    private var fullAnimation: Animation<TextureRegion>
    private var animationTimer: Float = 0f

    var state: State = state
        private set

    init {
        // populate animations
        val tmpFrames = TextureRegion.split(texture, Bucket.SPRITE_SIZE, Bucket.SPRITE_SIZE)

        // empty frames
        val emptyFrames = Array<TextureRegion>()
        for (col in 0..1) {
            emptyFrames.add(tmpFrames[0][col])
        }
        emptyAnimation = Animation<TextureRegion>(1 / 7f, emptyFrames, Animation.PlayMode.LOOP)

        // full frames
        val fullFrames = Array<TextureRegion>()
        for (col in 2..3) {
            fullFrames.add(tmpFrames[0][col])
        }
        fullAnimation = Animation<TextureRegion>(1 / 7f, fullFrames, Animation.PlayMode.LOOP)

        // place on input position
        this.x = x
        this.y = y
    }

    constructor(texture: Texture): this(texture, 0f, 0f, State.EMPTY) {}

    fun update(dt: Float) {
        animationTimer += dt

        // set texture region
        var currentFrameRegion: TextureRegion? = null
        when {
            state == State.EMPTY -> {
                currentFrameRegion = emptyAnimation.getKeyFrame(animationTimer)
            }
            state == State.FULL -> {
                currentFrameRegion = fullAnimation.getKeyFrame(animationTimer)
            }
        }

        if (currentFrameRegion != null) {
            setRegion(currentFrameRegion!!)
        }
    }

    fun makeItEmpty() {
        state = State.EMPTY
    }

    fun makeItFull() {
        state = State.FULL
    }
}