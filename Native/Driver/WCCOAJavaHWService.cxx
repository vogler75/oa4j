#include <WCCOAJavaHWService.hxx>
#include <WCCOAJavaDrv.hxx>

#include <DrvManager.hxx>
#include <PVSSMacros.hxx>     // DEBUG macros

//--------------------------------------------------------------------------------
// called after connect to data

PVSSboolean WCCOAJavaHWService::initialize(int argc, char *argv[])
{
	// use this function to initialize internals
	// if you don't need it, you can safely remove the whole method	
	// To stop driver return PVSS_FALSE
	return WCCOAJavaDrv::thisManager->initialize(argc, argv);
}

//--------------------------------------------------------------------------------
// called after connect to event

PVSSboolean WCCOAJavaHWService::start()
{
	// use this function to start your hardware activity.
	// return PVSS_FALSE on failure
	return WCCOAJavaDrv::thisManager->start();
}

//--------------------------------------------------------------------------------

void WCCOAJavaHWService::stop()
{
	// use this function to stop your hardware activity.
	WCCOAJavaDrv::thisManager->stop();
}

//--------------------------------------------------------------------------------

void WCCOAJavaHWService::workProc()
{
	HWObject obj;
	while (WCCOAJavaDrv::thisManager->workProc(&obj))
	{
		//PVSSchar *data = obj->cutData();
		//delete data;
		//delete obj;
		//return;
		DEBUG_DRV_USR1("received message");

		// a chance to see what's happening
		if (Resources::isDbgFlag(Resources::DBG_DRV_USR1))
			obj.debugPrint();

		// find the HWObject via the periphery address in the HWObject list,
		// e.g. for the low level old-new comparison
		HWObject *addrObj = DrvManager::getHWMapperPtr()->findHWObject(&obj);

		// ok, we found it; now send to the DPEs
		if (addrObj) {
			if (obj.getDlen() > 0 && obj.getDataPtr() != NULL) {
				DrvManager::getSelfPtr()->toDp(&obj, addrObj);
				//std::cout << "toDp done! " << obj.getAddress().c_str() << std::endl;
			}
			else {
				std::cout << "HWServer::workProc() hw object has no data! " << obj.getAddress().c_str() << std::endl;
			}
		}
		else {
			//std::cout << "HWServer::workProc() hw address not found! " << obj.getAddress().c_str() << std::endl;
		}
	}
}

//--------------------------------------------------------------------------------
// we get data from PVSS and shall send it to the periphery

PVSSboolean WCCOAJavaHWService::writeData(HWObject *objPtr)
{
	if (Resources::isDbgFlag(Resources::DBG_DRV_USR1))
		objPtr->debugPrint();

	//// TODO somehow send the data to your device
	//std::cout << "writeData: info: " << objPtr->getInfo() 
	//	<< " orgTime: " << objPtr->getOrgTime()
	//	<< " statusSize: " << (objPtr->getStatus()).getSize()
	//	<< " subIndex: " << objPtr->getSubindex()
	//	<< " objSrcType: " << objPtr->getObjSrcType()
	//	<< std::endl;

	//// sending was successful
	//return PVSS_TRUE;
	return WCCOAJavaDrv::thisManager->writeData(objPtr);
}

//--------------------------------------------------------------------------------

void WCCOAJavaHWService::flushHW()
{
	//std::cout << "flushHW" << std::endl;
	WCCOAJavaDrv::thisManager->flushHW();
}

//----------------------------------------------------------------------------
// funktion is called after driver changes between aktive and passive state
// driver calls notify function for all devices
// in:   dc  ... true = Connection closing vor all devices
//              false= Connection opening vor all devices
//----------------------------------------------------------------------------
void WCCOAJavaHWService::notifyDisableCommands(PVSSboolean dc)
{
	//std::cout << "notifyDisableCommands(" << dc << ")" << std::endl;
	WCCOAJavaDrv::thisManager->notifyDisableCommands(dc);
}
