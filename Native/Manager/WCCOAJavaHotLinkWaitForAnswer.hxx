#include <WCCOAJavaManager.hxx>
#include <Manager.hxx>
#include <HotLinkWaitForAnswer.hxx>
#include <jni.h>

class WCCOAJavaHotLinkWaitForAnswer : public HotLinkWaitForAnswer
{
private:
	jint jHdl;
public:
	void setjHdl(jint id);
	jint getjHdl();

	// Answer on connect
	virtual void hotLinkCallBack(DpMsgAnswer &answer);
	virtual void hotLinkCallBack(DpHLGroup &group);
};
