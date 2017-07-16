package io.wasin.raceplant.handlers

import com.badlogic.gdx.utils.Disposable
import io.wasin.raceplant.Game
import io.wasin.raceplant.states.*

import java.util.Stack

/**
 * Created by haxpor on 5/14/17.
 */

class GameStateManager(game: Game): Disposable {
    var game: Game
        private set
    private var gameStates: Stack<GameState>

    init {
        this.game = game
        this.gameStates = Stack<GameState>()
    }

    companion object {
        const val PLAY = 5000
    }

    fun update(dt: Float) {
        this.gameStates.peek().update(dt)
    }

    fun resize(width: Int, height: Int) {
        this.gameStates.peek().resize(width, height)
    }

    fun render() {
        for (state in this.gameStates) {
            state.render()
        }
    }

    private fun getState(state: Int): GameState? {
        if (state == PLAY)  return Play(this)

        return null
    }

    fun setState(state: Int) {
        this.gameStates.forEach { it.dispose() }
        this.gameStates.clear()
        this.pushState(state)
    }

    fun pushState(state: Int) {
        this.gameStates.push(this.getState(state))
    }

    fun popState() {
        val g = this.gameStates.pop()
        g.dispose()
    }

    override fun dispose() {
        gameStates.forEach { it.dispose() }
    }
}