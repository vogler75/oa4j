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
#include <WCCOAJavaHotLinkWaitForAnswer.hxx>
#include <Java.hxx>

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
