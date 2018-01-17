package yhb.chorus.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.SeekBar;

import yhb.chorus.R;


public class MySeekBar extends android.support.v7.widget.AppCompatSeekBar{
    private static final int DEFAULT_THUMB_COLOR = 0xff000000;
    private static final int DEFAULT_SEEKBAR_HEIGHT = 5;//dp
    private static final int DEFAULT_REACH_COLOR = 0xffffffff;
    private static final int DEFAULT_UNREACH_COLOR = 0xffffffff ;
    private static final int DEFAULT_THUMB_WIDTH = 6;//dp

    //声明成员变量
    private int mThumbColor = DEFAULT_THUMB_COLOR;
    private int mThumbWidth = dp2px(DEFAULT_THUMB_WIDTH);
    private int mReachColor = DEFAULT_REACH_COLOR;
    private int mUnReachColor = DEFAULT_UNREACH_COLOR;
    private int mSeekBarHeight = dp2px(DEFAULT_SEEKBAR_HEIGHT);

    private Paint mPaint;
    public MySeekBar(Context context) {
        this(context,null);
    }

    public MySeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MySeekBar);
        mReachColor = typedArray.getColor(R.styleable.MySeekBar_seekBar_reach_color,mReachColor);
        mThumbColor = typedArray.getColor(R.styleable.MySeekBar_seekBar_thumb_color,mThumbColor);
        mThumbWidth = (int) typedArray.getDimension(R.styleable.MySeekBar_seekBar_thumb_width,mThumbWidth);
        mUnReachColor = typedArray.getColor(R.styleable.MySeekBar_seekBar_unreach_color,mUnReachColor);
        mSeekBarHeight = (int) typedArray.getDimension(R.styleable.MySeekBar_seekBar_height,mSeekBarHeight);

        mPaint = new Paint();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        setPadding(0,getPaddingTop(),0,getPaddingBottom());
        mPaint.setStrokeWidth(mSeekBarHeight);
        canvas.translate(0,getMeasuredHeight()/2);
        int reachDeltaX = (int) (getProgress()*1.0f/getMax()*(getMeasuredWidth()-getPaddingRight()-getPaddingLeft())-mThumbWidth/2);

        if(reachDeltaX>0){
            mPaint.setColor(mReachColor);
            canvas.drawLine(getPaddingLeft(),0,getPaddingLeft()+reachDeltaX,0,mPaint);
        }

        mPaint.setColor(mThumbColor);
        canvas.drawLine(getPaddingLeft()+reachDeltaX,0,getPaddingLeft()+reachDeltaX+mThumbWidth,0,mPaint);

        if(reachDeltaX+mThumbWidth<getMeasuredWidth()-getPaddingRight()-getPaddingLeft()){
            mPaint.setColor(mUnReachColor);
            canvas.drawLine(getPaddingLeft()+reachDeltaX+mThumbWidth,0,getMeasuredWidth()-getPaddingRight(),0,mPaint);
        }

    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mWidth = MeasureSpec.getSize(widthMeasureSpec);
        int mHeight;
        if(MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY){
            mHeight = MeasureSpec.getSize(heightMeasureSpec);
        }else{
            mHeight = mSeekBarHeight;
        }
        setMeasuredDimension(mWidth,mHeight);
    }

    private int dp2px(int dpVal){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dpVal,getResources().getDisplayMetrics());
    }
}
