package com.example.myapplication;
import android.view.View;
import android.view.Choreographer;
import android.content.Context;
public class RedrawView extends View {
    private final Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            invalidate();
            Choreographer.getInstance().postFrameCallback(this);
        }
    };

    public RedrawView(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Choreographer.getInstance().postFrameCallback(frameCallback);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Choreographer.getInstance().removeFrameCallback(frameCallback);
    }
}
