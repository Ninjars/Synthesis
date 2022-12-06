/*
 * Copyright 2022 Jeremy Stevens, based on work by Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


#include <memory>
#include "utils/log.h"

#include "SynthesisEngine.h"

/**
 * Main audio engine for the Synthesis C++ audio synthesizer. It is responsible for:
 *
 * - Creating the callback object which will be supplied when constructing the audio stream
 * - Creating the playback stream, including setting the callback object
 * - Creating `Synth` which will render the audio inside the callback
 * - Starting the playback stream
 * - Restarting the playback stream when `restart()` is called by the callback object
 *
 * @param numSignals
 */
SynthesisEngine::SynthesisEngine(int32_t numSignals) {
    createCallback(numSignals);
}

SynthesisEngine::~SynthesisEngine() {
    if (mStream) {
        LOGE("SynthesisEngine destructor was called without calling stop()."
             "Please call stop() to ensure stream resources are not leaked.");
        stop();
    }
}

void SynthesisEngine::noteOff(int32_t noteIndex) {
    mSynth->noteOff(noteIndex);
}

void SynthesisEngine::noteOn(int32_t noteIndex) {
    mSynth->noteOn(noteIndex);
}

void SynthesisEngine::tap(bool isDown) {
    mSynth->tap(isDown);
}

void SynthesisEngine::restart() {
    stop();
    start();
}

// Create the playback stream
oboe::Result SynthesisEngine::createPlaybackStream() {
    oboe::AudioStreamBuilder builder;
    return builder.setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setFormat(oboe::AudioFormat::Float)
            ->setDataCallback(mDataCallback.get())
            ->setErrorCallback(mErrorCallback.get())
            ->openStream(mStream);
}

// Create the callback and set its thread affinity to the supplied CPU core IDs
void SynthesisEngine::createCallback(int32_t numSignals) {

    mDataCallback = std::make_shared<DefaultDataCallback>();

    // Create the error callback, we supply ourselves as the parent so that we can restart the stream
    // when it's disconnected
    mErrorCallback = std::make_shared<DefaultErrorCallback>(*this);

    mNumSignals = numSignals;
}

bool SynthesisEngine::start() {
    // It is possible for a stream's device to become disconnected during stream open or between
    // stream open and stream start.
    // If the stream fails to start, close the old stream and try again.
    bool didStart = false;
    int tryCount = 0;
    do {
        if (tryCount > 0) {
            usleep(20 * 1000); // Sleep between tries to give the system time to settle.
        }
        didStart = attemptStart();
    } while (!didStart && tryCount++ < 3);
    if (!didStart) {
        LOGE("Failed at starting the stream");
    }
    return didStart;
}

bool SynthesisEngine::attemptStart() {
    auto result = createPlaybackStream();

    if (result == Result::OK) {
        // Create our synthesizer audio source using the properties of the stream
        mSynth = Synth::create(mStream->getSampleRate(), mStream->getChannelCount(), mNumSignals);
        mDataCallback->reset();
        mDataCallback->setSource(std::dynamic_pointer_cast<IRenderableAudio>(mSynth));
        result = mStream->start();
        if (result == Result::OK) {
            return true;
        } else {
            LOGW("Failed attempt at starting the playback stream. Error: %s",
                 convertToText(result));
            return false;
        }
    } else {
        LOGW("Failed attempt at creating the playback stream. Error: %s", convertToText(result));
        return false;
    }
}

bool SynthesisEngine::stop() {
    if (mStream && mStream->getState() != oboe::StreamState::Closed) {
        mStream->stop();
        mStream->close();
    }
    mStream.reset();
    return true;
}
