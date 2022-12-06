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

#ifndef SYNTHESIS_TAPPABLEAUDIOSOURCE_H
#define SYNTHESIS_TAPPABLEAUDIOSOURCE_H

#include <stdint.h>
#include "ITappable.h"

#include "IRenderableAudio.h"

/**
 * This class renders Float audio, but can be tapped to control.
 * It also contains members for sample rate and channel count
 */
class TappableAudioSource : public IRenderableAudio, public ITappable {
public:
    TappableAudioSource(int32_t sampleRate, int32_t channelCount) :
            mSampleRate(sampleRate), mChannelCount(channelCount) {}

    int32_t mSampleRate;
    int32_t mChannelCount;
};

#endif //SYNTHESIS_TAPPABLEAUDIOSOURCE_H
