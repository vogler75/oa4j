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

#include "at_rocworks_oa4j_jni_Cns.hxx"
#include <WCCOAJavaManager.hxx>
#include <../LibJava/Java.hxx>

#include <CNSNode.hxx>
#include <CNSNodeNames.hxx>
#include <CNSNodeTree.hxx>
#include <CNSDataIdentifier.hxx>
#include <CNSUserData.hxx>
#include <CNSObserver.hxx>
#include <CommonNameService.hxx>
#include <Manager.hxx>
#include <DpIdentification.hxx>
#include <LangText.hxx>
#include <Blob.hxx>
#include <DpIdentifierVar.hxx>

#include <vector>
#include <map>

//=============================================================================
// Class name constants
//=============================================================================

const char *JCns::CnsNodeClassName = "at/rocworks/oa4j/var/CnsNode";
const char *JCns::CnsDataIdentifierClassName = "at/rocworks/oa4j/var/CnsDataIdentifier";

//=============================================================================
// JCnsNodeClass implementation
//=============================================================================

JCnsNodeClass::JCnsNodeClass(JNIEnv *env) : env(env) {
    cls = env->FindClass(JCns::CnsNodeClassName);
    if (cls == nullptr) {
        std::cerr << "JCnsNodeClass: Could not find class " << JCns::CnsNodeClassName << std::endl;
        return;
    }
    cls = (jclass)env->NewGlobalRef(cls);

    midInit = env->GetMethodID(cls, "<init>", "()V");
    midSetPath = env->GetMethodID(cls, "setPath", "(Ljava/lang/String;)V");
    midSetName = env->GetMethodID(cls, "setName", "(Ljava/lang/String;)V");
    midSetSystem = env->GetMethodID(cls, "setSystem", "(Ljava/lang/String;)V");
    midSetView = env->GetMethodID(cls, "setView", "(Ljava/lang/String;)V");
    midSetDisplayNames = env->GetMethodID(cls, "setDisplayNames", "(Lat/rocworks/oa4j/var/LangTextVar;)V");
    midSetDisplayPaths = env->GetMethodID(cls, "setDisplayPaths", "(Lat/rocworks/oa4j/var/LangTextVar;)V");
    midSetDpId = env->GetMethodID(cls, "setDpId", "(Lat/rocworks/oa4j/var/DpIdentifierVar;)V");
    midSetNodeType = env->GetMethodID(cls, "setNodeType", "(I)V");
    midSetUserData = env->GetMethodID(cls, "setUserData", "([B)V");
}

JCnsNodeClass::~JCnsNodeClass() {
    if (cls != nullptr) {
        env->DeleteGlobalRef(cls);
    }
}

//=============================================================================
// JCnsDataIdentifierClass implementation
//=============================================================================

JCnsDataIdentifierClass::JCnsDataIdentifierClass(JNIEnv *env) : env(env) {
    cls = env->FindClass(JCns::CnsDataIdentifierClassName);
    if (cls == nullptr) {
        std::cerr << "JCnsDataIdentifierClass: Could not find class " << JCns::CnsDataIdentifierClassName << std::endl;
        return;
    }
    cls = (jclass)env->NewGlobalRef(cls);

    midInit = env->GetMethodID(cls, "<init>", "()V");
    midSetDpId = env->GetMethodID(cls, "setDpId", "(Lat/rocworks/oa4j/var/DpIdentifierVar;)V");
    midSetType = env->GetMethodID(cls, "setType", "(I)V");
    midSetUserData = env->GetMethodID(cls, "setUserData", "([B)V");
}

JCnsDataIdentifierClass::~JCnsDataIdentifierClass() {
    if (cls != nullptr) {
        env->DeleteGlobalRef(cls);
    }
}

//=============================================================================
// JCns helper functions
//=============================================================================

CommonNameService* JCns::getCNS() {
    return WCCOAJavaManager::thisManager->getCNS();
}

ViewId JCns::getViewId(JNIEnv *env, jstring jSystem, jstring jViewName) {
    CommonNameService *cns = getCNS();
    if (cns == nullptr) {
        return ViewId();
    }

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);

    const char *viewStr = env->GetStringUTFChars(jViewName, nullptr);
    ViewId viewId = cns->getViewId(sysNum, viewStr);
    env->ReleaseStringUTFChars(jViewName, viewStr);

    return viewId;
}

