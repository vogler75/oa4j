/*
OA4J - WinCC Open Architecture for Java
Copyright (C) 2017 Andreas Vogler

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

#ifndef _at_rocworks_oa4j_jni_Cns_H_
#define _at_rocworks_oa4j_jni_Cns_H_

#include <jni.h>
#include <CNSNode.hxx>
#include <CNSNodeNames.hxx>
#include <CNSDataIdentifier.hxx>
#include <CNSObserver.hxx>
#include <CommonNameService.hxx>
#include <LangText.hxx>
#include <map>

/**
 * Helper class for CNS Java class references and method IDs.
 * Caches class/method lookups for performance.
 */
class JCnsNodeClass {
private:
    JNIEnv *env;
    jclass cls;
    jmethodID midInit;
    jmethodID midSetPath;
    jmethodID midSetName;
    jmethodID midSetSystem;
    jmethodID midSetView;
    jmethodID midSetDisplayNames;
    jmethodID midSetDisplayPaths;
    jmethodID midSetDpId;
    jmethodID midSetNodeType;
    jmethodID midSetUserData;

public:
    JCnsNodeClass(JNIEnv *env);
    ~JCnsNodeClass();

    jclass Class() { return cls; }
    jmethodID Init() { return midInit; }
    jmethodID SetPath() { return midSetPath; }
    jmethodID SetName() { return midSetName; }
    jmethodID SetSystem() { return midSetSystem; }
    jmethodID SetView() { return midSetView; }
    jmethodID SetDisplayNames() { return midSetDisplayNames; }
    jmethodID SetDisplayPaths() { return midSetDisplayPaths; }
    jmethodID SetDpId() { return midSetDpId; }
    jmethodID SetNodeType() { return midSetNodeType; }
    jmethodID SetUserData() { return midSetUserData; }
};

/**
 * Helper class for CNS DataIdentifier Java class.
 */
class JCnsDataIdentifierClass {
private:
    JNIEnv *env;
    jclass cls;
    jmethodID midInit;
    jmethodID midSetDpId;
    jmethodID midSetType;
    jmethodID midSetUserData;

public:
    JCnsDataIdentifierClass(JNIEnv *env);
    ~JCnsDataIdentifierClass();

    jclass Class() { return cls; }
    jmethodID Init() { return midInit; }
    jmethodID SetDpId() { return midSetDpId; }
    jmethodID SetType() { return midSetType; }
    jmethodID SetUserData() { return midSetUserData; }
};

/**
 * CNS JNI helper functions.
 */
class JCns {
public:
    static const char *CnsNodeClassName;
    static const char *CnsDataIdentifierClassName;

    /**
     * Convert a C++ CNSNode to a Java CnsNode object.
     */
    static jobject convertToJava(JNIEnv *env, const CNSNode &node, JCnsNodeClass *jcnsNode = nullptr);

    /**
     * Convert a C++ CNSDataIdentifier to a Java CnsDataIdentifier object.
     */
    static jobject convertToJava(JNIEnv *env, const CNSDataIdentifier &id, JCnsDataIdentifierClass *jcnsId = nullptr);

    /**
     * Convert a C++ LangText to a Java LangTextVar object.
     */
    static jobject convertLangTextToJava(JNIEnv *env, const LangText &langText);

    /**
     * Convert a Java LangTextVar to a C++ LangText.
     */
    static LangText convertJavaToLangText(JNIEnv *env, jobject jLangText);

    /**
     * Convert a Java CNSNodeNames (viewId + displayNames) from Java strings.
     */
    static CNSNodeNames convertJavaToCnsNodeNames(JNIEnv *env, jstring jName, jobject jDisplayNames);

    /**
     * Convert a C++ CNSNodeNames to a Java LangTextVar (display names only).
     */
    static jobject convertCnsNodeNamesToJava(JNIEnv *env, const CNSNodeNames &names);

    /**
     * Get CommonNameService instance from Manager.
     */
    static CommonNameService* getCNS();

    /**
     * Get ViewId from view name string.
     */
    static ViewId getViewId(JNIEnv *env, jstring jSystem, jstring jViewName);
};

/**
 * Java CNS Observer - bridges C++ CNSObserver to Java callback.
 * Each instance holds a global reference to a Java object that receives callbacks.
 */
class JCnsObserver : public CNSObserver {
private:
    int observerId;
    JavaVM *jvm;
    jobject jObserver;      // Global reference to Java observer object
    jmethodID midOnChange;  // Method ID for onCnsChange callback

    static int nextObserverId;
    static std::map<int, JCnsObserver*> observers;

public:
    JCnsObserver(JNIEnv *env, jobject javaObserver);
    virtual ~JCnsObserver();

    int getId() const { return observerId; }

    /**
     * Called by WinCC OA when CNS changes occur.
     * This method bridges to the Java callback.
     */
    virtual void update(const CharString &path, CNSChanges what, const DpMsgManipCNS &msg) override;

    /**
     * Get observer by ID.
     */
    static JCnsObserver* getObserver(int id);

    /**
     * Remove observer from registry.
     */
    static void removeObserver(int id);
};

#endif // _at_rocworks_oa4j_jni_Cns_H_
