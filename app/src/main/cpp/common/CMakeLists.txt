# Check that people use the good file
if(NOT TOP_CMAKE_WAS_SOURCED)
	message(FATAL_ERROR "
	You did not 'cmake' the good CMakeLists.txt file. Use the one in the top dir.
	It is advice to delete all wrongly generated cmake stuff => CMakeFiles & CMakeCache.txt")
endif(NOT TOP_CMAKE_WAS_SOURCED)

add_library(common)

# x86emitter sources
target_sources(common PRIVATE
	AlignedMalloc.cpp
	VirtualMemory.cpp
	EventSource.inl
	SafeArray.inl
	Console.cpp
	EventSource.cpp
	Exceptions.cpp
	FastFormatString.cpp
	FastJmp.cpp
	FileSystem.cpp
	IniInterface.cpp
	Log.cpp
	Mutex.cpp
	Misc.cpp
	MD5Digest.cpp
	PathUtils.cpp
	PrecompiledHeader.cpp
	Perf.cpp
	ProgressCallback.cpp
	pxStreams.cpp
	pxTranslate.cpp
	ReadbackSpinManager.cpp
	RwMutex.cpp
	Semaphore.cpp
	SettingsWrapper.cpp
	StringHelpers.cpp
	StringUtil.cpp
	Timer.cpp
	ThreadTools.cpp
	WindowInfo.cpp
	emitter/bmi.cpp
	emitter/cpudetect.cpp
	emitter/fpu.cpp
	emitter/groups.cpp
	emitter/jmp.cpp
	emitter/legacy.cpp
	emitter/legacy_sse.cpp
	emitter/movs.cpp
	emitter/simd.cpp
	emitter/LnxCpuDetect.cpp
	emitter/WinCpuDetect.cpp
	emitter/x86emitter.cpp
	x86/MemcpyFast.cpp
	Darwin/DarwinThreads.cpp
	Darwin/DarwinMisc.cpp
	Darwin/DarwinSemaphore.cpp
	Linux/LnxHostSys.cpp
	Linux/LnxThreads.cpp
	Linux/LnxMisc.cpp
	Windows/WinThreads.cpp
	Windows/WinHostSys.cpp
	Windows/WinMisc.cpp
	)

# x86emitter headers
target_sources(common PRIVATE
	sse2neon.h
	Align.h
	Assertions.h
	boost_spsc_queue.hpp
	Console.h
	Dependencies.h
	EventSource.h
	Exceptions.h
	FastJmp.h
	FileSystem.h
	General.h
	HashCombine.h
	Log.h
	MemcpyFast.h
	MemsetFast.inl
	MD5Digest.h
	Path.h
	PageFaultSource.h
	PrecompiledHeader.h
	ProgressCallback.h
	pxForwardDefs.h
	pxStreams.h
	RedtapeWindows.h
	ReadbackSpinManager.h
	RwMutex.h
	SafeArray.h
	ScopedAlloc.h
	ScopedGuard.h
	ScopedPtrMT.h
	SettingsInterface.h
	SettingsWrapper.h
	StringHelpers.h
	StringUtil.h
	Timer.h
	Threading.h
	TraceLog.h
	WindowInfo.h
	wxBaseTools.h
	emitter/cpudetect_internal.h
	emitter/implement/dwshift.h
	emitter/implement/group1.h
	emitter/implement/group2.h
	emitter/implement/group3.h
	emitter/implement/helpers.h
	emitter/implement/incdec.h
	emitter/implement/jmpcall.h
	emitter/implement/movs.h
	emitter/implement/simd_arithmetic.h
	emitter/implement/simd_comparisons.h
	emitter/implement/simd_helpers.h
	emitter/implement/simd_moremovs.h
	emitter/implement/simd_shufflepack.h
	emitter/implement/simd_templated_helpers.h
	emitter/implement/test.h
	emitter/implement/xchg.h
	emitter/instructions.h
	emitter/internal.h
	emitter/legacy_instructions.h
	emitter/legacy_internal.h
	emitter/legacy_types.h
	emitter/tools.h
	emitter/x86emitter.h
	emitter/x86types.h
	)

if(USE_OPENGL)
	target_sources(common PRIVATE
		GL/Context.cpp
		GL/Program.cpp
		GL/ShaderCache.cpp
		GL/StreamBuffer.cpp
	)
	target_sources(common PRIVATE
		GL/Context.h
		GL/Program.h
		GL/ShaderCache.h
		GL/StreamBuffer.h
	)
	target_link_libraries(common PUBLIC glad)
endif()

if(USE_VULKAN)
	target_link_libraries(common PUBLIC
		Vulkan-Headers glslang
	)
	target_sources(common PRIVATE
		Vulkan/ShaderCache.cpp
		Vulkan/Texture.cpp
		Vulkan/Loader.cpp
		Vulkan/ShaderCompiler.cpp
		Vulkan/Util.cpp
		Vulkan/SwapChain.cpp
		Vulkan/StreamBuffer.cpp
		Vulkan/Context.cpp
		Vulkan/Builders.cpp
		Vulkan/vk_mem_alloc.cpp
		Vulkan/Context.h
		Vulkan/Texture.h
		Vulkan/ShaderCompiler.h
		Vulkan/SwapChain.h
		Vulkan/Builders.h
		Vulkan/StreamBuffer.h
		Vulkan/ShaderCache.h
		Vulkan/EntryPoints.h
		Vulkan/Loader.h
		Vulkan/Util.h
	)
endif()

if(USE_VTUNE)
	target_link_libraries(common PUBLIC Vtune::Vtune)
endif()

if(WIN32)
	enable_language(ASM_MASM)
	target_sources(common PRIVATE FastJmp.asm)
	target_link_libraries(common PUBLIC pthreads4w Winmm.lib)
	target_sources(common PRIVATE
		FastJmp.asm
		GL/ContextWGL.cpp
		GL/ContextWGL.h
	)
	target_link_libraries(common PUBLIC pthreads4w Winmm.lib opengl32.lib)
elseif(APPLE)
	target_sources(common PRIVATE
		GL/ContextAGL.mm
		GL/ContextAGL.h
	)
	set_source_files_properties(GL/ContextAGL.mm PROPERTIES SKIP_PRECOMPILE_HEADERS ON)
	target_compile_options(common PUBLIC -fobjc-arc)
	target_link_options(common PUBLIC -fobjc-link-runtime)
elseif(ANDROID)
	target_sources(common PRIVATE
			GL/ContextEGL.cpp
			GL/ContextEGL.h
			GL/ContextEGLAndroid.cpp
			GL/ContextEGLAndroid.h
			)

	find_library(log-lib log)
	target_link_libraries(common PRIVATE ${EGL_LIBRARIES} ${log-lib})
else()
	if(X11_API OR WAYLAND_API)
		target_sources(common PRIVATE
			GL/ContextEGL.cpp
			GL/ContextEGL.h
		)
		target_link_libraries(common PRIVATE PkgConfig::EGL)
	endif()

	if(X11_API)
		target_sources(common PRIVATE
			GL/ContextEGLX11.cpp
			GL/ContextEGLX11.h
		)
		target_compile_definitions(common PUBLIC "VULKAN_USE_X11=1")
		if(TARGET PkgConfig::XRANDR)
			target_link_libraries(common PRIVATE PkgConfig::XRANDR)
			target_compile_definitions(common PRIVATE "HAS_XRANDR=1")
		endif()
	endif()

	if(WAYLAND_API)
		target_sources(common PRIVATE
			GL/ContextEGLWayland.cpp
			GL/ContextEGLWayland.h
		)
		target_link_libraries(common PRIVATE ${WAYLAND_EGL_LIBRARIES})
		target_compile_definitions(common PUBLIC "VULKAN_USE_WAYLAND=1")
	endif()
endif()

if (USE_GCC AND CMAKE_INTERPROCEDURAL_OPTIMIZATION)
	# GCC LTO doesn't work with asm statements
	set_source_files_properties(FastJmp.cpp PROPERTIES COMPILE_FLAGS -fno-lto)
endif()

target_link_libraries(common PRIVATE ${LIBC_LIBRARIES} PUBLIC wxWidgets::all fmt::fmt)
target_compile_features(common PUBLIC cxx_std_17)
target_include_directories(common PUBLIC ../3rdparty/include ../)
target_compile_definitions(common PUBLIC "${PCSX2_DEFS}")
target_compile_options(common PRIVATE "${PCSX2_WARNINGS}")

#if(COMMAND target_precompile_headers)
#	target_precompile_headers(common PRIVATE PrecompiledHeader.h)
#endif()
