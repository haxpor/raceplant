package io.wasin.raceplant.handlers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.graphics.g2d.Batch.*
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.GdxRuntimeException

/**
 * Created by haxpor on 6/26/17.
 */
class MinimapOrthogonalTiledMapRenderer(map: TiledMap): OrthogonalTiledMapRenderer(map) {

    init {
        // set to use minimap shader
        val shader = ShaderProgram(Gdx.files.internal("shader/minimap_vertex.glsl"), Gdx.files.internal("shader/minimap_fragment.glsl"))
        if (!shader.isCompiled) throw GdxRuntimeException("Couldn't compile shader: " + shader.log)

        // otherwise it's ok
        batch.shader = shader
    }

    override fun render() {
        // draw tiles that we want it to be in grayscale
        // optimized a little by not separating draw call into two as a need to set attribute for each type of layer
        // it's no need as we don't have different drawing mechanism for both
        beginRender()
        batch.shader.setUniformf("u_applyGrayScale", 1.0f, 1.0f)
        map.layers["floor"].let {
            if (it.isVisible) {
                renderLayer(it)
            }
        }
        endRender()

        // draw tiles that it's not grayscale and need colorize
        beginRender()
        batch.shader.setUniformf("u_applyGrayScale", 0.0f, 3.0f)
        map.layers["stockpiles"].let {
            if (it.isVisible) {
                renderLayer(it)
            }
        }
        endRender()

        beginRender()
        batch.shader.setUniformf("u_applyGrayScale", 0.0f, 2.0f)
        map.layers["water"].let {
            if (it.isVisible) {
                renderLayer(it)
            }
        }
        endRender()

        beginRender()
        batch.shader.setUniformf("u_applyGrayScale", 0.0f, 4.0f)
        map.layers["plantslot"].let {
            if (it.isVisible) {
                renderLayer(it)
            }
        }
        endRender()
    }

    private fun renderLayer(layer: MapLayer) {
        if (layer is TiledMapTileLayer) {
            renderTileLayer(layer)
        } else if (layer is TiledMapImageLayer) {
            renderImageLayer(layer)
        } else {
            renderObjects(layer)
        }
    }

    override fun renderTileLayer(layer: TiledMapTileLayer?) {

        if (layer == null) return

        val batchColor = batch.color
        val color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.opacity)

        val layerWidth = layer.width
        val layerHeight = layer.height

        val layerTileWidth = layer.tileWidth * unitScale
        val layerTileHeight = layer.tileHeight * unitScale

        val col1 = Math.max(0, (viewBounds.x / layerTileWidth).toInt())
        val col2 = Math.min(layerWidth, ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth).toInt())

        val row1 = Math.max(0, (viewBounds.y / layerTileHeight).toInt())
        val row2 = Math.min(layerHeight, ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight).toInt())

        var y = row2 * layerTileHeight
        val xStart = col1 * layerTileWidth
        val vertices = this.vertices

        for (row in row2 downTo row1) {
            var x = xStart
            for (col in col1..col2 - 1) {
                val cell = layer.getCell(col, row)
                if (cell == null) {
                    x += layerTileWidth
                    continue
                }
                val tile = cell.tile

                if (tile != null) {
                    val flipX = cell.flipHorizontally
                    val flipY = cell.flipVertically
                    val rotations = cell.rotation

                    val region = tile.textureRegion

                    val x1 = x + tile.offsetX * unitScale
                    val y1 = y + tile.offsetY * unitScale
                    val x2 = x1 + region.regionWidth * unitScale
                    val y2 = y1 + region.regionHeight * unitScale

                    val u1 = region.u
                    val v1 = region.v2
                    val u2 = region.u2
                    val v2 = region.v

                    vertices[X1] = x1
                    vertices[Y1] = y1
                    vertices[C1] = color
                    vertices[U1] = u1
                    vertices[V1] = v1

                    vertices[X2] = x1
                    vertices[Y2] = y2
                    vertices[C2] = color
                    vertices[U2] = u1
                    vertices[V2] = v2

                    vertices[X3] = x2
                    vertices[Y3] = y2
                    vertices[C3] = color
                    vertices[U3] = u2
                    vertices[V3] = v2

                    vertices[X4] = x2
                    vertices[Y4] = y1
                    vertices[C4] = color
                    vertices[U4] = u2
                    vertices[V4] = v1

                    if (flipX) {
                        var temp = vertices[U1]
                        vertices[U1] = vertices[U3]
                        vertices[U3] = temp
                        temp = vertices[U2]
                        vertices[U2] = vertices[U4]
                        vertices[U4] = temp
                    }
                    if (flipY) {
                        var temp = vertices[V1]
                        vertices[V1] = vertices[V3]
                        vertices[V3] = temp
                        temp = vertices[V2]
                        vertices[V2] = vertices[V4]
                        vertices[V4] = temp
                    }
                    if (rotations != 0) {
                        when (rotations) {
                            TiledMapTileLayer.Cell.ROTATE_90 -> {
                                val tempV = vertices[V1]
                                vertices[V1] = vertices[V2]
                                vertices[V2] = vertices[V3]
                                vertices[V3] = vertices[V4]
                                vertices[V4] = tempV

                                val tempU = vertices[U1]
                                vertices[U1] = vertices[U2]
                                vertices[U2] = vertices[U3]
                                vertices[U3] = vertices[U4]
                                vertices[U4] = tempU
                            }
                            TiledMapTileLayer.Cell.ROTATE_180 -> {
                                var tempU = vertices[U1]
                                vertices[U1] = vertices[U3]
                                vertices[U3] = tempU
                                tempU = vertices[U2]
                                vertices[U2] = vertices[U4]
                                vertices[U4] = tempU
                                var tempV = vertices[V1]
                                vertices[V1] = vertices[V3]
                                vertices[V3] = tempV
                                tempV = vertices[V2]
                                vertices[V2] = vertices[V4]
                                vertices[V4] = tempV
                            }
                            TiledMapTileLayer.Cell.ROTATE_270 -> {
                                val tempV = vertices[V1]
                                vertices[V1] = vertices[V4]
                                vertices[V4] = vertices[V3]
                                vertices[V3] = vertices[V2]
                                vertices[V2] = tempV

                                val tempU = vertices[U1]
                                vertices[U1] = vertices[U4]
                                vertices[U4] = vertices[U3]
                                vertices[U3] = vertices[U2]
                                vertices[U2] = tempU
                            }
                        }
                    }
                    batch.draw(region.texture, vertices, 0, BatchTiledMapRenderer.NUM_VERTICES)
                }
                x += layerTileWidth
            }
            y -= layerTileHeight
        }
    }
}