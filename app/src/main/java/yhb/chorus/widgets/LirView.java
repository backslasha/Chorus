package yhb.chorus.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.List;

import yhb.chorus.entity.LirBean;


public class LirView extends androidx.appcompat.widget.AppCompatTextView {
    public int getTime() {
        return time;
    }

    private int time = 0;
    private int index = 0;

    private List<LirBean> lirics = null;
    private Paint currentpaint;
    private Paint notCurrentpaint;

    public LirView(Context context) {
        this(context, null);
    }

    public LirView(Context context, AttributeSet attrs) {
        super(context, attrs);

        currentpaint = new Paint();
        currentpaint.setAntiAlias(true);
        currentpaint.setDither(false);
        currentpaint.setTextAlign(Paint.Align.CENTER);// 设置文本对齐方式
        currentpaint.setTextSize(42);
        currentpaint.setTypeface(Typeface.DEFAULT); //设置字体为黑体
        currentpaint.setColor(Color.RED);


        notCurrentpaint = new Paint();
        notCurrentpaint.setAntiAlias(true);
        notCurrentpaint.setDither(false);
        notCurrentpaint.setTextAlign(Paint.Align.CENTER);// 设置文本对齐方式
        notCurrentpaint.setTextSize(38);
        notCurrentpaint.setColor(Color.RED);
        notCurrentpaint.setTypeface(Typeface.DEFAULT);//设置字体为默认
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (lirics != null && lirics.size() > 0) {
            canvas.drawText(lirics.get(index).getLiric(), getWidth() / 2, getHeight() / 2 - 30, currentpaint);//画出当前句子
            /*
             * 在当前句下方画出还没读到的句子
			 */
            float tempY = getHeight() / 2 - 30;
            int alpha = 255;
            for (int i = index + 1; i < lirics.size(); i++) {
                tempY = tempY + 96;
                alpha = alpha - 32;
                notCurrentpaint.setAlpha(alpha);
                canvas.drawText(lirics.get(i).getLiric(), getWidth() / 2, tempY, notCurrentpaint);
            }
            /*
			 * 在当前句上方画出已经读过的句子
			 */
            alpha = 256;
            tempY = getHeight() / 2 - 30;
            for (int i = index - 1; i >= 0; i--) {
                tempY = tempY - 96;
                alpha = alpha - 56;
                notCurrentpaint.setAlpha(alpha);
                canvas.drawText(lirics.get(i).getLiric(), getWidth() / 2, tempY, notCurrentpaint);//在当前句下方画出还没读到的句子
            }
        } else {
            canvas.drawText("没有找到歌词", getWidth() / 2, getHeight() / 2, currentpaint);
        }
    }

    public void setLirics(List<LirBean> lirics) {
        this.lirics = lirics;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setIndex(int index) {
        if (index < 0) {
            this.index = 0;
        } else {
            this.index = index;
        }
    }
}