jobject JCns::convertLangTextToJava(JNIEnv *env, const LangText &langText) {
    // Delegate to shared Java:: function
    return Java::convertToJava(env, langText);
}

LangText JCns::convertJavaToLangText(JNIEnv *env, jobject jLangText) {
    // Delegate to shared Java:: function
    return Java::convertJavaToLangText(env, jLangText);
}

CNSNodeNames JCns::convertJavaToCnsNodeNames(JNIEnv *env, jstring jName, jobject jDisplayNames) {
    const char *nameStr = env->GetStringUTFChars(jName, nullptr);
    CharString name(nameStr);
    env->ReleaseStringUTFChars(jName, nameStr);

    LangText displayNames;
    if (jDisplayNames != nullptr) {
        displayNames = convertJavaToLangText(env, jDisplayNames);
    }

    return CNSNodeNames(name, displayNames);
}

jobject JCns::convertCnsNodeNamesToJava(JNIEnv *env, const CNSNodeNames &names) {
    return convertLangTextToJava(env, names.getDisplayNames());
}

jobject JCns::convertToJava(JNIEnv *env, const CNSNode &node, JCnsNodeClass *jcnsNode) {
    JCnsNodeClass *localJcnsNode = jcnsNode;
    if (localJcnsNode == nullptr) {
        localJcnsNode = new JCnsNodeClass(env);
    }

    if (localJcnsNode->Class() == nullptr) {
        if (jcnsNode == nullptr) delete localJcnsNode;
        return nullptr;
    }

    // Create new CnsNode object
    jobject jNode = env->NewObject(localJcnsNode->Class(), localJcnsNode->Init());
    if (jNode == nullptr) {
        if (jcnsNode == nullptr) delete localJcnsNode;
        return nullptr;
    }

    // Set path
    CharString path;
    node.getPath(path);
    jstring jPath = env->NewStringUTF(path.c_str());
    env->CallVoidMethod(jNode, localJcnsNode->SetPath(), jPath);
    env->DeleteLocalRef(jPath);

    // Set name
    CharString name = node.getName();
    jstring jName = env->NewStringUTF(name.c_str());
    env->CallVoidMethod(jNode, localJcnsNode->SetName(), jName);
    env->DeleteLocalRef(jName);

    // Set system
    SystemNumType sysNum = node.getSystem();
    CharString sysName;
    Manager::getSystemName(sysNum, sysName);
    jstring jSystem = env->NewStringUTF(sysName.c_str());
    env->CallVoidMethod(jNode, localJcnsNode->SetSystem(), jSystem);
    env->DeleteLocalRef(jSystem);

    // Set view
    ViewId viewId = node.getView();
    CommonNameService *cns = getCNS();
    if (cns != nullptr) {
        CNSNodeNames viewNames;
        cns->getViewNames(sysNum, viewId, viewNames);
        CharString viewName = viewNames.getName();
        jstring jView = env->NewStringUTF(viewName.c_str());
        env->CallVoidMethod(jNode, localJcnsNode->SetView(), jView);
        env->DeleteLocalRef(jView);
    }

    // Set display names
    LangText displayNames = node.getDisplayNames();
    jobject jDisplayNames = convertLangTextToJava(env, displayNames);
    if (jDisplayNames != nullptr) {
        env->CallVoidMethod(jNode, localJcnsNode->SetDisplayNames(), jDisplayNames);
        env->DeleteLocalRef(jDisplayNames);
    }

    // Set display paths
    LangText displayPaths = node.getDisplayPaths();
    jobject jDisplayPaths = convertLangTextToJava(env, displayPaths);
    if (jDisplayPaths != nullptr) {
        env->CallVoidMethod(jNode, localJcnsNode->SetDisplayPaths(), jDisplayPaths);
        env->DeleteLocalRef(jDisplayPaths);
    }

    // Set data identifier (DpId and node type)
    const CNSDataIdentifier &dataId = node.getDataIdentifier();
    DpIdentifierVar dpIdVar;
    if (dataId.getData(dpIdVar)) {
        // getData returns the DpIdentifierVar for DATAPOINT type
        DpIdentifier dpId = dpIdVar.getValue();
        jobject jDpId = Java::convertToJava(env, dpId);
        if (jDpId != nullptr) {
            env->CallVoidMethod(jNode, localJcnsNode->SetDpId(), jDpId);
            env->DeleteLocalRef(jDpId);
        }
    }
    env->CallVoidMethod(jNode, localJcnsNode->SetNodeType(), (jint)dataId.isA());

    // Set user data
    const PVSSuchar *userData = node.getUserDataPtr();
    unsigned int userDataLen = node.getUserDataLen();
    if (userData != nullptr && userDataLen > 0) {
        jbyteArray jUserData = env->NewByteArray(userDataLen);
        env->SetByteArrayRegion(jUserData, 0, userDataLen, (jbyte*)userData);
        env->CallVoidMethod(jNode, localJcnsNode->SetUserData(), jUserData);
        env->DeleteLocalRef(jUserData);
    }

    if (jcnsNode == nullptr) delete localJcnsNode;
    return jNode;
}

