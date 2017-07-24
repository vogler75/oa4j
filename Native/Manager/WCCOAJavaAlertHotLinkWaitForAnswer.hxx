#include <WCCOAJavaManager.hxx>
#include <Manager.hxx>
#include <AlertHotLinkWaitForAnswer.hxx>
#include <jni.h>

class WCCOAJavaAlertHotLinkWaitForAnswer : public AlertHotLinkWaitForAnswer
{
private:
	jint jHdl;
public:
	void setjHdl(jint id);
	jint getjHdl();

	virtual void alertHotLinkCallBack(DpMsgAnswer &answer);

protected:
	virtual void alertHotLinkCallBack(AlertAttrList &group);
};
