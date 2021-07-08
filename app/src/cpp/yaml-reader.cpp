#include <jni.h>
#include <string>
#include <fstream>
#include "yaml-cpp/yaml.h"
#include "include/androidLogCommand.h"


YAML::Node merge_nodes(YAML::Node a, YAML::Node b);

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

extern "C"
JNIEXPORT void JNICALL
Java_com_dashboard_kotlin_clashhelper_clashConfig_mergeFile(JNIEnv *env, jobject thiz,
                                                            jstring jmainFilePath,
                                                            jstring jtemplatePath,
                                                            jstring joutputFilePath) {
    try {

        jboolean isCopy;
        const char *mainFilePath = env->GetStringUTFChars(jmainFilePath, &isCopy);
        LOGV("isCopy jpath:%d", isCopy)
        LOGV("mainFilePath: %s", mainFilePath)
        const char *templatePath = env->GetStringUTFChars(jtemplatePath, &isCopy);
        LOGV("isCopy jpath:%d", isCopy)
        LOGV("templatePath: %s", templatePath)
        const char *outputFilePath = env->GetStringUTFChars(joutputFilePath, &isCopy);
        LOGV("isCopy jpath:%d", isCopy)
        LOGV("outputFilePath: %s", outputFilePath)

        YAML::Node mainFileObj = YAML::LoadFile(mainFilePath);
        YAML::Node templateObj = YAML::LoadFile(templatePath);

        YAML::Node outputObj = merge_nodes(mainFileObj, templateObj);

        std::ofstream fileOut(outputFilePath);
        fileOut << outputObj;

    } catch (const std::exception &e) {
        LOGE("%s", e.what())
    }

}

inline const YAML::Node &cnode(const YAML::Node &n) {
    return n;
}

YAML::Node merge_nodes(YAML::Node a, YAML::Node b) {
    if (!b.IsMap()) {
        // If b is not a map, merge result is b, unless b is null
        return b.IsNull() ? a : b;
    }
    if (!a.IsMap()) {
        // If a is not a map, merge result is b
        return b;
    }
    if (!b.size()) {
        // If a is a map, and b is an empty map, return a
        return a;
    }
    // Create a new map 'c' with the same mappings as a, merged with b
    auto c = YAML::Node(YAML::NodeType::Map);
    for (auto n : a) {
        if (n.first.IsScalar()) {
            const std::string &key = n.first.Scalar();
            auto t = YAML::Node(cnode(b)[key]);
            if (t) {
                c[n.first] = merge_nodes(n.second, t);
                continue;
            }
        }
        c[n.first] = n.second;
    }
    // Add the mappings from 'b' not already in 'c'
    for (auto n : b) {
        if (!n.first.IsScalar() || !cnode(c)[n.first.Scalar()]) {
            c[n.first] = n.second;
        }
    }
    return c;
}



