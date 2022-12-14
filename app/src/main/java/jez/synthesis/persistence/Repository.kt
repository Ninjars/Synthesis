package jez.synthesis.persistence

import androidx.compose.ui.unit.IntOffset
import jez.synthesis.audiotrack.Sampler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map

interface Repository {
    suspend fun storeSampler(sampler: Sampler)
    suspend fun getSampler(id: String): Sampler?
    suspend fun getSamplers(): Flow<List<Sampler>>
    suspend fun deleteSampler(id: String)
    fun saveInput(input: List<IntOffset>)
    fun getInput(): List<IntOffset>
}

class InMemoryRepository : Repository {
    private var input: List<IntOffset> = emptyList()
    private val samplers = MutableStateFlow(HashMap<String, Sampler>())

    override suspend fun storeSampler(sampler: Sampler) {
        samplers.value = samplers.value.apply { this[sampler.id] = sampler }
    }

    override suspend fun getSampler(id: String): Sampler? = samplers.value[id]

    override suspend fun getSamplers(): Flow<List<Sampler>> =
        samplers.asSharedFlow().map { it.values.toList() }

    override suspend fun deleteSampler(id: String) {
        samplers.value = samplers.value.apply { remove(id) }
    }

    override fun saveInput(input: List<IntOffset>) {
        this.input = input
    }

    override fun getInput(): List<IntOffset> = input
}
