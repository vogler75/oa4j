#include <WCCOAJavaAlertHotLinkWaitForAnswer.hxx>
#include <../LibJava/Java.hxx>

void WCCOAJavaAlertHotLinkWaitForAnswer::setjHdl(jint id)
{
	//std::cout << "setjHdl=" << id << std::endl;
	jHdl = id;

}

jint WCCOAJavaAlertHotLinkWaitForAnswer::getjHdl()
{
	//std::cout << "getjHdl=" << jHdl << std::endl;
	return jHdl;
}

void WCCOAJavaAlertHotLinkWaitForAnswer::alertHotLinkCallBack(DpMsgAnswer &answer)
{
	Java::dpIdMutex.lock();
	((WCCOAJavaManager *)Manager::getManPtr())->handleHotLink(getjHdl(), answer);
	Java::dpIdMutex.unlock();

}

void WCCOAJavaAlertHotLinkWaitForAnswer::alertHotLinkCallBack(AlertAttrList &group)
{
	Java::dpIdMutex.lock();
	((WCCOAJavaManager *)Manager::getManPtr())->handleHotLink(getjHdl(), group);
	Java::dpIdMutex.unlock();
}