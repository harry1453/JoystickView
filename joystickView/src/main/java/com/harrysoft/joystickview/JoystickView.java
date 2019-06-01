package com.harrysoft.joystickview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private float centerX, centerY;
    private float baseRadius, hatRadius;
    private int baseA, baseR, baseG, baseB; // ARGB values for base
    private int hatA, hatR, hatG, hatB; // ARGB values for hat
    private int stickShadeR, stickShadeG, stickShadeB; // ARGB values for stick shade
    private int hatShadeA, hatShadeR, hatShadeG, hatShadeB; // ARGB values for the hat shade
    private boolean drawBase, shadeBase, shadeHat;
    private int ratio; //The smaller, smoother shading will occur

    @Nullable
    private JoystickListener joystickListener;

    private void setupDimensions() {
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        baseRadius = Math.min(getWidth() * 0.93f, getHeight() * 0.93f) / 3;
        hatRadius = Math.min(getWidth() * 0.93f, getHeight() * 0.93f) / 5;
    }

    public JoystickView(Context context, AttributeSet attributes, int style) {
        super(context, attributes, style);
        setupJoystickView();
        initAttributes(context, attributes);
    }

    public JoystickView(Context context, AttributeSet attributes) {
        super(context, attributes);
        setupJoystickView();
        initAttributes(context, attributes);

    }

    private void setupJoystickView() {
        getHolder().addCallback(this);
        setOnTouchListener(this);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
    }

    public void initAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.JoystickView);

        int base = a.getColor(R.styleable.JoystickView_base_color, 0);
        int hat = a.getColor(R.styleable.JoystickView_hat_color, 0);
        int stickShade = a.getColor(R.styleable.JoystickView_stick_shade_color, 0);
        int hatShade = a.getColor(R.styleable.JoystickView_hat_shade_color, 0);

        // Conversion from int to ARGB value
        baseA = (base >> 24) & 0xff;
        baseR = (base >> 16) & 0xff;
        baseG = (base >> 8) & 0xff;
        baseB = base & 0xff;

        hatA = (hat >> 24) & 0xff;
        hatR = (hat >> 16) & 0xff;
        hatG = (hat >> 8) & 0xff;
        hatB = hat & 0xff;

        stickShadeR = (stickShade >> 16) & 0xff;
        stickShadeG = (stickShade >> 8) & 0xff;
        stickShadeB = stickShade & 0xff;

        hatShadeA = (hatShade >> 24) & 0xff;
        hatShadeR = (hatShade >> 16) & 0xff;
        hatShadeG = (hatShade >> 8) & 0xff;
        hatShadeB = hatShade & 0xff;

        ratio = a.getInteger(R.styleable.JoystickView_ratio, 5);
        shadeBase = a.getBoolean(R.styleable.JoystickView_draw_stick_shading, true);
        shadeHat = a.getBoolean(R.styleable.JoystickView_draw_hat_shading, true);
        drawBase = a.getBoolean(R.styleable.JoystickView_draw_base, true);

        a.recycle();
    }

    private void drawJoystick(float newX, float newY) {
        if(getHolder().getSurface().isValid()) {
            Canvas myCanvas = this.getHolder().lockCanvas(); // Stuff to draw
            Paint colors = new Paint();
            myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // Clear the BG

            // First determine the sin and cos of the angle that the touched point is at relative to the center of the joystick
            float hypotenuse = (float) Math.sqrt(Math.pow(newX - centerX, 2) + Math.pow(newY - centerY, 2));
            float sin = (newY - centerY) / hypotenuse; // sin = o/h
            float cos = (newX - centerX) / hypotenuse; // cos = a/h

            // Draw the base first before shading
            if (drawBase) {
                colors.setARGB(baseA, baseR, baseG, baseB);
                myCanvas.drawCircle(centerX, centerY, baseRadius, colors);
            }

            // Draw stick shading
            if (shadeBase) {
                for (int i = 1; i <= (int) (baseRadius / ratio); i++) {
                    colors.setARGB(150 / i, stickShadeR, stickShadeG, stickShadeB); // Gradually decrease the shade of black drawn to create a nice shading effect
                    myCanvas.drawCircle(newX - cos * hypotenuse * (ratio / baseRadius) * i, newY - sin * hypotenuse * (ratio / baseRadius) * i, i * (hatRadius * ratio / baseRadius), colors); // Gradually increase the size of the shading effect
                }
            }

            // Drawing the joystick hat
            if (shadeHat) {
                for (int i = 0; i <= (int) (hatRadius / ratio); i++) { // TODO: Change (int) (hatRadius / ratio) to converge to a custom color
                    colors.setARGB(255, (int) (i * (hatR * ratio / hatRadius)), (int) (i * (hatG * ratio / hatRadius)), (int) (i * (hatB * ratio / hatRadius))); // Change the joystick color for shading purposes
                    myCanvas.drawCircle(newX, newY, hatRadius - (float) i * (ratio) / 2, colors); //Draw the shading for the hat
                }
            } else {
                colors.setARGB(hatA, hatR, hatG, hatB);
                myCanvas.drawCircle(newX, newY, hatRadius, colors);
            }

            getHolder().unlockCanvasAndPost(myCanvas); // Write the new drawing to the SurfaceView
        }
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setupDimensions();
        drawJoystick(centerX, centerY);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public boolean onTouch(View v, MotionEvent e) {
        if(v.equals(this)) {
            if(e.getAction() != MotionEvent.ACTION_UP) {
                float displacement = (float) Math.sqrt((Math.pow(e.getX() - centerX, 2)) + Math.pow(e.getY() - centerY, 2));
                if(displacement < baseRadius) {
                    drawJoystick(e.getX(), e.getY());
                    updateListener((e.getX() - centerX)/baseRadius, (e.getY() - centerY)/baseRadius);
                } else {
                    float ratio = baseRadius / displacement;
                    float constrainedX = centerX + (e.getX() - centerX) * ratio;
                    float constrainedY = centerY + (e.getY() - centerY) * ratio;
                    drawJoystick(constrainedX, constrainedY);
                    updateListener((constrainedX-centerX)/baseRadius, (constrainedY-centerY)/baseRadius);
                }
            }
            else {
                drawJoystick(centerX, centerY);
                updateListener(0,0);
            }
        }
        return true;
    }

    private void updateListener(float xPercent, float yPercent) {
        if (joystickListener != null) {
            joystickListener.onJoystickMoved(xPercent, yPercent, getId());
        }
    }

    public void setJoystickListener(@Nullable JoystickListener joystickListener) {
        this.joystickListener = joystickListener;
    }

    public interface JoystickListener {
        void onJoystickMoved(float xPercent, float yPercent, int id);
    }
}
