package kr.co.iefriends.pcsx2;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SDLControllerManager
{
    public static native int nativeSetupJNI();

    public static native int nativeAddJoystick(int device_id, String name, String desc,
                                               int vendor_id, int product_id,
                                               boolean is_accelerometer, int button_mask,
                                               int naxes, int axis_mask, int nhats, int nballs);
    public static native int nativeRemoveJoystick(int device_id);
    public static native int nativeAddHaptic(int device_id, String name);
    public static native int nativeRemoveHaptic(int device_id);
    public static native int onNativePadDown(int device_id, int keycode);
    public static native int onNativePadUp(int device_id, int keycode);
    public static native void onNativeJoy(int device_id, int axis,
                                          float value);
    public static native void onNativeHat(int device_id, int hat_id,
                                          int x, int y);

    protected static SDLJoystickHandler mJoystickHandler;
    protected static SDLHapticHandler mHapticHandler;

    private static final String TAG = "SDLControllerManager";

    public static void initialize() {
        if (mJoystickHandler == null) {
            if (Build.VERSION.SDK_INT >= 19 /* Android 4.4 (KITKAT) */) {
                mJoystickHandler = new SDLJoystickHandler_API19();
            } else {
                mJoystickHandler = new SDLJoystickHandler_API16();
            }
        }

        if (mHapticHandler == null) {
            if (Build.VERSION.SDK_INT >= 26 /* Android 8.0 (O) */) {
                mHapticHandler = new SDLHapticHandler_API26();
            } else {
                mHapticHandler = new SDLHapticHandler();
            }
        }
    }

    // Joystick glue code, just a series of stubs that redirect to the SDLJoystickHandler instance
    public static boolean handleJoystickMotionEvent(MotionEvent event) {
        return mJoystickHandler.handleMotionEvent(event);
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void pollInputDevices() {
        mJoystickHandler.pollInputDevices();
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void pollHapticDevices() {
        mHapticHandler.pollHapticDevices();
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void hapticRun(int device_id, float intensity, int length) {
        mHapticHandler.run(device_id, intensity, length);
    }

    /**
     * This method is called by SDL using JNI.
     */
    public static void hapticStop(int device_id)
    {
        mHapticHandler.stop(device_id);
    }

    // Check if a given device is considered a possible SDL joystick
    public static boolean isDeviceSDLJoystick(int deviceId) {
        InputDevice device = InputDevice.getDevice(deviceId);
        // We cannot use InputDevice.isVirtual before API 16, so let's accept
        // only nonnegative device ids (VIRTUAL_KEYBOARD equals -1)
        if ((device == null) || (deviceId < 0)) {
            return false;
        }
        int sources = device.getSources();

        /* This is called for every button press, so let's not spam the logs */
        /*
        if ((sources & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
            Log.v(TAG, "Input device " + device.getName() + " has class joystick.");
        }
        if ((sources & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) {
            Log.v(TAG, "Input device " + device.getName() + " is a dpad.");
        }
        if ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            Log.v(TAG, "Input device " + device.getName() + " is a gamepad.");
        }
        */

        return ((sources & InputDevice.SOURCE_CLASS_JOYSTICK) != 0 ||
                ((sources & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD) ||
                ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
        );
    }

}

class SDLJoystickHandler {

    /**
     * Handles given MotionEvent.
     * @param event the event to be handled.
     * @return if given event was processed.
     */
    public boolean handleMotionEvent(MotionEvent event) {
        return false;
    }

    /**
     * Handles adding and removing of input devices.
     */
    public void pollInputDevices() {
    }
}

/* Actual joystick functionality available for API >= 12 devices */
class SDLJoystickHandler_API16 extends SDLJoystickHandler {

    static class SDLJoystick {
        public int device_id;
        public String name;
        public String desc;
        public ArrayList<InputDevice.MotionRange> axes;
        public ArrayList<InputDevice.MotionRange> hats;
    }
    static class RangeComparator implements Comparator<InputDevice.MotionRange> {
        @Override
        public int compare(InputDevice.MotionRange arg0, InputDevice.MotionRange arg1) {
            // Some controllers, like the Moga Pro 2, return AXIS_GAS (22) for right trigger and AXIS_BRAKE (23) for left trigger - swap them so they're sorted in the right order for SDL
            int arg0Axis = arg0.getAxis();
            int arg1Axis = arg1.getAxis();
            if (arg0Axis == MotionEvent.AXIS_GAS) {
                arg0Axis = MotionEvent.AXIS_BRAKE;
            } else if (arg0Axis == MotionEvent.AXIS_BRAKE) {
                arg0Axis = MotionEvent.AXIS_GAS;
            }
            if (arg1Axis == MotionEvent.AXIS_GAS) {
                arg1Axis = MotionEvent.AXIS_BRAKE;
            } else if (arg1Axis == MotionEvent.AXIS_BRAKE) {
                arg1Axis = MotionEvent.AXIS_GAS;
            }

            // Make sure the AXIS_Z is sorted between AXIS_RY and AXIS_RZ.
            // This is because the usual pairing are:
            // - AXIS_X + AXIS_Y (left stick).
            // - AXIS_RX, AXIS_RY (sometimes the right stick, sometimes triggers).
            // - AXIS_Z, AXIS_RZ (sometimes the right stick, sometimes triggers).
            // This sorts the axes in the above order, which tends to be correct
            // for Xbox-ish game pads that have the right stick on RX/RY and the
            // triggers on Z/RZ.
            //
            // Gamepads that don't have AXIS_Z/AXIS_RZ but use
            // AXIS_LTRIGGER/AXIS_RTRIGGER are unaffected by this.
            //
            // References:
            // - https://developer.android.com/develop/ui/views/touch-and-input/game-controllers/controller-input
            // - https://www.kernel.org/doc/html/latest/input/gamepad.html
            if (arg0Axis == MotionEvent.AXIS_Z) {
                arg0Axis = MotionEvent.AXIS_RZ - 1;
            } else if (arg0Axis > MotionEvent.AXIS_Z && arg0Axis < MotionEvent.AXIS_RZ) {
                --arg0Axis;
            }
            if (arg1Axis == MotionEvent.AXIS_Z) {
                arg1Axis = MotionEvent.AXIS_RZ - 1;
            } else if (arg1Axis > MotionEvent.AXIS_Z && arg1Axis < MotionEvent.AXIS_RZ) {
                --arg1Axis;
            }

            return arg0Axis - arg1Axis;
        }
    }

    private final ArrayList<SDLJoystick> mJoysticks;

    public SDLJoystickHandler_API16() {

        mJoysticks = new ArrayList<SDLJoystick>();
    }

    @Override
    public void pollInputDevices() {
        int[] deviceIds = InputDevice.getDeviceIds();

        for (int device_id : deviceIds) {
            if (SDLControllerManager.isDeviceSDLJoystick(device_id)) {
                SDLJoystick joystick = getJoystick(device_id);
                if (joystick == null) {
                    InputDevice joystickDevice = InputDevice.getDevice(device_id);
                    joystick = new SDLJoystick();
                    joystick.device_id = device_id;
                    joystick.name = joystickDevice.getName();
                    joystick.desc = getJoystickDescriptor(joystickDevice);
                    joystick.axes = new ArrayList<InputDevice.MotionRange>();
                    joystick.hats = new ArrayList<InputDevice.MotionRange>();

                    List<InputDevice.MotionRange> ranges = joystickDevice.getMotionRanges();
                    Collections.sort(ranges, new RangeComparator());
                    for (InputDevice.MotionRange range : ranges) {
                        if ((range.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
                            if (range.getAxis() == MotionEvent.AXIS_HAT_X || range.getAxis() == MotionEvent.AXIS_HAT_Y) {
                                joystick.hats.add(range);
                            } else {
                                joystick.axes.add(range);
                            }
                        }
                    }

                    mJoysticks.add(joystick);
                    SDLControllerManager.nativeAddJoystick(joystick.device_id, joystick.name, joystick.desc,
                            getVendorId(joystickDevice), getProductId(joystickDevice), false,
                            getButtonMask(joystickDevice), joystick.axes.size(), getAxisMask(joystick.axes), joystick.hats.size()/2, 0);
                }
            }
        }

        /* Check removed devices */
        ArrayList<Integer> removedDevices = null;
        for (SDLJoystick joystick : mJoysticks) {
            int device_id = joystick.device_id;
            int i;
            for (i = 0; i < deviceIds.length; i++) {
                if (device_id == deviceIds[i]) break;
            }
            if (i == deviceIds.length) {
                if (removedDevices == null) {
                    removedDevices = new ArrayList<Integer>();
                }
                removedDevices.add(device_id);
            }
        }

        if (removedDevices != null) {
            for (int device_id : removedDevices) {
                SDLControllerManager.nativeRemoveJoystick(device_id);
                for (int i = 0; i < mJoysticks.size(); i++) {
                    if (mJoysticks.get(i).device_id == device_id) {
                        mJoysticks.remove(i);
                        break;
                    }
                }
            }
        }
    }

    protected SDLJoystick getJoystick(int device_id) {
        for (SDLJoystick joystick : mJoysticks) {
            if (joystick.device_id == device_id) {
                return joystick;
            }
        }
        return null;
    }

    @Override
    public boolean handleMotionEvent(MotionEvent event) {
        int actionPointerIndex = event.getActionIndex();
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_MOVE) {
            SDLJoystick joystick = getJoystick(event.getDeviceId());
            if (joystick != null) {
                for (int i = 0; i < joystick.axes.size(); i++) {
                    InputDevice.MotionRange range = joystick.axes.get(i);
                    /* Normalize the value to -1...1 */
                    float value = (event.getAxisValue(range.getAxis(), actionPointerIndex) - range.getMin()) / range.getRange() * 2.0f - 1.0f;
                    SDLControllerManager.onNativeJoy(joystick.device_id, i, value);
                }
                for (int i = 0; i < joystick.hats.size() / 2; i++) {
                    int hatX = Math.round(event.getAxisValue(joystick.hats.get(2 * i).getAxis(), actionPointerIndex));
                    int hatY = Math.round(event.getAxisValue(joystick.hats.get(2 * i + 1).getAxis(), actionPointerIndex));
                    SDLControllerManager.onNativeHat(joystick.device_id, i, hatX, hatY);
                }
            }
        }
        return true;
    }

    public String getJoystickDescriptor(InputDevice joystickDevice) {
        String desc = joystickDevice.getDescriptor();

        if (desc != null && !desc.isEmpty()) {
            return desc;
        }

        return joystickDevice.getName();
    }
    public int getProductId(InputDevice joystickDevice) {
        return 0;
    }
    public int getVendorId(InputDevice joystickDevice) {
        return 0;
    }
    public int getAxisMask(List<InputDevice.MotionRange> ranges) {
        return -1;
    }
    public int getButtonMask(InputDevice joystickDevice) {
        return -1;
    }
}

class SDLJoystickHandler_API19 extends SDLJoystickHandler_API16 {

    @Override
    public int getProductId(InputDevice joystickDevice) {
        return joystickDevice.getProductId();
    }

    @Override
    public int getVendorId(InputDevice joystickDevice) {
        return joystickDevice.getVendorId();
    }

    @Override
    public int getAxisMask(List<InputDevice.MotionRange> ranges) {
        // For compatibility, keep computing the axis mask like before,
        // only really distinguishing 2, 4 and 6 axes.
        int axis_mask = 0;
        if (ranges.size() >= 2) {
            // ((1 << SDL_GAMEPAD_AXIS_LEFTX) | (1 << SDL_GAMEPAD_AXIS_LEFTY))
            axis_mask |= 0x0003;
        }
        if (ranges.size() >= 4) {
            // ((1 << SDL_GAMEPAD_AXIS_RIGHTX) | (1 << SDL_GAMEPAD_AXIS_RIGHTY))
            axis_mask |= 0x000c;
        }
        if (ranges.size() >= 6) {
            // ((1 << SDL_GAMEPAD_AXIS_LEFT_TRIGGER) | (1 << SDL_GAMEPAD_AXIS_RIGHT_TRIGGER))
            axis_mask |= 0x0030;
        }
        // Also add an indicator bit for whether the sorting order has changed.
        // This serves to disable outdated gamecontrollerdb.txt mappings.
        boolean have_z = false;
        boolean have_past_z_before_rz = false;
        for (InputDevice.MotionRange range : ranges) {
            int axis = range.getAxis();
            if (axis == MotionEvent.AXIS_Z) {
                have_z = true;
            } else if (axis > MotionEvent.AXIS_Z && axis < MotionEvent.AXIS_RZ) {
                have_past_z_before_rz = true;
            }
        }
        if (have_z && have_past_z_before_rz) {
            // If both these exist, the compare() function changed sorting order.
            // Set a bit to indicate this fact.
            axis_mask |= 0x8000;
        }
        return axis_mask;
    }

    @Override
    public int getButtonMask(InputDevice joystickDevice) {
        int button_mask = 0;
        int[] keys = new int[] {
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_BUTTON_Y,
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_BUTTON_MODE,
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_BUTTON_THUMBL,
            KeyEvent.KEYCODE_BUTTON_THUMBR,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_BUTTON_SELECT,
            KeyEvent.KEYCODE_DPAD_CENTER,

            // These don't map into any SDL controller buttons directly
            KeyEvent.KEYCODE_BUTTON_L2,
            KeyEvent.KEYCODE_BUTTON_R2,
            KeyEvent.KEYCODE_BUTTON_C,
            KeyEvent.KEYCODE_BUTTON_Z,
            KeyEvent.KEYCODE_BUTTON_1,
            KeyEvent.KEYCODE_BUTTON_2,
            KeyEvent.KEYCODE_BUTTON_3,
            KeyEvent.KEYCODE_BUTTON_4,
            KeyEvent.KEYCODE_BUTTON_5,
            KeyEvent.KEYCODE_BUTTON_6,
            KeyEvent.KEYCODE_BUTTON_7,
            KeyEvent.KEYCODE_BUTTON_8,
            KeyEvent.KEYCODE_BUTTON_9,
            KeyEvent.KEYCODE_BUTTON_10,
            KeyEvent.KEYCODE_BUTTON_11,
            KeyEvent.KEYCODE_BUTTON_12,
            KeyEvent.KEYCODE_BUTTON_13,
            KeyEvent.KEYCODE_BUTTON_14,
            KeyEvent.KEYCODE_BUTTON_15,
            KeyEvent.KEYCODE_BUTTON_16,
        };
        int[] masks = new int[] {
            (1 << 0),   // A -> A
            (1 << 1),   // B -> B
            (1 << 2),   // X -> X
            (1 << 3),   // Y -> Y
            (1 << 4),   // BACK -> BACK
            (1 << 6),   // MENU -> START
            (1 << 5),   // MODE -> GUIDE
            (1 << 6),   // START -> START
            (1 << 7),   // THUMBL -> LEFTSTICK
            (1 << 8),   // THUMBR -> RIGHTSTICK
            (1 << 9),   // L1 -> LEFTSHOULDER
            (1 << 10),  // R1 -> RIGHTSHOULDER
            (1 << 11),  // DPAD_UP -> DPAD_UP
            (1 << 12),  // DPAD_DOWN -> DPAD_DOWN
            (1 << 13),  // DPAD_LEFT -> DPAD_LEFT
            (1 << 14),  // DPAD_RIGHT -> DPAD_RIGHT
            (1 << 4),   // SELECT -> BACK
            (1 << 0),   // DPAD_CENTER -> A
            (1 << 15),  // L2 -> ??
            (1 << 16),  // R2 -> ??
            (1 << 17),  // C -> ??
            (1 << 18),  // Z -> ??
            (1 << 20),  // 1 -> ??
            (1 << 21),  // 2 -> ??
            (1 << 22),  // 3 -> ??
            (1 << 23),  // 4 -> ??
            (1 << 24),  // 5 -> ??
            (1 << 25),  // 6 -> ??
            (1 << 26),  // 7 -> ??
            (1 << 27),  // 8 -> ??
            (1 << 28),  // 9 -> ??
            (1 << 29),  // 10 -> ??
            (1 << 30),  // 11 -> ??
            (1 << 31),  // 12 -> ??
            // We're out of room...
            0xFFFFFFFF,  // 13 -> ??
            0xFFFFFFFF,  // 14 -> ??
            0xFFFFFFFF,  // 15 -> ??
            0xFFFFFFFF,  // 16 -> ??
        };
        boolean[] has_keys = joystickDevice.hasKeys(keys);
        for (int i = 0; i < keys.length; ++i) {
            if (has_keys[i]) {
                button_mask |= masks[i];
            }
        }
        return button_mask;
    }
}

class SDLHapticHandler_API26 extends SDLHapticHandler {
    @Override
    public void run(int device_id, float intensity, int length) {
        SDLHaptic haptic = getHaptic(device_id);
        if (haptic != null) {
            Log.d("SDL", "Rtest: Vibe with intensity " + intensity + " for " + length);
            if (intensity == 0.0f) {
                stop(device_id);
                return;
            }

            int vibeValue = Math.round(intensity * 255);

            if (vibeValue > 255) {
                vibeValue = 255;
            }
            if (vibeValue < 1) {
                stop(device_id);
                return;
            }
            try {
                haptic.vib.vibrate(VibrationEffect.createOneShot(length, vibeValue));
            }
            catch (Exception e) {
                // Fall back to the generic method, which uses DEFAULT_AMPLITUDE, but works even if
                // something went horribly wrong with the Android 8.0 APIs.
                haptic.vib.vibrate(length);
            }
        }
    }
}

class SDLHapticHandler {

    static class SDLHaptic {
        public int device_id;
        public String name;
        public Vibrator vib;
    }

    private final ArrayList<SDLHaptic> mHaptics;

    public SDLHapticHandler() {
        mHaptics = new ArrayList<SDLHaptic>();
    }

    public void run(int device_id, float intensity, int length) {
        SDLHaptic haptic = getHaptic(device_id);
        if (haptic != null) {
            haptic.vib.vibrate(length);
        }
    }

    public void stop(int device_id) {
        SDLHaptic haptic = getHaptic(device_id);
        if (haptic != null) {
            haptic.vib.cancel();
        }
    }

    public void pollHapticDevices() {

        final int deviceId_VIBRATOR_SERVICE = 999999;
        boolean hasVibratorService = false;

        int[] deviceIds = InputDevice.getDeviceIds();
        // It helps processing the device ids in reverse order
        // For example, in the case of the XBox 360 wireless dongle,
        // so the first controller seen by SDL matches what the receiver
        // considers to be the first controller

        for (int i = deviceIds.length - 1; i > -1; i--) {
            SDLHaptic haptic = getHaptic(deviceIds[i]);
            if (haptic == null) {
                InputDevice device = InputDevice.getDevice(deviceIds[i]);
                Vibrator vib = device.getVibrator();
                if (vib != null && vib.hasVibrator()) {
                    haptic = new SDLHaptic();
                    haptic.device_id = deviceIds[i];
                    haptic.name = device.getName();
                    haptic.vib = vib;
                    mHaptics.add(haptic);
                    SDLControllerManager.nativeAddHaptic(haptic.device_id, haptic.name);
                }
            }
        }

        /* Check VIBRATOR_SERVICE */
        Vibrator vib = (Vibrator) NativeApp.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vib != null) {
            hasVibratorService = vib.hasVibrator();

            if (hasVibratorService) {
                SDLHaptic haptic = getHaptic(deviceId_VIBRATOR_SERVICE);
                if (haptic == null) {
                    haptic = new SDLHaptic();
                    haptic.device_id = deviceId_VIBRATOR_SERVICE;
                    haptic.name = "VIBRATOR_SERVICE";
                    haptic.vib = vib;
                    mHaptics.add(haptic);
                    SDLControllerManager.nativeAddHaptic(haptic.device_id, haptic.name);
                }
            }
        }

        /* Check removed devices */
        ArrayList<Integer> removedDevices = null;
        for (SDLHaptic haptic : mHaptics) {
            int device_id = haptic.device_id;
            int i;
            for (i = 0; i < deviceIds.length; i++) {
                if (device_id == deviceIds[i]) break;
            }

            if ((device_id != deviceId_VIBRATOR_SERVICE || !hasVibratorService) && i == deviceIds.length) {
                if (removedDevices == null) {
                    removedDevices = new ArrayList<Integer>();
                }
                removedDevices.add(device_id);
            }  // else: don't remove the vibrator if it is still present
        }

        if (removedDevices != null) {
            for (int device_id : removedDevices) {
                SDLControllerManager.nativeRemoveHaptic(device_id);
                for (int i = 0; i < mHaptics.size(); i++) {
                    if (mHaptics.get(i).device_id == device_id) {
                        mHaptics.remove(i);
                        break;
                    }
                }
            }
        }
    }

    protected SDLHaptic getHaptic(int device_id) {
        for (SDLHaptic haptic : mHaptics) {
            if (haptic.device_id == device_id) {
                return haptic;
            }
        }
        return null;
    }
}

class SDLGenericMotionListener_API12 implements View.OnGenericMotionListener {
    // Generic Motion (mouse hover, joystick...) events go here
    @Override
    public boolean onGenericMotion(View v, MotionEvent event) {
        if (event.getSource() == InputDevice.SOURCE_JOYSTICK) {
            return SDLControllerManager.handleJoystickMotionEvent(event);
        }

        // Event was not managed
        return false;
    }

    public boolean supportsRelativeMouse() {
        return false;
    }

    public boolean inRelativeMode() {
        return false;
    }

    public boolean setRelativeMouseEnabled(boolean enabled) {
        return false;
    }

    public void reclaimRelativeMouseModeIfNeeded()
    {

    }

    public float getEventX(MotionEvent event) {
        return event.getX(0);
    }

    public float getEventY(MotionEvent event) {
        return event.getY(0);
    }

}

class SDLGenericMotionListener_API24 extends SDLGenericMotionListener_API12 {
    // Generic Motion (mouse hover, joystick...) events go here

    private boolean mRelativeModeEnabled;

    @Override
    public boolean onGenericMotion(View v, MotionEvent event) {
        // Event was not managed, call SDLGenericMotionListener_API12 method
        return super.onGenericMotion(v, event);
    }

    @Override
    public boolean supportsRelativeMouse() {
        return true;
    }

    @Override
    public boolean inRelativeMode() {
        return mRelativeModeEnabled;
    }

    @Override
    public boolean setRelativeMouseEnabled(boolean enabled) {
        mRelativeModeEnabled = enabled;
        return true;
    }

    @Override
    public float getEventX(MotionEvent event) {
        if (mRelativeModeEnabled) {
            return event.getAxisValue(MotionEvent.AXIS_RELATIVE_X);
        } else {
            return event.getX(0);
        }
    }

    @Override
    public float getEventY(MotionEvent event) {
        if (mRelativeModeEnabled) {
            return event.getAxisValue(MotionEvent.AXIS_RELATIVE_Y);
        } else {
            return event.getY(0);
        }
    }
}

class SDLGenericMotionListener_API26 extends SDLGenericMotionListener_API24 {
    // Generic Motion (mouse hover, joystick...) events go here
    private boolean mRelativeModeEnabled;

    @Override
    public boolean onGenericMotion(View v, MotionEvent event) {
        if (event.getSource() == InputDevice.SOURCE_JOYSTICK) {
            return SDLControllerManager.handleJoystickMotionEvent(event);
        }

        // Event was not managed
        return false;
    }

    @Override
    public boolean supportsRelativeMouse() {
        return false;
    }

    @Override
    public boolean inRelativeMode() {
        return mRelativeModeEnabled;
    }

    @Override
    public boolean setRelativeMouseEnabled(boolean enabled) {
        return false;
    }

    @Override
    public void reclaimRelativeMouseModeIfNeeded()
    {
    }

    @Override
    public float getEventX(MotionEvent event) {
        // Relative mouse in capture mode will only have relative for X/Y
        return event.getX(0);
    }

    @Override
    public float getEventY(MotionEvent event) {
        // Relative mouse in capture mode will only have relative for X/Y
        return event.getY(0);
    }
}
