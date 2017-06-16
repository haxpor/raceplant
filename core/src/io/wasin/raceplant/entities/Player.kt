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

    enum class State {
        IDLE,
        WALK,
        CARRY
    }

    companion object {
        const val SPRITE_SIZE: Int = 16
    }

    var frontVector: Vector2 = Vector2(1f, 0f)

    // animations
    private var idleAnimation: Animation<TextureRegion>
    private var walkAnimation: Animation<TextureRegion>
    private var carryAnimation: Animation<TextureRegion>
    private var animationTimer: Float = 0f

    // public variables
    var state: State = State.IDLE
    var treePlanted: Int = 0

    // internal operation
    private var faceRight: Boolean = true

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

        // carry
        var carryFrames = Array<TextureRegion>()
        for (col in 6..9) {
            carryFrames.add(tmpFrames[0][col])
        }
        carryAnimation = Animation<TextureRegion>(1 / 7f, carryFrames, Animation.PlayMode.LOOP)
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
            state == State.CARRY -> {
                currentFrameRegion = carryAnimation.getKeyFrame(animationTimer)
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

    fun faceLeft() {
        faceRight = false
    }

    fun faceRight() {
        faceRight = true
    }

    // take into account if player is carrying at the moment, thus player will walk while carrying
    fun walk() {

        if (state != State.CARRY) {
            state = State.WALK
        }
    }

    fun increaseTreePlanted() {
        treePlanted++
    }
}