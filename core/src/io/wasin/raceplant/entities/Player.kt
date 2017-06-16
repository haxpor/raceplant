package io.wasin.raceplant.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array

/**
 * Created by haxpor on 6/16/17.
 */
class Player(id: Int, texture: Texture): Sprite(texture, SPRITE_SIZE, SPRITE_SIZE) {

    var frontVector: Vector2 = Vector2(1f, 0f)

    // animations
    private var idleAnimation: Animation<TextureRegion>
    private var walkAnimation: Animation<TextureRegion>
    private var animationTimer: Float = 0f

    // internal oeration
    private var state: State = State.IDLE
    private var faceRight: Boolean = true

    enum class State {
        IDLE,
        WALK
    }

    companion object {
        const val SPRITE_SIZE: Int = 16
    }

    init {
        // populate animations
        val tmpFrames = TextureRegion.split(texture, SPRITE_SIZE, SPRITE_SIZE)

        // idle
        val idleFrames = Array<TextureRegion>()
        for (col in 4..5) {
            idleFrames.add(tmpFrames[0][col])
        }
        idleAnimation = Animation<TextureRegion>(1 / 5f, idleFrames, Animation.PlayMode.LOOP)

        // walk
        var walkFrames = Array<TextureRegion>()
        for (col in 0..3) {
            walkFrames.add(tmpFrames[0][col])
        }
        walkAnimation = Animation<TextureRegion>(1 / 7f, walkFrames, Animation.PlayMode.LOOP)
    }

    fun update(dt: Float) {
        animationTimer += dt

        // set texture region
        var currentFrameRegion: TextureRegion? = null
        when {
            state == State.IDLE -> {
                currentFrameRegion = idleAnimation.getKeyFrame(animationTimer)
            }
            state == State.WALK -> {
                currentFrameRegion = walkAnimation.getKeyFrame(animationTimer)
            }
        }

        if (currentFrameRegion != null) {
            setRegion(currentFrameRegion!!)
        }

        // update flip x-direction
        if (faceRight) {
            flip(false, isFlipY)
        }
        else {
            flip(true, isFlipY)
        }
    }

    fun setState(state: State) {
        this.state = state
    }

    fun faceLeft() {
        faceRight = false
    }

    fun faceRight() {
        faceRight = true
    }
}