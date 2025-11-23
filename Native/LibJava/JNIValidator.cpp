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
#include "JNIValidator.hxx"
#include <sstream>

bool JNIValidator::validateJString(JNIEnv *env, jstring jstr, const char *paramName)
{
	if (jstr == NULL) {
		reportValidationError(paramName, "NULL jstring");
		return false;
	}
	return true;
}

bool JNIValidator::validateNonNull(const void *ptr, const char *paramName)
{
	if (ptr == NULL) {
		reportValidationError(paramName, "NULL pointer");
		return false;
	}
	return true;
}

bool JNIValidator::validatePriority(jint prio)
{
	// Valid priorities: 0 (FATAL), 1 (SEVERE), 2 (WARNING), 3 (INFO)
	if (prio < 0 || prio > 3) {
		std::ostringstream oss;
		oss << "Invalid priority value: " << prio << " (valid range: 0-3)";
		reportValidationError("prio", oss.str().c_str());
		return false;
	}
	return true;
}

bool JNIValidator::validateErrorCode(jlong code)
{
	// Error codes should be non-negative
	if (code < 0) {
		std::ostringstream oss;
		oss << "Invalid error code: " << code << " (must be non-negative)";
		reportValidationError("state", oss.str().c_str());
		return false;
	}
	return true;
}

void JNIValidator::reportValidationError(const char *paramName, const char *reason)
{
	std::string message = "JNI validation error in parameter '";
	message += paramName;
	message += "': ";
	message += reason;

	ErrHdl::error(ErrClass::PRIO_WARNING, ErrClass::ERR_PARAM, ErrClass::UNEXPECTEDSTATE,
		message.c_str());
}
