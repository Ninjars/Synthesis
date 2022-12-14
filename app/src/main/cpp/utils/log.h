//
// Created by Jeremy Stevens on 06/12/2022.
//

#ifndef SYNTHESIS_LOG_H
#define SYNTHESIS_LOG_H

#include <android/log.h>

#define LOG_TAG "synthesis"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , LOG_TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , LOG_TAG,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , LOG_TAG,__VA_ARGS__)
#define LOGSIMPLE(...)

#endif //SYNTHESIS_LOG_H
