#ifndef _DEMODRVINTDP_H_
#define _DEMODRVINTDP_H_

#include <DrvIntDp.hxx>

class WCCOAJavaDrvIntDp : public DrvIntDp
{
  public:

   // enum
   // {
   //   inConfig,            // Configuration String 
   //   maxConnect,          // No. of DPE we will connect to
	  //outStatus,           // Status Datapoint
   //   maxDP=outStatus      // Total no. of DPE
   // };

	enum
	{
		inConfig,            // Configuration String 
		maxConnect,          // No. of DPE we will connect to
		maxDP = maxConnect     // Total no. of DPE
	};


	WCCOAJavaDrvIntDp();

	~WCCOAJavaDrvIntDp();

    /** retrievs the names of the internal DPEs. The function must be overloaded.
        It should return the proper DPE string for the corresponding index */
    virtual const CharString& getDpName4Query(int index);

    /** called if there is an answer to a certain index */
    virtual void answer4DpId(int index, Variable* varPtr);

    /** called if there is a hotlink to a certain index */
    virtual void hotLink2Internal(int index, Variable* varPtr);

  private:

};

#endif