jobject JCns::convertToJava(JNIEnv *env, const CNSDataIdentifier &id, JCnsDataIdentifierClass *jcnsId) {
    JCnsDataIdentifierClass *localJcnsId = jcnsId;
    if (localJcnsId == nullptr) {
        localJcnsId = new JCnsDataIdentifierClass(env);
    }

    if (localJcnsId->Class() == nullptr) {
        if (jcnsId == nullptr) delete localJcnsId;
        return nullptr;
    }

    // Create new CnsDataIdentifier object
    jobject jDataId = env->NewObject(localJcnsId->Class(), localJcnsId->Init());
    if (jDataId == nullptr) {
        if (jcnsId == nullptr) delete localJcnsId;
        return nullptr;
    }

    // Set DpId using getData which returns DpIdentifierVar
    DpIdentifierVar dpIdVar;
    if (id.getData(dpIdVar)) {
        DpIdentifier dpId = dpIdVar.getValue();
        jobject jDpId = Java::convertToJava(env, dpId);
        if (jDpId != nullptr) {
            env->CallVoidMethod(jDataId, localJcnsId->SetDpId(), jDpId);
            env->DeleteLocalRef(jDpId);
        }
    }

    // Set type
    env->CallVoidMethod(jDataId, localJcnsId->SetType(), (jint)id.isA());

    // Set user data
    Blob userData = id.getUserData();
    if (userData.getLen() > 0) {
        jbyteArray jUserData = env->NewByteArray(userData.getLen());
        env->SetByteArrayRegion(jUserData, 0, userData.getLen(), (jbyte*)userData.getData());
        env->CallVoidMethod(jDataId, localJcnsId->SetUserData(), jUserData);
        env->DeleteLocalRef(jUserData);
    }

    if (jcnsId == nullptr) delete localJcnsId;
    return jDataId;
}

//=============================================================================
// JNI Functions - View Management
//=============================================================================

extern "C" {

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsCreateView
(JNIEnv *env, jobject, jstring jSystem, jstring jViewId, jstring jSeparator, jobject jDisplayNames)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return -1;

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);

    const char *viewIdStr = env->GetStringUTFChars(jViewId, nullptr);
    const char *sepStr = env->GetStringUTFChars(jSeparator, nullptr);

    // Create node names for view
    CharString viewName(viewIdStr);
    LangText displayNames;
    if (jDisplayNames != nullptr) {
        displayNames = JCns::convertJavaToLangText(env, jDisplayNames);
    }
    CNSNodeNames viewNames(viewName, displayNames);

    // Create separator LangText (same separator for all languages)
    LangText separators;
    for (int i = 0; i < separators.getNoOfLanguages(); i++) {
        separators.setText(i, sepStr);
    }

    PVSSboolean result = cns->createView(sysNum, viewNames, separators);

    env->ReleaseStringUTFChars(jViewId, viewIdStr);
    env->ReleaseStringUTFChars(jSeparator, sepStr);

    return result ? 0 : -1;
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsDeleteView
(JNIEnv *env, jobject, jstring jSystem, jstring jViewId)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return -1;

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);
    ViewId viewId = JCns::getViewId(env, jSystem, jViewId);

    PVSSboolean result = cns->deleteView(sysNum, viewId);
    return result ? 0 : -1;
}

JNIEXPORT jobjectArray JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsGetViews
(JNIEnv *env, jobject, jstring jSystem)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return nullptr;

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);

    ViewIdVector views;
    if (!cns->getViews(sysNum, views)) {
        return nullptr;
    }

    // Convert to Java String array
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray jViews = env->NewObjectArray(views.size(), stringClass, nullptr);

    for (size_t i = 0; i < views.size(); i++) {
        CNSNodeNames viewNames;
        cns->getViewNames(sysNum, views[i], viewNames);
        CharString viewName = viewNames.getName();
        jstring jViewName = env->NewStringUTF(viewName.c_str());
        env->SetObjectArrayElement(jViews, i, jViewName);
        env->DeleteLocalRef(jViewName);
    }

    return jViews;
}

JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsGetViewDisplayNames
(JNIEnv *env, jobject, jstring jSystem, jstring jViewId)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return nullptr;

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);
    ViewId viewId = JCns::getViewId(env, jSystem, jViewId);

    CNSNodeNames viewNames;
    if (!cns->getViewNames(sysNum, viewId, viewNames)) {
        return nullptr;
    }

    return JCns::convertLangTextToJava(env, viewNames.getDisplayNames());
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsChangeViewDisplayNames
(JNIEnv *env, jobject, jstring jSystem, jstring jViewId, jobject jDisplayNames)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return -1;

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);
    ViewId viewId = JCns::getViewId(env, jSystem, jViewId);

    // Get current view name
    CNSNodeNames currentNames;
    cns->getViewNames(sysNum, viewId, currentNames);

    // Create new names with updated display names
    LangText displayNames = JCns::convertJavaToLangText(env, jDisplayNames);
    CNSNodeNames newNames(currentNames.getName(), displayNames);

    PVSSboolean result = cns->changeView(sysNum, viewId, newNames);
    return result ? 0 : -1;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsGetViewSeparators
(JNIEnv *env, jobject, jstring jSystem, jstring jViewId)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return nullptr;

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);
    ViewId viewId = JCns::getViewId(env, jSystem, jViewId);

    LangText separators;
    if (!cns->getSeparators(sysNum, viewId, separators)) {
        return nullptr;
    }

    // Return first language separator (they should all be the same)
    const char *sep = separators.getText(0);
    return sep ? env->NewStringUTF(sep) : nullptr;
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsChangeViewSeparators
(JNIEnv *env, jobject, jstring jSystem, jstring jViewId, jstring jSeparator)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return -1;

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);
    ViewId viewId = JCns::getViewId(env, jSystem, jViewId);

    const char *sepStr = env->GetStringUTFChars(jSeparator, nullptr);

    LangText separators;
    for (int i = 0; i < separators.getNoOfLanguages(); i++) {
        separators.setText(i, sepStr);
    }

    PVSSboolean result = cns->changeView(sysNum, viewId, separators);

    env->ReleaseStringUTFChars(jSeparator, sepStr);
    return result ? 0 : -1;
}

//=============================================================================
// JNI Functions - Tree Management
//=============================================================================

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsAddTree
(JNIEnv *env, jobject, jstring jSystem, jstring jViewId, jstring jNodeId, jint jNodeType,
 jobject jDpId, jobject jDisplayNames)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return -1;

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);
    ViewId viewId = JCns::getViewId(env, jSystem, jViewId);

    // Create node names
    const char *nodeIdStr = env->GetStringUTFChars(jNodeId, nullptr);
    CharString nodeName(nodeIdStr);
    env->ReleaseStringUTFChars(jNodeId, nodeIdStr);

    LangText displayNames;
    if (jDisplayNames != nullptr) {
        displayNames = JCns::convertJavaToLangText(env, jDisplayNames);
    }
    CNSNodeNames nodeNames(nodeName, displayNames);

    // Create data identifier
    DpIdentifier dpId;
    if (jDpId != nullptr) {
        Java::convertJDpIdentifierToDpIdentifier(env, jDpId, dpId);
    }
    CNSDataIdentifier dataId(dpId, (CNSDataIdentifier::Types)jNodeType);

    // Create tree with single root node
    CNSNodeTree tree(nodeNames, dataId);

    PVSSboolean result = cns->addTree(sysNum, viewId, tree);
    return result ? 0 : -1;
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsDeleteTree
(JNIEnv *env, jobject, jstring jCnsPath)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return -1;

    const char *pathStr = env->GetStringUTFChars(jCnsPath, nullptr);

    CNSNode node;
    if (!cns->getNode(pathStr, node)) {
        env->ReleaseStringUTFChars(jCnsPath, pathStr);
        return -1;
    }

    PVSSboolean result = cns->deleteTree(node);
    env->ReleaseStringUTFChars(jCnsPath, pathStr);
    return result ? 0 : -1;
}

