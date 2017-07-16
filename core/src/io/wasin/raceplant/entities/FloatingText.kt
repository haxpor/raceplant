package io.wasin.raceplant.entities

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable

/**
 * Created by haxpor on 6/18/17.
 */
class FloatingText(text: String, startingPosX: Float, startingPosY: Float): Disposable {
    private var font: BitmapFont = BitmapFont()
    private var glyph: GlyphLayout = GlyphLayout()
    var text: String = text
        private set

    private var pos: Vector2 = Vector2(startingPosX, startingPosY)

    var speed: Float = 20.0f
    var fadeTimeout: Float = 2.0f
        set(value) {
            _fadeTimeout = value
            field = value
        }

    private var _fadeTimeout: Float  = fadeTimeout
    private var alpha: Float = 1.0f
    private val alphaSpeed: Float = 0.1f

    var isAlive: Boolean = true
        private set

    init {
        glyph.setText(font, text)
    }

    fun update(dt: Float) {
        if (isAlive) {
            _fadeTimeout -= dt

            // calculate new alpha value
            alpha = _fadeTimeout / fadeTimeout

            // update position floating towards upper direction
            pos.add(0f, dt * speed)

            if (alpha < 0f) {
                isAlive = false
            }
        }
    }

    fun render(sb: SpriteBatch) {
        font.setColor(1f, 1f, 1f, alpha)
        font.draw(sb, glyph, pos.x, pos.y)
    }

    override fun dispose() {
        font.dispose()
    }
}