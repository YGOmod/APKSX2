add_library(soundtouch
	source/SoundStretch/WavFile.cpp
	source/SoundTouch/AAFilter.cpp
	source/SoundTouch/BPMDetect.cpp
	source/SoundTouch/cpu_detect_x86.cpp
	source/SoundTouch/FIFOSampleBuffer.cpp
	source/SoundTouch/FIRFilter.cpp
	source/SoundTouch/InterpolateCubic.cpp
	source/SoundTouch/InterpolateLinear.cpp
	source/SoundTouch/InterpolateShannon.cpp
	source/SoundTouch/mmx_optimized.cpp
	source/SoundTouch/PeakFinder.cpp
	source/SoundTouch/RateTransposer.cpp
	source/SoundTouch/SoundTouch.cpp
	source/SoundTouch/sse_optimized.cpp
	source/SoundTouch/TDStretch.cpp
	soundtouch/BPMDetect.h
	soundtouch/FIFOSampleBuffer.h
	soundtouch/FIFOSamplePipe.h
	soundtouch/SoundTouch.h
	soundtouch/STTypes.h
	source/SoundStretch/WavFile.h
	source/SoundTouch/AAFilter.h
	source/SoundTouch/cpu_detect.h
	source/SoundTouch/FIRFilter.h
	source/SoundTouch/InterpolateCubic.h
	source/SoundTouch/InterpolateLinear.h
	source/SoundTouch/InterpolateShannon.h
	source/SoundTouch/PeakFinder.h
	source/SoundTouch/RateTransposer.h
	source/SoundTouch/TDStretch.h
)

target_include_directories(soundtouch PUBLIC soundtouch)
add_library(PkgConfig::SOUNDTOUCH ALIAS soundtouch)
