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
#pragma once

#include <jni.h>
#include <ErrClass.hxx>
#include <ErrHdl.hxx>

/**
 * JNI Parameter Validation Helper
 * Provides centralized validation and error reporting for JNI parameters
 */
class JNIValidator {
public:
    /**
     * Validate a jstring parameter
     * @param env JNI environment
     * @param jstr The jstring to validate
     * @param paramName Name of the parameter (for error reporting)
     * @return true if valid, false if NULL
     */
    static bool validateJString(JNIEnv *env, jstring jstr, const char *paramName);

    /**
     * Validate a non-NULL pointer parameter
     * @param ptr The pointer to validate
     * @param paramName Name of the parameter (for error reporting)
     * @return true if not NULL, false if NULL
     */
    static bool validateNonNull(const void *ptr, const char *paramName);

    /**
     * Validate priority parameter
     * @param prio The priority value to validate (0-3)
     * @return true if valid priority (PRIO_FATAL to PRIO_INFO), false otherwise
     */
    static bool validatePriority(jint prio);

    /**
     * Validate error code parameter
     * @param code The error code to validate
     * @return true if valid error code (non-negative), false otherwise
     */
    static bool validateErrorCode(jlong code);

    /**
     * Report validation error to WinCC OA logging
     * @param paramName Name of the invalid parameter
     * @param reason Description of what's wrong
     */
    static void reportValidationError(const char *paramName, const char *reason);
};
