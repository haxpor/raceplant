package io.wasin.raceplant.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array

/**
 * Created by haxpor on 6/17/17.
 */
class WaterFilled(texture: Texture, x: Float, y: Float): Sprite(texture, SPRITE_SIZE, SPRITE_SIZE) {

    companion object {
        const val SPRITE_SIZE: Int = 16
        const val EFFECT_DURATION: Float = 3.0f
    }

    // animation
    private var idleAnimation: Animation<TextureRegion>
    private var animationTimer: Float = 0f

    private var effectTimer: Float = 0.0f

    var isAlive: Boolean = true

    init {
        // populate animations
        val tmpFrames = TextureRegion.split(texture, Player.SPRITE_SIZE, Player.SPRITE_SIZE)

        // idle
        val idleFrames = Array<TextureRegion>()
        for (col in 0..0) {
            idleFrames.add(tmpFrames[0][col])
        }
        idleAnimation = Animation<TextureRegion>(1 / 4f, idleFrames, Animation.PlayMode.NORMAL)

        // place on input position
        this.x = x
        this.y = y
    }

    constructor(texture: Texture): this(texture, 0f, 0f) {}

    fun update(dt: Float) {
        if (isAlive) {
            animationTimer += dt

            effectTimer += dt

            val progress = effectTimer / EFFECT_DURATION
            setScale(progress + 1.0f)
            setAlpha(1.0f - progress)

            if (effectTimer > EFFECT_DURATION) {
                isAlive = false
            }

            // set texture region
            var currentFrameRegion = idleAnimation.getKeyFrame(animationTimer)

            if (currentFrameRegion != null) {
                setRegion(currentFrameRegion!!)
            }
        }
    }
}