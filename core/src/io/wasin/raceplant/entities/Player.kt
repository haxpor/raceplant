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
        CARRY_SEED,
        CARRY_FRUIT,
        CARRY_EMPTYBUCKET,
        CARRY_FULLBUCKET
    }

    companion object {
        const val SPRITE_SIZE: Int = 16
    }

    var frontVector: Vector2 = Vector2(1f, 0f)

    // animations
    private var idleAnimation: Animation<TextureRegion>
    private var walkAnimation: Animation<TextureRegion>
    private var carrySeedAnimation: Animation<TextureRegion>
    private var carryDBallAnimation: Animation<TextureRegion>
    private var carryEmptyBucketAnimation: Animation<TextureRegion>
    private var carryFullBucketAnimation: Animation<TextureRegion>
    private var animationTimer: Float = 0f

    // public variables
    var state: State = State.IDLE
    var treePlanted: Int = 0
        private set
    var fruitCollected: Int = 0
        private set

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

        // carry seed
        var carryFrames = Array<TextureRegion>()
        for (col in 6..9) {
            carryFrames.add(tmpFrames[0][col])
        }
        carrySeedAnimation = Animation<TextureRegion>(1 / 7f, carryFrames, Animation.PlayMode.LOOP)

        // carry dball
        var carryDBallFrames = Array<TextureRegion>()
        for (col in 10..13) {
            carryDBallFrames.add(tmpFrames[0][col])
        }
        carryDBallAnimation = Animation<TextureRegion>(1 / 7f, carryDBallFrames, Animation.PlayMode.LOOP)

        // carry empty bucket
        var carryEmptyBucketFrames = Array<TextureRegion>()
        for (col in 14..17) {
            carryEmptyBucketFrames.add(tmpFrames[0][col])
        }
        carryEmptyBucketAnimation = Animation<TextureRegion>(1 / 7f, carryEmptyBucketFrames, Animation.PlayMode.LOOP)

        // carry full bucket
        var carryFullBucketFrames = Array<TextureRegion>()
        for (col in 18..21) {
            carryFullBucketFrames.add(tmpFrames[0][col])
        }
        carryFullBucketAnimation = Animation<TextureRegion>(1 / 7f, carryFullBucketFrames, Animation.PlayMode.LOOP)
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
            state == State.CARRY_SEED -> {
                currentFrameRegion = carrySeedAnimation.getKeyFrame(animationTimer)
            }
            state == State.CARRY_FRUIT -> {
                currentFrameRegion = carryDBallAnimation.getKeyFrame(animationTimer)
            }
            state == State.CARRY_EMPTYBUCKET -> {
                currentFrameRegion = carryEmptyBucketAnimation.getKeyFrame(animationTimer)
            }
            state == State.CARRY_FULLBUCKET -> {
                currentFrameRegion = carryFullBucketAnimation.getKeyFrame(animationTimer)
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

        if (!isCarry()) {
            state = State.WALK
        }
    }

    fun increaseTreePlanted() {
        treePlanted++
    }

    fun increaseCollectedFruit() {
        fruitCollected++
    }

    fun isCarry(): Boolean {
        if (state == State.CARRY_SEED ||
                state == State.CARRY_FRUIT ||
                state == State.CARRY_EMPTYBUCKET ||
                state == State.CARRY_FULLBUCKET) {
            return true
        }
        else {
            return false
        }
    }

    fun resetState() {
        fruitCollected = 0
        treePlanted = 0
        state = State.IDLE
        faceRight()
    }
}