/*
OA4J - WinCC Open Architecture for Java
Copyright (C) 2017 Andreas Vogler

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
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
