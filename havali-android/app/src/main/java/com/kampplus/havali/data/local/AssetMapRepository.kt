package com.kampplus.havali.data.local

import android.content.res.AssetManager
import com.google.gson.Gson
import com.kampplus.havali.data.local.dto.MapFileDto
import com.kampplus.havali.data.local.dto.MapLayerDto
import com.kampplus.havali.domain.model.MapGeometry
import com.kampplus.havali.domain.model.MapLayer
import com.kampplus.havali.domain.repository.MapRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Gerçek Türkiye haritası uygulamanın içinde gelir (assets): internet olmadan da çizilir.
 *
 * ~0,5 MB'lık JSON'u okumak ve ayrıştırmak ana thread'i dondurabileceği için iş
 * [ioDispatcher] üzerinde yapılır. Sonuç bir kez okunur ve bellekte tutulur.
 */
class AssetMapRepository(
    private val assets: AssetManager,
    private val gson: Gson,
    private val ioDispatcher: CoroutineDispatcher
) : MapRepository {

    private val mutex = Mutex()
    private var cached: MapGeometry? = null

    override suspend fun loadGeometry(): MapGeometry = mutex.withLock {
        cached ?: read().also { cached = it }
    }

    private suspend fun read(): MapGeometry = withContext(ioDispatcher) {
        val file = assets.open(MAP_ASSET).bufferedReader().use { gson.fromJson(it, MapFileDto::class.java) }
        MapGeometry(file.layers.orEmpty().mapNotNull { it.toLayerEntry() }.toMap())
    }

    /** Bilinmeyen katman adı (ileride dosyaya eklenen bir katman) sessizce atlanır. */
    private fun MapLayerDto.toLayerEntry(): Pair<MapLayer, List<FloatArray>>? {
        val layer = MapLayer.entries.firstOrNull { it.name == id } ?: return null
        return layer to rings.orEmpty().map { it.toFloatArray() }
    }

    private companion object {
        const val MAP_ASSET = "map/turkiye_map.json"
    }
}
