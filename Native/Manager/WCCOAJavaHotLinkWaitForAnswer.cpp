#include <WCCOAJavaHotLinkWaitForAnswer.hxx>
#include <../LibJava/Java.hxx>

void WCCOAJavaHotLinkWaitForAnswer::setjHdl(jint id)
{
	//std::cout << "setjHdl=" << id << std::endl;
	jHdl = id;

}

jint WCCOAJavaHotLinkWaitForAnswer::getjHdl()
{
	//std::cout << "getjHdl=" << jHdl << std::endl;
	return jHdl;
}

void WCCOAJavaHotLinkWaitForAnswer::hotLinkCallBack(DpMsgAnswer &answer)
{
	Java::dpIdMutex.lock();
	((WCCOAJavaManager *)Manager::getManPtr())->handleHotLink(getjHdl(), answer);
	Java::dpIdMutex.unlock();
}

void WCCOAJavaHotLinkWaitForAnswer::hotLinkCallBack(DpHLGroup &group)
{
	Java::dpIdMutex.lock();
	((WCCOAJavaManager *)Manager::getManPtr())->handleHotLink(getjHdl(), group);
	Java::dpIdMutex.unlock();
}