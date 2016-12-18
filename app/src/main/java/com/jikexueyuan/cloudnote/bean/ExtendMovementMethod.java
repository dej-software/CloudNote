package com.jikexueyuan.cloudnote.bean;

import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by dej on 2016/12/14.
 * 扩展的MovementMethod 继承LinkMovementMethod
 * 用来处理图片点击事件(LinkMovementMethod已经支持链接点击UrlSpan)
 * UrlSpan 和 ImageSpan
 */

public class ExtendMovementMethod extends LinkMovementMethod {

    public static final int MESSAGE_WHAT = 100;
    private static ExtendMovementMethod sInstance;
    private Handler handler = null;
    private Class spanClass = null;

    int xSatrt, yStart, xEnd, yEnd;

    public static MovementMethod getInstance(Handler handler, Class spanClass) {
        if (sInstance == null) {
            sInstance = new ExtendMovementMethod(handler, spanClass);
        }

        return sInstance;
    }

    public ExtendMovementMethod(Handler handler, Class spanClass) {
        this.handler = handler;
        this.spanClass = spanClass;
    }

    /**
     * 复写其onTouchEvent方法
     *
     * @param widget
     * @param buffer
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            xSatrt = (int) event.getX();
            yStart = (int) event.getY();
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            xEnd = (int) event.getX();
            yEnd = (int) event.getY();
        }

        if (Math.abs(xEnd - xSatrt) < 10 && Math.abs(yEnd - yStart) < 10) {

            System.out.println("begin: " + xEnd + "--" + yEnd);
//            xEnd -= widget.getTotalPaddingLeft();
//            yEnd -= widget.getTotalPaddingTop();
//            xEnd += widget.getScrollX();
//            yEnd += widget.getScrollY();

            System.out.println("end: " + xEnd + "--" + yEnd);

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(yEnd);
            int off = layout.getOffsetForHorizontal(line, xEnd);

            System.out.println("off: " + line + "--" + off);

            Object[] spans = buffer.getSpans(off, off, spanClass);
            if (spans.length != 0) {
                System.out.println(spans.length);
                // 设置选中范围
                Selection.setSelection(buffer, buffer.getSpanStart(spans[0]), buffer.getSpanEnd(spans[0]));

                Message message = handler.obtainMessage();
                message.obj = spans[0];
                message.what = MESSAGE_WHAT;
                message.sendToTarget();
                return true;
            }
        }
        return super.onTouchEvent(widget, buffer, event);
    }
}
