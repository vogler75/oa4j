// WCCOAJavaConnection.hxx -- WCCOAJava HmiConnection-Class


#ifndef _WCCOAJavaCONNECTION_H_
#define _WCCOAJavaCONNECTION_H_

#include <HmiConnection.hxx>

class WCCOAJavaConnection : public HmiConnection
{
  public:
     
    WCCOAJavaConnection(DpIdType id);

    ~WCCOAJavaConnection();

    /** the open connection callback.
      * possible state changes are HMI_CS_Failure and HMI_CS_Connected
      */
    virtual void open();

    /** the close connection callback.
      * there are no active state changes possible here
      */
    virtual void close();

    /** the open server side callback.
      * possible state changes are HMI_CS_Failure and HMI_CS_Listening
      */
    virtual void openServer();

    /** the close server side callback.
      * there are no active state changes possible here
      */
    virtual void closeServer();

    /** the general query callback.
      * this callback is triggered whenever a general query should be done.
      * the default implementation issues singleQuery calls for all input and IO elements.
      */
    virtual void doGeneralQuery();

    /** the internal general query callback.
      * this callback is triggered whenever an internal general query should be done.
      * the default implementation issues write requests calls for all output and IO elements.
      */
    virtual void doInternalGeneralQuery();

    /** the alert sync callback.
      * this callback is triggered whenever a general alert sync should be done.
      */
    virtual void doAlertSync();

    /** the connection error check callback.
      * this callback is triggered periodically to check for connection faults.
      * returning true will cause the connection to be closed.
      */
    virtual bool isConnError();

    /** This function is called for a single data request or when polling data.
      * The HWObject contains the PeriphAddress string. Find the corresponding hardware address, ask
      * for the data. The objPtr must not be deleted.
      * @param objPtr the HWObject to fill with data
      * @classification public use, overload
      */
    virtual void singleQuery(HWObject *objPtr);

    /** This function is called for each fully converted HWObject. The data inside it has
      * been processed according to the choosen transformation. All you have to do is to determine the
      * corresponding hardware address and to transmit the data packet. The objPtr must not be deleted.
      * @param objPtr pointer to HW object
      * @classification public use, overload
      */
    virtual PVSSboolean writeData(HWObject *objPtr);

    /** Working procedure. This function is called repeatedly by the DrvManager mainLoop() to check the
      * hardware for any incoming data. when data is ready for accepting you should pack the data into a HWObject and
      * use the DrvManager toDp() function to transmit the data to the pvss2 system.
      * @warning beware of staying inside this function too long because no messages can go in and
      * out of the driver as long as you keep control!
      * @classification public use, overload
      */ 
    virtual void workFunc();

};

#endif
