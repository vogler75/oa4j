#include <WCCOAJavaDrvIntDp.hxx>
#include <WCCOAJavaResources.hxx>
#include <WCCOAJavaHWService.hxx>
#include <WCCOAJavaDrv.hxx>

WCCOAJavaDrvIntDp::WCCOAJavaDrvIntDp()
	: DrvIntDp(maxDP, maxConnect)
{
}

WCCOAJavaDrvIntDp::~WCCOAJavaDrvIntDp()
{
}

const CharString& WCCOAJavaDrvIntDp::getDpName4Query(int index)
{
	static CharString str("");

	switch (index)
	{
	case inConfig: str = WCCOAJavaResources::drvDpName + CharString(Resources::getManNum()) + ".Config:_original.._value";          break;
	//case outStatus: str = WCCOAJavaResources::drvDpName + CharString(Resources::getManNum()) + ".Status:_original.._value";          break;
	default:
		;
	}
	return str;
}

void WCCOAJavaDrvIntDp::answer4DpId(int index, Variable* varPtr)
{
	//std::cout << "answer4DpId " << index << ": " << varPtr->formatValue() << std::endl;
	if (index == inConfig)
	{
		//std::cout << "config: " << varPtr->formatValue() << std::endl;
		WCCOAJavaDrv::thisManager->answer4DpId(index, varPtr);
	}
	delete varPtr;
}

void WCCOAJavaDrvIntDp::hotLink2Internal(int index, Variable* varPtr)
{
	//std::cout << "hotLink2Internal " << index << ": " << varPtr->formatValue() << std::endl;
	switch (index)
	{
	case inConfig:
		//std::cout << "config: " << varPtr->formatValue() << std::endl;
		WCCOAJavaDrv::thisManager->hotLink2Internal(index, varPtr);
		break;

	default:
		// Cannot happen, just make some compilers happy.
		// Therefore don't even print an error message :)
		break;
	}
	delete varPtr;
}
