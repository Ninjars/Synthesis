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

#ifndef SYNTHESIS_IRESTARTABLE_H
#define SYNTHESIS_IRESTARTABLE_H

/**
 * Represents an object which can be restarted. For example an audio engine which has one or more
 * streams which can be restarted following a change in audio device configuration. For example,
 * headphones being connected.
 */
class IRestartable {
public:
    virtual void restart() = 0;
};

#endif //SYNTHESIS_IRESTARTABLE_H