#include <WCCOAJavaAlertService.hxx>
#include <AlertObject.hxx>
#include <HWObject.hxx>
#include <WCCOAJavaResources.hxx>

//----------------------------------------------------------------------------------
WCCOAJavaAlertService::WCCOAJavaAlertService()
{
}

//----------------------------------------------------------------------------------
WCCOAJavaAlertService::~WCCOAJavaAlertService()
{
}

//---------------------------------------------------------------------------------------------
PVSSboolean WCCOAJavaAlertService::ackAlertInHW(AlertObject *alertPtr, HWObject *objPtr)
{
  if (Resources::isDbgFlag(DrvRsrce::DBG_DRV_ALERT))
  {
      std::cerr << "got alert ack request for HW " << objPtr->getAddress() << std::endl;
      alertPtr->debugPrint();
      std::cerr << "----------------------------------------------" << std::endl;
  }

  alertPtr->setEvent(AlertObject::EA_ACK);
  alertPtr->setComment("simulated ACK from PLC");
  setAlert(alertPtr, objPtr);

  return PVSS_TRUE;
}


void WCCOAJavaAlertService::ackConfirmCB(const AlertObject *alertPtr, const HWObject *adrPtr)
{
  if (Resources::isDbgFlag(DrvRsrce::DBG_DRV_ALERT))
  {
      std::cerr << "got ack confirm CB for HW " << adrPtr->getAddress() << std::endl;
      alertPtr->debugPrint();
      std::cerr << "----------------------------------------------" << std::endl;
  }
}

void WCCOAJavaAlertService::changeNotificationCB(const AlertObject *alertPtr, const HWObject *adrPtr)
{
  if (Resources::isDbgFlag(DrvRsrce::DBG_DRV_ALERT))
  {
      std::cerr << "got change notification CB for HW " << adrPtr->getAddress() << std::endl;
      alertPtr->debugPrint();
      std::cerr << "----------------------------------------------" << std::endl;
  }
}

void WCCOAJavaAlertService::invisibleConfirmCB(const AlertObject *alertPtr, const HWObject *adrPtr)
{
  if (Resources::isDbgFlag(DrvRsrce::DBG_DRV_ALERT))
  {
      std::cerr << "got invisible confirm CB for HW " << adrPtr->getAddress() << std::endl;
      alertPtr->debugPrint();
      std::cerr << "----------------------------------------------" << std::endl;
  }
}
