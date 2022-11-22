package jez.synthesis.audio

import nl.igorski.mwengine.MWEngine
import nl.igorski.mwengine.core.Notifications.ids.BOUNCE_COMPLETE
import nl.igorski.mwengine.core.Notifications.ids.ERROR_HARDWARE_UNAVAILABLE
import nl.igorski.mwengine.core.Notifications.ids.ERROR_THREAD_START
import nl.igorski.mwengine.core.Notifications.ids.MARKER_POSITION_REACHED
import nl.igorski.mwengine.core.Notifications.ids.RECORDED_SNIPPET_READY
import nl.igorski.mwengine.core.Notifications.ids.RECORDED_SNIPPET_SAVED
import nl.igorski.mwengine.core.Notifications.ids.RECORDING_COMPLETED
import nl.igorski.mwengine.core.Notifications.ids.SEQUENCER_POSITION_UPDATED
import nl.igorski.mwengine.core.Notifications.ids.SEQUENCER_TEMPO_UPDATED
import nl.igorski.mwengine.core.Notifications.ids.STATUS_BRIDGE_CONNECTED
import nl.igorski.mwengine.core.Notifications.ids.values
import timber.log.Timber

class MWEngineStateObserver(private val hardwareUnavailableCallback: () -> Unit) :
    MWEngine.IObserver {
    // cache notification ids from native layer
    private val notificationEnums = values()

    override fun handleNotification(notificationId: Int) {
        Timber.d("handleNotification ${notificationEnums[notificationId]}")
        when (notificationEnums[notificationId]) {
            ERROR_HARDWARE_UNAVAILABLE -> {
                Timber.e("received driver error callback from native layer")
                hardwareUnavailableCallback()
            }
            MARKER_POSITION_REACHED -> Timber.d("Marker position has been reached")
            SEQUENCER_POSITION_UPDATED -> Timber.d("position update")
            RECORDING_COMPLETED,
            SEQUENCER_TEMPO_UPDATED,
            RECORDED_SNIPPET_READY,
            RECORDED_SNIPPET_SAVED,
            BOUNCE_COMPLETE,
            STATUS_BRIDGE_CONNECTED -> Unit
            ERROR_THREAD_START -> Timber.d("ERROR_THREAD_START")
        }
    }

    override fun handleNotification(notificationId: Int, notificationValue: Int) {
        when (notificationEnums[notificationId]) {
            SEQUENCER_POSITION_UPDATED -> {
                // for this notification id, the notification value describes the precise buffer offset of the
                // engine when the notification fired (as a value in the range of 0 - BUFFER_SIZE). using this value
                // we can calculate the amount of samples pending until the next step position is reached
                // which in turn allows us to calculate the engine latency
//                val sequencerPosition: Int = _sequencerController.getStepPosition()
//                val elapsedSamples: Int = _sequencerController.getBufferPosition()
            }
            else -> Unit
        }
    }
}
