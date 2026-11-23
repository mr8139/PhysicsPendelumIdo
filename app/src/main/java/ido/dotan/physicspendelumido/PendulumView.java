package ido.dotan.physicspendelumido;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PendulumView extends View {
    private Paint paint;
    private float length = 300; // Length in pixels
    private float angle = (float) Math.PI / 4; // Initial angle 45 degrees
    private float angularVelocity = 0;
    private float angularAcceleration = 0;
    private final float g = 9.8f; // Gravity
    private long lastTime = 0;

    public PendulumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);
    }

    public void setLength(float length) {
        this.length = length;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long currentTime = System.currentTimeMillis();
        if (lastTime == 0) {
            lastTime = currentTime;
        }
        float dt = (currentTime - lastTime) / 1000f;
        lastTime = currentTime;

        // Physics update (Simple Euler integration)
        // Adjust g for the scale of pixels if needed, but let's keep it simple
        // To make it look "real", we can scale length to meters for calculation
        float lengthInMeters = length / 1000f; 
        angularAcceleration = -(g / lengthInMeters) * (float) Math.sin(angle);
        angularVelocity += angularAcceleration * dt;
        angle += angularVelocity * dt;

        // Damping (optional, to make it stop eventually)
        angularVelocity *= 0.995f;

        int width = getWidth();
        int height = getHeight();
        float cx = width / 2f;
        float cy = 50; // Pivot point Y

        float stopX = cx + (float) (length * Math.sin(angle));
        float stopY = cy + (float) (length * Math.cos(angle));

        // Draw string
        paint.setColor(Color.BLACK);
        canvas.drawLine(cx, cy, stopX, stopY, paint);

        // Draw pivot
        paint.setColor(Color.RED);
        canvas.drawCircle(cx, cy, 10, paint);

        // Draw bob
        paint.setColor(Color.BLUE);
        canvas.drawCircle(stopX, stopY, 30, paint);

        // Request next frame
        invalidate();
    }
}
