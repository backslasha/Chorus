package yhb.chorus.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;

import yhb.chorus.R;


public class SlimSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {
    private static final int DEFAULT_THUMB_COLOR = 0xff000000;
    private static final int DEFAULT_SEEK_BAR_HEIGHT = 5;//dp
    private static final int DEFAULT_REACH_COLOR = 0xffffffff;
    private static final int DEFAULT_NOT_REACH_COLOR = 0xffffffff;
    private static final int DEFAULT_THUMB_RADIUS = 6;//dp

    //声明成员变量
    private int mSeekBarHeight = dp2px(DEFAULT_SEEK_BAR_HEIGHT);
    private int mReachColor = DEFAULT_REACH_COLOR;
    private int mUnReachColor = DEFAULT_NOT_REACH_COLOR;
    private int mThumbColor = DEFAULT_THUMB_COLOR;
    private int mThumbRadius = dp2px(DEFAULT_THUMB_RADIUS);

    private Paint mPaint;

    public SlimSeekBar(Context context) {
        this(context, null);
    }

    public SlimSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlimSeekBar);
        mSeekBarHeight = (int) typedArray.getDimension(R.styleable.SlimSeekBar_seekBar_height, mSeekBarHeight);
        mReachColor = typedArray.getColor(R.styleable.SlimSeekBar_seekBar_reach_color, mReachColor);
        mUnReachColor = typedArray.getColor(R.styleable.SlimSeekBar_seekBar_not_reach_color, mUnReachColor);
        mThumbColor = typedArray.getColor(R.styleable.SlimSeekBar_seekBar_thumb_icon, mThumbColor);
        mThumbRadius = (int) typedArray.getDimension(R.styleable.SlimSeekBar_seekBar_thumb_radius, mThumbRadius);
        typedArray.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        setPadding(getPaddingStart(), getPaddingTop(), getPaddingRight(), getPaddingBottom());

        canvas.translate(0, getMeasuredHeight() / 2);

        // 画 reached 部分
        float reachPercent = ((float) getProgress()) / getMax();

        int progressBarWidthReal = getMeasuredWidth() - getPaddingRight() - getPaddingLeft();

        int reachDeltaX = (int) (reachPercent * progressBarWidthReal - mThumbRadius);

        if (reachDeltaX > 0) {
            mPaint.setStrokeWidth(mSeekBarHeight);
            mPaint.setColor(mReachColor);
            canvas.drawLine(getPaddingLeft(), 0, getPaddingLeft() + reachDeltaX, 0, mPaint);
        }

        // 画 thumb
        mPaint.setColor(mThumbColor);
        canvas.drawCircle(getPaddingLeft() + reachDeltaX + mThumbRadius, 0, mThumbRadius, mPaint);

        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(getPaddingLeft() + reachDeltaX + mThumbRadius, 0, mThumbRadius / 3, mPaint);

        // 画 not reached 部分
        int thumbWidth = mThumbRadius * 2;
        if (reachDeltaX + thumbWidth < progressBarWidthReal) {
            mPaint.setColor(mUnReachColor);
            canvas.drawLine(getPaddingLeft() + reachDeltaX + thumbWidth, 0, getMeasuredWidth() - getPaddingRight(), 0, mPaint);
        }

    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mWidth = MeasureSpec.getSize(widthMeasureSpec);
        int mHeight;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            mHeight = MeasureSpec.getSize(heightMeasureSpec);
        } else {
            mHeight = mSeekBarHeight;
        }

        mHeight = Math.max(mThumbRadius * 2, mHeight);

        setMeasuredDimension(mWidth, mHeight);
    }

    private int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getResources().getDisplayMetrics());
    }
}
