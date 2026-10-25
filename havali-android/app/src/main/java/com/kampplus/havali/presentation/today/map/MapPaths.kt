package com.kampplus.havali.presentation.today.map

import androidx.compose.ui.graphics.Path
import com.kampplus.havali.domain.model.MapGeometry
import com.kampplus.havali.domain.model.MapLayer

/**
 * Geometriden bir kez çizim yolları (Path) üretir. Kamera hareket ederken yollar
 * yeniden kurulmaz; yalnızca tuval dönüşümü (ölçek, kaydırma) değişir.
 */
class MapPaths(geometry: MapGeometry) {

    private val paths: Map<MapLayer, Path> = geometry.layers.mapValues { (layer, rings) -> buildPath(rings, layer.closed) }

    operator fun get(layer: MapLayer): Path? = paths[layer]

    private fun buildPath(rings: List<FloatArray>, closed: Boolean): Path = Path().apply {
        rings.filter { it.size >= MIN_RING_VALUES }.forEach { ring ->
            moveTo(ring[0], ring[1])
            for (i in 2 until ring.size - 1 step 2) lineTo(ring[i], ring[i + 1])
            if (closed) close()
        }
    }

    private companion object {
        const val MIN_RING_VALUES = 4
    }
}
