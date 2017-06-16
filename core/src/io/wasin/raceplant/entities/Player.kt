package io.wasin.raceplant.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2

/**
 * Created by haxpor on 6/16/17.
 */
class Player(id: Int, texture: Texture): Sprite(texture, 16, 16) {

    var frontVector: Vector2 = Vector2(1f, 0f)

    init {
        setRegion(TextureRegion(texture, 16, 16))
    }

    fun update(dt: Float) {

    }

}