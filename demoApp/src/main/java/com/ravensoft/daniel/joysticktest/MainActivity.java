package com.ravensoft.daniel.joysticktest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.harrysoft.joystickview.JoystickView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener{

    private DecimalFormat decimalFormat;
    private TextView leftX, leftY, rightX, rightY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        leftX = findViewById(R.id.left_x);
        leftY = findViewById(R.id.left_y);
        rightX = findViewById(R.id.right_x);
        rightY = findViewById(R.id.right_y);
        JoystickView left = findViewById(R.id.left_joystick);
        JoystickView right = findViewById(R.id.right_joystick);

        decimalFormat = new DecimalFormat("#.###");

        // Initialize display with zero values
        onJoystickMoved(0f, 0f, R.id.left_joystick);
        onJoystickMoved(0f, 0f, R.id.right_joystick);

        left.setJoystickListener(this);
        right.setJoystickListener(this);
    }

    @Override
    public void onJoystickMoved(float xPercent, float yPercent, int id) {
        switch (id) {
            case R.id.left_joystick:
                leftX.setText(getString(R.string.x_pos_format, decimalFormat.format(xPercent)));
                leftY.setText(getString(R.string.y_pos_format, decimalFormat.format(yPercent)));
                break;
            case R.id.right_joystick:
                rightX.setText(getString(R.string.x_pos_format, decimalFormat.format(xPercent)));
                rightY.setText(getString(R.string.y_pos_format, decimalFormat.format(yPercent)));
                break;
        }
    }
}
