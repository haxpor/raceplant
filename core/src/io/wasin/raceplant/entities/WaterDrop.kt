package io.wasin.raceplant.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array

/**
 * Created by haxpor on 6/17/17.
 */
class WaterDrop(texture: Texture, x: Float, y: Float): Sprite(texture, SPRITE_SIZE, SPRITE_SIZE) {

    companion object {
        const val SPRITE_SIZE: Int = 32
    }

    private var aliveTimeout: Float = 3f
    var isAlive: Boolean = true
        private set

    init {
        // place on input position
        this.x = x
        this.y = y
    }

    constructor(texture: Texture): this(texture, 0f, 0f) {}

    fun update(dt: Float) {
        if (isAlive) {
            aliveTimeout -= dt

            if (aliveTimeout < 0f) {
                isAlive = false
            }
        }
    }
}