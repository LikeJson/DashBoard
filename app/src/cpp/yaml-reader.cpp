#include <jni.h>
#include <string>
#include <fstream>
#include "yaml-cpp/yaml.h"
#include "include/androidLogCommand.h"


extern "C"
JNIEXPORT jstring JNICALL
Java_com_dashboard_kotlin_clashhelper_clashConfig_getFromFile(JNIEnv *env, jobject thiz,
                                                              jstring jpath, jstring jnode) {
    try {
        jboolean isCopy;
        const char *filePath = env->GetStringUTFChars(jpath, &isCopy);
        LOGV("isCopy jpath:%d", isCopy)
        LOGV("path: %s", filePath)
        const char *node = env->GetStringUTFChars(jnode, &isCopy);
        LOGV("isCopy jpath:%d", isCopy)
        LOGV("node: %s", node)


        YAML::Node config = YAML::LoadFile(filePath);
        const std::string secret = config[node].as<std::string>();
        LOGV("result: %s", secret.c_str())

        return env->NewStringUTF(secret.c_str());
    } catch (const std::exception &e) {
        LOGE("%s", e.what())
        return env->NewStringUTF("");
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_dashboard_kotlin_clashhelper_clashConfig_modifyFile(JNIEnv *env, jobject thiz,
                                                             jstring jpath, jstring jnode,
                                                             jstring jvalue) {
    try {
        jboolean isCopy;
        const char *filePath = env->GetStringUTFChars(jpath, &isCopy);
        LOGV("isCopy jpath:%d", isCopy)
        LOGV("path: %s", filePath)
        const char *node = env->GetStringUTFChars(jnode, &isCopy);
        LOGV("isCopy jpath:%d", isCopy)
        LOGV("node: %s", node)
        const char *value = env->GetStringUTFChars(jvalue, &isCopy);
        LOGV("isCopy jpath:%d", isCopy)
        LOGV("value: %s", value)

        YAML::Node config = YAML::LoadFile(filePath);
        config[node] = value;

        std::ofstream fileOut(filePath);
        fileOut << config;
    } catch (const std::exception &e) {
        LOGE("%s", e.what())
    }
}