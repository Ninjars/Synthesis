
package jez.synthesis.audiotrack;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

/**
 * Adapted from https://github.com/MasterEx/BeatKeeper/blob/master/src/pntanasis/android/metronome/AudioGenerator.java
 * <p>
 * Lines 25 and 31-38 were originally found here:
 * http://stackoverflow.com/questions/2413426/playing-an-arbitrary-tone-with-android
 * which came from here:
 * http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
 */
public class AudioGenerator {

    final int sampleRate;
    private AudioTrack audioTrack;

    public AudioGenerator(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    private byte[] get16BitPcm(double[] samples) {
        byte[] generatedSound = new byte[2 * samples.length];
        int index = 0;
        for (double sample : samples) {
            // scale to maximum amplitude
            short maxSample = (short) ((sample * Short.MAX_VALUE));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSound[index++] = (byte) (maxSample & 0x00ff);
            generatedSound[index++] = (byte) ((maxSample & 0xff00) >>> 8);

        }
        return generatedSound;
    }

    public void createPlayer(int sessionId) {
        audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build(),
                new AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                sampleRate,
                AudioTrack.MODE_STREAM,
                sessionId
        );
        audioTrack.play();
    }

    public void writeSound(double[] samples) {
        byte[] generatedSnd = get16BitPcm(samples);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
    }

    public void destroyAudioTrack() {
        audioTrack.stop();
        audioTrack.release();
    }
}