JNIEXPORT jobjectArray JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsGetTrees
(JNIEnv *env, jobject, jstring jSystem, jstring jViewId)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return nullptr;

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);
    ViewId viewId = JCns::getViewId(env, jSystem, jViewId);

    CNSNodeVector trees;
    if (!cns->getTrees(sysNum, viewId, trees)) {
        return nullptr;
    }

    // Convert to Java String array (paths)
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray jTrees = env->NewObjectArray(trees.size(), stringClass, nullptr);

    for (size_t i = 0; i < trees.size(); i++) {
        CharString path;
        trees[i].getPath(path);
        jstring jPath = env->NewStringUTF(path.c_str());
        env->SetObjectArrayElement(jTrees, i, jPath);
        env->DeleteLocalRef(jPath);
    }

    return jTrees;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsGetRoot
(JNIEnv *env, jobject, jstring jCnsPath)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return nullptr;

    const char *pathStr = env->GetStringUTFChars(jCnsPath, nullptr);

    CNSNode node;
    if (!cns->getNode(pathStr, node)) {
        env->ReleaseStringUTFChars(jCnsPath, pathStr);
        return nullptr;
    }

    CNSNode root;
    if (!cns->getRoot(node, root)) {
        env->ReleaseStringUTFChars(jCnsPath, pathStr);
        return nullptr;
    }

    CharString rootPath;
    root.getPath(rootPath);

    env->ReleaseStringUTFChars(jCnsPath, pathStr);
    return env->NewStringUTF(rootPath.c_str());
}

//=============================================================================
// JNI Functions - Node Management
//=============================================================================

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsAddNode
(JNIEnv *env, jobject, jstring jParentPath, jstring jNodeId, jint jNodeType,
 jobject jDpId, jobject jDisplayNames)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return -1;

    const char *parentPathStr = env->GetStringUTFChars(jParentPath, nullptr);

    CNSNode parent;
    if (!cns->getNode(parentPathStr, parent)) {
        env->ReleaseStringUTFChars(jParentPath, parentPathStr);
        return -1;
    }
    env->ReleaseStringUTFChars(jParentPath, parentPathStr);

    // Create node names
    const char *nodeIdStr = env->GetStringUTFChars(jNodeId, nullptr);
    CharString nodeName(nodeIdStr);
    env->ReleaseStringUTFChars(jNodeId, nodeIdStr);

    LangText displayNames;
    if (jDisplayNames != nullptr) {
        displayNames = JCns::convertJavaToLangText(env, jDisplayNames);
    }
    CNSNodeNames nodeNames(nodeName, displayNames);

    // Create data identifier
    DpIdentifier dpId;
    if (jDpId != nullptr) {
        Java::convertJDpIdentifierToDpIdentifier(env, jDpId, dpId);
    }
    CNSDataIdentifier dataId(dpId, (CNSDataIdentifier::Types)jNodeType);

    PVSSboolean result = cns->addNode(parent, nodeNames, dataId);
    return result ? 0 : -1;
}

JNIEXPORT jobjectArray JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsGetChildren
(JNIEnv *env, jobject, jstring jCnsPath)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return nullptr;

    const char *pathStr = env->GetStringUTFChars(jCnsPath, nullptr);

    CNSNode node;
    if (!cns->getNode(pathStr, node)) {
        env->ReleaseStringUTFChars(jCnsPath, pathStr);
        return nullptr;
    }
    env->ReleaseStringUTFChars(jCnsPath, pathStr);

    CNSNodeVector children;
    if (!cns->getChildren(node, children)) {
        return nullptr;
    }

    // Convert to Java String array (paths)
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray jChildren = env->NewObjectArray(children.size(), stringClass, nullptr);

    for (size_t i = 0; i < children.size(); i++) {
        CharString path;
        children[i].getPath(path);
        jstring jPath = env->NewStringUTF(path.c_str());
        env->SetObjectArrayElement(jChildren, i, jPath);
        env->DeleteLocalRef(jPath);
    }

    return jChildren;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsGetParent
