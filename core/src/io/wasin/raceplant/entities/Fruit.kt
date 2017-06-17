package io.wasin.raceplant.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array

/**
 * Created by haxpor on 6/17/17.
 */
class Fruit(texture: Texture, x: Float, y: Float, aliveToScore: Boolean = false): Sprite(texture, SPRITE_SIZE, SPRITE_SIZE) {

    companion object {
        const val SPRITE_SIZE: Int = 16
        const val EFFECT_DURATION: Float = 2.0f
    }

    // animation
    private var idleAnimation: Animation<TextureRegion>
    private var animationTimer: Float = 0f

    // create it then score right away? (from input in constructure)
    private var aliveToScore: Boolean = aliveToScore
    private var targetScale: Float = 1.5f
    private var effectTimer: Float = 0.0f

    var isAlive: Boolean = true
    var isMarkedAsCollected: Boolean = aliveToScore

    init {
        // populate animations
        val tmpFrames = TextureRegion.split(texture, Player.SPRITE_SIZE, Player.SPRITE_SIZE)

        // idle
        val idleFrames = Array<TextureRegion>()
        for (col in 0..2) {
            idleFrames.add(tmpFrames[0][col])
        }
        idleAnimation = Animation<TextureRegion>(1 / 7f, idleFrames, Animation.PlayMode.LOOP)

        // place on input position
        this.x = x
        this.y = y
    }

    constructor(texture: Texture): this(texture, 0f, 0f) {}

    fun update(dt: Float) {
        if (isAlive) {
            animationTimer += dt

            if (aliveToScore) {
                effectTimer += dt

                val progress = effectTimer / EFFECT_DURATION
                setScale(progress + 1.0f)
                setAlpha(1.0f - progress)

                if (effectTimer > EFFECT_DURATION) {
                    isAlive = false
                }
            }

            // set texture region
            var currentFrameRegion = idleAnimation.getKeyFrame(animationTimer)

            if (currentFrameRegion != null) {
                setRegion(currentFrameRegion!!)
            }
        }
    }
}