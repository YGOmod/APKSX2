package kr.co.iefriends.pcsx2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public final class ControllerDPadView extends View {
    // Resource IDs for the drawable resources
    public static final int[] drawableResourceIds = new int[]{
        2131230890, 2131230885, 2131230873, 2131230878
    };
    
    // Array to hold the drawable objects
    public final Drawable[] drawables = new Drawable[4];
    
    // Array to hold button IDs
    public final int[] buttonIds = new int[]{-1, -1, -1, -1};
    
    // Array to track button states (pressed or not)
    public final boolean[] buttonStates = new boolean[4];
    
    // Flag to indicate if the D-Pad is pressed
    public boolean isPressed;
    
    // Pointer ID for touch events
    public int pointerId;
    
    // Current X and Y coordinates of the touch
    public int currentX;
    public int currentY;
    
    // Configuration name
    public String configName;
    
    // Default visibility state
    public boolean defaultVisibility;
    
    // Identifier for the D-Pad
    public int dPadId;

    public ControllerDPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.isPressed = false;
        this.pointerId = -1;
        this.currentX = 0;
        this.currentY = 0;
        this.defaultVisibility = true;
        this.dPadId = -1;

        // Initialize drawables
        for (int i = 0; i < 4; i++) {
            this.drawables[i] = this.getContext().getDrawable(drawableResourceIds[i]);
        }
    }

    // Method to draw a specific button on the canvas
    public final void drawButton(int buttonIndex, int xMultiplier, int yMultiplier, Canvas canvas, int width, int height) {
        int left = xMultiplier * width;
        int right = width + left;
        int top = yMultiplier * height;
        int bottom = height + top;

        // If the button is pressed, adjust the bounds
        if (this.buttonStates[buttonIndex]) {
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.0F, this.getResources().getDisplayMetrics());
            left -= padding;
            right += padding;
            top -= padding;
            bottom += padding;
        }

        // Set bounds and draw the drawable
        Drawable drawable = this.drawables[buttonIndex];
        drawable.setBounds(left, top, right, bottom);
        drawable.draw(canvas);
    }

    // Method to reset the D-Pad state
    public void resetDPadState() {
        if (this.isPressed || this.currentX != 0 || this.currentY != 0) {
            this.isPressed = false;
            this.pointerId = -1;
            this.currentX = 0;
            this.currentY = 0;
            this.updateButtonStates();
            this.invalidate();
        }
    }

    // Method to update button states based on current touch position
    public final void updateButtonStates() {
        int xPosition = this.currentX;
        int viewWidth = this.getWidth();
        int xIndex = (xPosition < 0) ? 0 : Math.min(xPosition, viewWidth) / (viewWidth / 3);

        int yPosition = this.currentY;
        int viewHeight = this.getHeight();
        int yIndex = (yPosition < 0) ? 0 : Math.min(yPosition, viewHeight) / (viewHeight / 3);

        boolean[] buttonStates = this.buttonStates;
        boolean isPressed = this.isPressed;

        buttonStates[0] = isPressed && yIndex == 0;
        buttonStates[1] = isPressed && xIndex >= 2;
        buttonStates[2] = isPressed && yIndex >= 2;
        buttonStates[3] = isPressed && xIndex == 0;

        for (int i = 0; i < 4; i++) {
            if (this.buttonIds[i] >= 0) {
                NativeLibrary.setPadButton(this.dPadId, this.buttonIds[i], this.buttonStates[i]);
            }
        }
    }

    // Getter for configuration name
    public String getConfigName() {
        return this.configName;
    }

    // Getter for default visibility
    public boolean getDefaultVisibility() {
        return this.defaultVisibility;
    }

    // Getter for pointer ID
    public int getPointerId() {
        return this.pointerId;
    }

    // Check if the D-Pad is pressed
    public boolean isPressed() {
        return this.isPressed;
    }

    // Override onDraw to draw the D-Pad
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.getWidth() / 3;
        int height = this.getHeight() / 3;
        this.drawButton(0, 1, 0, canvas, width, height);
        this.drawButton(1, 2, 1, canvas, width, height);
        this.drawButton(2, 1, 2, canvas, width, height);
        this.drawButton(3, 0, 1, canvas, width, height);
    }

    // Setter for configuration name
    public void setConfigName(String configName) {
        this.configName = configName;
    }

    // Setter for default visibility
    public void setDefaultVisibility(boolean defaultVisibility) {
        this.defaultVisibility = defaultVisibility;
    }

    // Setter for pointer ID
    public void setPointerId(int pointerId) {
        this.pointerId = pointerId;
    }
}