(JNIEnv *env, jobject, jstring jCnsPath)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return nullptr;

    const char *pathStr = env->GetStringUTFChars(jCnsPath, nullptr);

    CNSNode node;
    if (!cns->getNode(pathStr, node)) {
        env->ReleaseStringUTFChars(jCnsPath, pathStr);
        return nullptr;
    }

    CNSNode parent;
    if (!cns->getParent(node, parent)) {
        env->ReleaseStringUTFChars(jCnsPath, pathStr);
        return nullptr;
    }

    CharString parentPath;
    parent.getPath(parentPath);

    env->ReleaseStringUTFChars(jCnsPath, pathStr);
    return env->NewStringUTF(parentPath.c_str());
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsChangeNodeData
(JNIEnv *env, jobject, jstring jCnsPath, jobject jDpId, jint jNodeType)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return -1;

    const char *pathStr = env->GetStringUTFChars(jCnsPath, nullptr);

    CNSNode node;
    if (!cns->getNode(pathStr, node)) {
        env->ReleaseStringUTFChars(jCnsPath, pathStr);
        return -1;
    }
    env->ReleaseStringUTFChars(jCnsPath, pathStr);

    DpIdentifier dpId;
    if (jDpId != nullptr) {
        Java::convertJDpIdentifierToDpIdentifier(env, jDpId, dpId);
    }
    CNSDataIdentifier dataId(dpId, (CNSDataIdentifier::Types)jNodeType);

    PVSSboolean result = cns->changeNode(node, dataId);
    return result ? 0 : -1;
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsChangeNodeDisplayNames
(JNIEnv *env, jobject, jstring jCnsPath, jobject jDisplayNames)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return -1;

    const char *pathStr = env->GetStringUTFChars(jCnsPath, nullptr);

    CNSNode node;
    if (!cns->getNode(pathStr, node)) {
        env->ReleaseStringUTFChars(jCnsPath, pathStr);
        return -1;
    }
    env->ReleaseStringUTFChars(jCnsPath, pathStr);

    // Create new names with current ID name but new display names
    CharString currentName = node.getName();
    LangText displayNames = JCns::convertJavaToLangText(env, jDisplayNames);
    CNSNodeNames newNames(currentName, displayNames);

    PVSSboolean result = cns->changeNode(node, newNames);
    return result ? 0 : -1;
}

//=============================================================================
// JNI Functions - Query & Navigation
//=============================================================================

JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsGetNode
(JNIEnv *env, jobject, jstring jCnsPath)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return nullptr;

    const char *pathStr = env->GetStringUTFChars(jCnsPath, nullptr);

    CNSNode node;
    if (!cns->getNode(pathStr, node)) {
        env->ReleaseStringUTFChars(jCnsPath, pathStr);
        return nullptr;
    }

    env->ReleaseStringUTFChars(jCnsPath, pathStr);
    return JCns::convertToJava(env, node);
}

JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsGetId
(JNIEnv *env, jobject, jstring jCnsPath)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return nullptr;

    const char *pathStr = env->GetStringUTFChars(jCnsPath, nullptr);

    CNSDataIdentifier dataId;
    if (!cns->getId(pathStr, dataId)) {
        env->ReleaseStringUTFChars(jCnsPath, pathStr);
        return nullptr;
    }

    env->ReleaseStringUTFChars(jCnsPath, pathStr);
    return JCns::convertToJava(env, dataId);
}

JNIEXPORT jobjectArray JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsGetNodesByName
(JNIEnv *env, jobject, jstring jSystem, jstring jViewId, jstring jPattern, jint jSearchMode, jint jLangIdx)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return nullptr;

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);
    ViewId viewId = JCns::getViewId(env, jSystem, jViewId);

    const char *patternStr = env->GetStringUTFChars(jPattern, nullptr);

    CNSNodeVector nodes;
    if (!cns->getNodes(patternStr, sysNum, viewId,
                       (CommonNameService::SearchMode)jSearchMode,
                       (LanguageIdType)jLangIdx,
                       CNSDataIdentifier::ALL_TYPES, nodes)) {
        env->ReleaseStringUTFChars(jPattern, patternStr);
        return nullptr;
    }
    env->ReleaseStringUTFChars(jPattern, patternStr);

    // Convert to Java String array (paths)
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray jNodes = env->NewObjectArray(nodes.size(), stringClass, nullptr);

    for (size_t i = 0; i < nodes.size(); i++) {
        CharString path;
        nodes[i].getPath(path);
        jstring jPath = env->NewStringUTF(path.c_str());
        env->SetObjectArrayElement(jNodes, i, jPath);
        env->DeleteLocalRef(jPath);
    }

    return jNodes;
}

