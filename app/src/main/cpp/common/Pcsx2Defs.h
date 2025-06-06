/*  PCSX2 - PS2 Emulator for PCs
 *  Copyright (C) 2002-2010  PCSX2 Dev Team
 *
 *  PCSX2 is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License as published by the Free Software Found-
 *  ation, either version 3 of the License, or (at your option) any later version.
 *
 *  PCSX2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 *  PURPOSE.  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with PCSX2.
 *  If not, see <http://www.gnu.org/licenses/>.
 */

#pragma once

#ifdef __CYGWIN__
#define __linux__
#endif

// make sure __POSIX__ is defined for all systems where we assume POSIX
// compliance
#if defined(__linux__) || defined(__APPLE__) || defined(__unix__) || defined(__CYGWIN__) || defined(__LINUX__)
#if !defined(__POSIX__)
#define __POSIX__ 1
#endif
#endif

#include "Pcsx2Types.h"

#if defined(_M_X86_32) || defined(_M_X86_64)
#include "common/emitter/x86_intrin.h"
#endif

// The C++ standard doesn't allow `offsetof` to be used on non-constant values (e.g. `offsetof(class, field[i])`)
// Use this in those situations
#define OFFSETOF(a, b) (reinterpret_cast<size_t>(&(static_cast<a*>(0)->b)))

// Renamed ARRAYSIZE to ArraySize -- looks nice and gets rid of Windows.h conflicts (air)
// Notes: I'd have used ARRAY_SIZE instead but ran into cross-platform lib conflicts with
// that as well.  >_<
#ifndef ArraySize
#define ArraySize(x) (sizeof(x) / sizeof((x)[0]))
#endif

// --------------------------------------------------------------------------------------
// Dev / Debug conditionals - Consts for using if() statements instead of uglier #ifdef.
// --------------------------------------------------------------------------------------
// Note: Using if() optimizes nicely in Devel and Release builds, but will generate extra
// code overhead in debug builds (since debug neither inlines, nor optimizes out const-
// level conditionals).  Normally not a concern, but if you stick if( IsDevbuild ) in
// some tight loops it will likely make debug builds unusably slow.
//
#ifdef PCSX2_DEVBUILD
static const bool IsDevBuild = true;
#else
static const bool IsDevBuild = false;
#endif

#ifdef PCSX2_DEBUG
static const bool IsDebugBuild = true;
#else
static const bool IsDebugBuild = false;
#endif

#ifdef PCSX2_DEBUG
#define pxDebugCode(code) code
#else
#define pxDebugCode(code)
#endif

#ifdef PCSX2_DEVBUILD
#define pxDevelCode(code) code
#else
#define pxDevelCode(code)
#endif

#if defined(PCSX2_DEBUG) || defined(PCSX2_DEVBUILD)
#define pxReleaseCode(code)
#define pxNonReleaseCode(code) code
#else
#define pxReleaseCode(code) code
#define pxNonReleaseCode(code)
#endif

// Defines the memory page size for the target platform at compilation.  All supported platforms
// (which means Intel only right now) have a 4k granularity.
#define PCSX2_PAGESIZE 0x1000
static const int __pagesize = PCSX2_PAGESIZE;

// --------------------------------------------------------------------------------------
//  Microsoft Visual Studio
// --------------------------------------------------------------------------------------
#ifdef _MSC_VER

// Using these breaks compat with VC2005; so we're not using it yet.
//#	define __pack_begin		__pragma(pack(1))
//#	define __pack_end		__pragma(pack())

// This is the 2005/earlier compatible packing define, which must be used in conjunction
// with #ifdef _MSC_VER/#pragma pack() directives (ugly).
#define __packed

#define __noinline __declspec(noinline)
#define __noreturn __declspec(noreturn)

// Don't know if there are Visual C++ equivalents of these.
#define likely(x) (!!(x))
#define unlikely(x) (!!(x))

#define CALLBACK __stdcall

#else

// --------------------------------------------------------------------------------------
//  GCC / Intel Compilers Section
// --------------------------------------------------------------------------------------

#ifndef __packed
#define __packed __attribute__((packed))
#endif

#define __assume(cond) ((void)0) // GCC has no equivalent for __assume
#define CALLBACK __attribute__((stdcall))

// Inlining note: GCC needs ((unused)) attributes defined on inlined functions to suppress
// warnings when a static inlined function isn't used in the scope of a single file (which
// happens *by design* like all the friggen time >_<)

#ifndef __fastcall
#define __fastcall __attribute__((fastcall))
#endif
#define __vectorcall __fastcall
#define _inline __inline__ __attribute__((unused))
#ifdef NDEBUG
#define __forceinline __attribute__((always_inline, unused))
#else
#define __forceinline __attribute__((unused))
#endif
#ifndef __noinline
#define __noinline __attribute__((noinline))
#endif
#ifndef __noreturn
#define __noreturn __attribute__((noreturn))
#endif
#define likely(x) __builtin_expect(!!(x), 1)
#define unlikely(x) __builtin_expect(!!(x), 0)
#endif

// --------------------------------------------------------------------------------------
// __releaseinline / __ri -- a forceinline macro that is enabled for RELEASE/PUBLIC builds ONLY.
// --------------------------------------------------------------------------------------
// This is useful because forceinline can make certain types of debugging problematic since
// functions that look like they should be called won't breakpoint since their code is
// inlined, and it can make stack traces confusing or near useless.
//
// Use __releaseinline for things which are generally large functions where trace debugging
// from Devel builds is likely useful; but which should be inlined in an optimized Release
// environment.
//
#ifdef PCSX2_DEVBUILD
#define __releaseinline
#else
#define __releaseinline __forceinline
#endif

#define __ri __releaseinline
#define __fi __forceinline
#define __fc __fastcall

typedef unsigned char uint8;
typedef signed char int8;
typedef unsigned short uint16;
typedef signed short int16;
typedef unsigned int uint32;
typedef signed int int32;
typedef unsigned long long uint64;
typedef signed long long int64;
typedef signed long long sint64;

// Makes sure that if anyone includes xbyak, it doesn't do anything bad
#define XBYAK_ENABLE_OMITTED_OPERAND

#ifdef __x86_64__
#define _M_AMD64
#endif

#ifndef RESTRICT
#ifdef __INTEL_COMPILER
#define RESTRICT restrict
#elif defined(_MSC_VER)
#define RESTRICT __restrict
#elif defined(__GNUC__)
#define RESTRICT __restrict__
#else
#define RESTRICT
#endif
#endif

#ifndef __has_attribute
#define __has_attribute(x) 0
#endif

#ifndef __has_builtin
#define __has_builtin(x) 0
#endif

#ifdef __cpp_constinit
#define CONSTINIT constinit
#elif __has_attribute(require_constant_initialization)
#define CONSTINIT __attribute__((require_constant_initialization))
#else
#define CONSTINIT
#endif

#define ASSERT assert
