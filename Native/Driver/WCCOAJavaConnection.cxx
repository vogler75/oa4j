// WCCOAJavaConnection.cxx

#include <PVSSMacros.hxx>

#include <WCCOAJavaConnection.hxx>
#include <WCCOAJavaHWService.hxx>
#include <WCCOAJavaDrv.hxx>
#include <WCCOAJavaResources.hxx>

WCCOAJavaConnection::WCCOAJavaConnection(DpIdType id) : HmiConnection(id)
{
  DEBUG_MSG("creating new connection with  id " << id);
}

WCCOAJavaConnection::~WCCOAJavaConnection()
{
}

/** the open connection callback.
  * possible state changes are HMI_CS_Failure and HMI_CS_Connected
  */
void WCCOAJavaConnection::open()
{
  DEBUG_MSG("opening connection with name " << getMyName());
  requestStateChange(HMI_CS_Connected);
}

/** the close connection callback.
  * there are no active state changes possible here
  */
void WCCOAJavaConnection::close()
{
  DEBUG_MSG("closing connection with name " << getMyName());
}

/** the open server side callback.
  * possible state changes are HMI_CS_Failure and HMI_CS_Listening
  */
void WCCOAJavaConnection::openServer()
{
  DEBUG_MSG("opening server connection with name " << getMyName() << " - will fail");
  requestStateChange(HMI_CS_Failure);
}

/** the close server side callback.
  * there are no active state changes possible here
  */
void WCCOAJavaConnection::closeServer()
{
  DEBUG_MSG("closing server connection with name " << getMyName());
}

/** the general query callback.
  * this callback is triggered whenever a general query should be done.
  * the default implementation issues singleQuery calls for all input and IO elements.
  */
void WCCOAJavaConnection::doGeneralQuery()
{
  DEBUG_MSG("GQ requested on connection with name " << getMyName());
}

/** the internal general query callback.
  * this callback is triggered whenever an internal general query should be done.
  * the default implementation issues write requests calls for all output and IO elements.
  */
void WCCOAJavaConnection::doInternalGeneralQuery()
{
  DEBUG_MSG("IGQ requested on connection with name " << getMyName());
}

/** the alert sync callback.
  * this callback is triggered whenever a general alert sync should be done.
  */
void WCCOAJavaConnection::doAlertSync()
{
  DEBUG_MSG("AlertSync requested on connection with name " << getMyName());
}

bool WCCOAJavaConnection::isConnError()
{
  static int cnt = 0;
  
  DEBUG_MSG("connError check on connection with name " << getMyName());

  // return connError every 5 times (e.g. 100 secs by default settings)
  if (++cnt == 5)
  {
    DEBUG_MSG("connError check fails");
    cnt = 0;
    return true;
  }

  DEBUG_MSG("connError check succeeds");
  return false;   // no error
}

void WCCOAJavaConnection::singleQuery(HWObject *objPtr)
{
  // get value from PLC
}

PVSSboolean WCCOAJavaConnection::writeData(HWObject *objPtr)
{
  // write value to PLC
  return PVSS_TRUE;
}

void WCCOAJavaConnection::workFunc()
{
  // check the incoming connection traffic for spontaneous updates
}

