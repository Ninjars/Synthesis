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

#ifndef SYNTHESIS_ENGINE_H
#define SYNTHESIS_ENGINE_H


#include <oboe/Oboe.h>
#include <vector>
#include "utils/IRestartable.h"

#include "utils/DefaultDataCallback.h"
#include "utils/DefaultErrorCallback.h"
#include "utils/TappableAudioSource.h"
#include "Synth.h"

using namespace oboe;

class SynthesisEngine : public IRestartable {

public:
    SynthesisEngine(int32_t numSignals);

    virtual ~SynthesisEngine();

    void noteOff(int32_t noteIndex);

    void noteOn(int32_t noteIndex);

    void tap(bool isDown);

    // from IRestartable
    virtual void restart() override;

    bool start();

    bool stop();

private:
    int32_t mNumSignals;

    std::shared_ptr<AudioStream> mStream;
    std::shared_ptr<Synth> mSynth;
    std::shared_ptr<DefaultDataCallback> mDataCallback;
    std::shared_ptr<DefaultErrorCallback> mErrorCallback;

    bool attemptStart();

    oboe::Result createPlaybackStream();

    void createCallback(int32_t numSignals);
};


#endif //SYNTHESIS_ENGINE_H