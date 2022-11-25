package jez.synthesis.persistence

import jez.synthesis.audiotrack.Sampler
import kotlinx.coroutines.flow.MutableStateFlow

interface Repository {
    suspend fun storeSampler(sampler: Sampler)
    suspend fun getSampler(id: String): Sampler?
    suspend fun deleteSampler(id: String)
}

class InMemoryRepository : Repository {
    private val samplers = MutableStateFlow(HashMap<String, Sampler>())

    override suspend fun storeSampler(sampler: Sampler) {
        samplers.value = samplers.value.apply { this[sampler.id] }
    }

    override suspend fun getSampler(id: String): Sampler? = samplers.value[id]

    override suspend fun deleteSampler(id: String) {
        samplers.value = samplers.value.apply { remove(id) }
    }
}
