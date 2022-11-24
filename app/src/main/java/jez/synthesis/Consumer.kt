package jez.synthesis

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember


interface Consumer<T> {
    fun accept(value: T)
}

@Composable
fun <T> rememberEventConsumer(consumer: Consumer<T>) =
    remember<(T) -> Unit>(consumer) { { consumer.accept(it) } }