JNIEXPORT jobjectArray JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsGetNodesByData
(JNIEnv *env, jobject, jstring jSystem, jstring jViewId, jobject jDpId)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return nullptr;

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);
    ViewId viewId = JCns::getViewId(env, jSystem, jViewId);

    DpIdentifier dpId;
    if (!Java::convertJDpIdentifierToDpIdentifier(env, jDpId, dpId)) {
        return nullptr;
    }
    CNSDataIdentifier dataId(dpId);

    CNSNodeVector nodes;
    if (!cns->getNodes(dataId, sysNum, viewId, nodes)) {
        return nullptr;
    }

    // Convert to Java String array (paths)
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray jNodes = env->NewObjectArray(nodes.size(), stringClass, nullptr);

    for (size_t i = 0; i < nodes.size(); i++) {
        CharString path;
        nodes[i].getPath(path);
        jstring jPath = env->NewStringUTF(path.c_str());
        env->SetObjectArrayElement(jNodes, i, jPath);
        env->DeleteLocalRef(jPath);
    }

    return jNodes;
}

JNIEXPORT jstring JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsSubStr
(JNIEnv *env, jobject, jstring jCnsPath, jint jMask, jboolean jResolve)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return nullptr;

    const char *pathStr = env->GetStringUTFChars(jCnsPath, nullptr);
    CharString path(pathStr);

    CharString result = cns->subStr(path, jMask, jResolve);

    env->ReleaseStringUTFChars(jCnsPath, pathStr);
    return env->NewStringUTF(result.c_str());
}

//=============================================================================
// JNI Functions - System Names
//=============================================================================

JNIEXPORT jobject JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsGetSystemNames
(JNIEnv *env, jobject, jstring jSystem)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return nullptr;

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);

    CNSNodeNames sysNames;
    if (!cns->getSystemNames(sysNum, sysNames)) {
        return nullptr;
    }

    return JCns::convertLangTextToJava(env, sysNames.getDisplayNames());
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsSetSystemNames
(JNIEnv *env, jobject, jstring jSystem, jobject jDisplayNames)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return -1;

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);
    LangText displayNames = JCns::convertJavaToLangText(env, jDisplayNames);

    PVSSboolean result = cns->setSystemNames(sysNum, displayNames);
    return result ? 0 : -1;
}

//=============================================================================
// JNI Functions - Validation
//=============================================================================

JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsCheckId
(JNIEnv *env, jobject, jstring jId)
{
    const char *idStr = env->GetStringUTFChars(jId, nullptr);
    PVSSboolean result = CNSNodeNames::isLegalName(idStr);
    env->ReleaseStringUTFChars(jId, idStr);
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsCheckName
(JNIEnv *env, jobject, jstring jDisplayName)
{
    const char *nameStr = env->GetStringUTFChars(jDisplayName, nullptr);
    PVSSboolean result = CNSNodeNames::isLegalDisplayName(nameStr);
    env->ReleaseStringUTFChars(jDisplayName, nameStr);
    return result ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsCheckSeparator
(JNIEnv *env, jobject, jchar jSeparator)
{
    PVSSboolean result = CNSNodeNames::isLegalDisplayNameSeparator((char)jSeparator);
    return result ? JNI_TRUE : JNI_FALSE;
}

//=============================================================================
// JNI Functions - getIdSet
//=============================================================================

JNIEXPORT jobjectArray JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsGetIdSet
(JNIEnv *env, jobject, jstring jSystem, jstring jViewId, jstring jPattern, jint jSearchMode, jint jLangIdx)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return nullptr;

    SystemNumType sysNum = Java::parseSystemNum(env, jSystem);
    ViewId viewId = JCns::getViewId(env, jSystem, jViewId);

    const char *patternStr = env->GetStringUTFChars(jPattern, nullptr);

    CNSDataIdentifierSet idSet;
    if (!cns->getIdSet(patternStr, sysNum, viewId,
                       (CommonNameService::SearchMode)jSearchMode,
                       (LanguageIdType)jLangIdx,
                       CNSDataIdentifier::ALL_TYPES, idSet)) {
        env->ReleaseStringUTFChars(jPattern, patternStr);
        return nullptr;
    }
    env->ReleaseStringUTFChars(jPattern, patternStr);

    // Convert to Java DpIdentifierVar array
    jclass dpIdClass = env->FindClass("at/rocworks/oa4j/var/DpIdentifierVar");
    jobjectArray jIdSet = env->NewObjectArray(idSet.size(), dpIdClass, nullptr);

    int i = 0;
    for (CNSDataIdentifierSet::const_iterator it = idSet.begin(); it != idSet.end(); ++it, ++i) {
        DpIdentifierVar dpIdVar;
        if (it->getData(dpIdVar)) {
            DpIdentifier dpId = dpIdVar.getValue();
            jobject jDpId = Java::convertToJava(env, dpId);
            if (jDpId != nullptr) {
                env->SetObjectArrayElement(jIdSet, i, jDpId);
                env->DeleteLocalRef(jDpId);
            }
        }
    }

    return jIdSet;
}

} // extern "C"

//=============================================================================
// JCnsObserver Implementation
//=============================================================================

int JCnsObserver::nextObserverId = 1;
std::map<int, JCnsObserver*> JCnsObserver::observers;

JCnsObserver::JCnsObserver(JNIEnv *env, jobject javaObserver)
    : observerId(nextObserverId++), jvm(nullptr), jObserver(nullptr), midOnChange(nullptr)
{
    // Get the JVM reference for later use in callbacks
    env->GetJavaVM(&jvm);

    // Create a global reference to the Java observer object
    jObserver = env->NewGlobalRef(javaObserver);

    // Get the callback method ID
    jclass observerClass = env->GetObjectClass(javaObserver);
    midOnChange = env->GetMethodID(observerClass, "onCnsChange", "(Ljava/lang/String;I)V");

    if (midOnChange == nullptr) {
        std::cerr << "JCnsObserver: Could not find onCnsChange method" << std::endl;
    }

    // Register this observer
    observers[observerId] = this;
}

JCnsObserver::~JCnsObserver()
{
    // Remove from registry
    observers.erase(observerId);

    // Release the global reference
    if (jObserver != nullptr && jvm != nullptr) {
        JNIEnv *env = nullptr;
        bool attached = false;

        // Get or attach to the current thread
        int status = jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
        if (status == JNI_EDETACHED) {
            if (jvm->AttachCurrentThread((void**)&env, nullptr) == 0) {
                attached = true;
            }
        }

        if (env != nullptr) {
            env->DeleteGlobalRef(jObserver);
        }

        if (attached) {
            jvm->DetachCurrentThread();
        }
    }
}

void JCnsObserver::update(const CharString &path, CNSChanges what, const DpMsgManipCNS &msg)
{
    if (jvm == nullptr || jObserver == nullptr || midOnChange == nullptr) {
        return;
    }

    JNIEnv *env = nullptr;
    bool attached = false;

    // Get or attach to the current thread
    int status = jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
    if (status == JNI_EDETACHED) {
        if (jvm->AttachCurrentThread((void**)&env, nullptr) == 0) {
            attached = true;
        } else {
            std::cerr << "JCnsObserver::update: Failed to attach thread" << std::endl;
            return;
        }
    }

    if (env == nullptr) {
        return;
    }

    // Create Java string for path
    jstring jPath = env->NewStringUTF(path.c_str());

    // Call the Java callback
    env->CallVoidMethod(jObserver, midOnChange, jPath, (jint)what);

    // Clean up
    env->DeleteLocalRef(jPath);

    // Check for exceptions
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();
    }

    if (attached) {
        jvm->DetachCurrentThread();
    }
}

JCnsObserver* JCnsObserver::getObserver(int id)
{
    auto it = observers.find(id);
    return (it != observers.end()) ? it->second : nullptr;
}

void JCnsObserver::removeObserver(int id)
{
    auto it = observers.find(id);
    if (it != observers.end()) {
        delete it->second;  // Destructor will remove from map
    }
}

//=============================================================================
// JNI Functions - Observer Management
//=============================================================================

extern "C" {

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsAddObserver
(JNIEnv *env, jobject, jobject jObserver)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return -1;

    if (jObserver == nullptr) return -1;

    // Create the C++ observer wrapper
    JCnsObserver *observer = new JCnsObserver(env, jObserver);

    // Register with CNS (addObserver returns void, so just call it)
    cns->addObserver(*observer);

    return observer->getId();
}

JNIEXPORT jint JNICALL Java_at_rocworks_oa4j_jni_Manager_apiCnsRemoveObserver
(JNIEnv *env, jobject, jint jObserverId)
{
    CommonNameService *cns = JCns::getCNS();
    if (cns == nullptr) return -1;

    JCnsObserver *observer = JCnsObserver::getObserver(jObserverId);
    if (observer == nullptr) return -1;

    // Unregister from CNS
    cns->removeObserver(*observer);

    // Delete the observer (removes from registry)
    delete observer;

    return 0;
}

} // extern "C"
