package com.guideview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;

/**
 * Created by Administrator on 2018/3/2.
 * 一个遮罩层，可以寻找到控件的位置，并聚焦
 */

public class GuideView extends View{

    private final Paint paint;
    private final AnimatorSet animatorSet;
    //当前的触摸点坐标
    private int pointX =0, pointY=0;
    //上一次的触摸点坐标
    private int lastPointX , lastPointY;
    //选中矩形的左及顶部的长度
    private int rectLeft, rectTop ;
    private int rectNewLeft, rectNewTop;
    //默认长度
    private final int RECT_PADDING = 100;
    //传递过来的content父布局，用于找到触摸点在的view上
    private ViewGroup root;
    private ObjectAnimator leftZoomOutAnimator, topZoomOutAnimator, colLineAnimator ,
            rowLineAnimator, leftZoomInAnimator ,topZoomInAnimator;

    private final int VIEW_INIT = 0x0;
    private final int ANIMATION_INIT = 0x1;
    private final int ANIMATION_START = 0x2;
    private final int ANIMATION_END = 0x4;
    private int state = VIEW_INIT;
    private RectF rect ;

    public GuideView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        animatorSet = new AnimatorSet();
        rectTop = rectLeft = 0;
        rectNewLeft = rectNewTop = RECT_PADDING;
    }

    public void bindViewGroup(ViewGroup parent){
        root = parent;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(state == VIEW_INIT){
            //画大遮罩层
            paint.setColor(0xff656565);
            paint.setAlpha(200);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0,0,getMeasuredWidth(),getMeasuredHeight(),paint);
            state = ANIMATION_INIT;
            return;
        }
        //画大遮罩层,四个矩形
        paint.setColor(0xff656565);
        paint.setAlpha(200);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0,0,getMeasuredWidth(),pointY-rectTop, paint);
        canvas.drawRect(0,pointY-rectTop,pointX-rectLeft,pointY+rectTop, paint);
        canvas.drawRect(pointX+rectLeft, pointY-rectTop, getMeasuredWidth(),pointY+rectTop, paint);
        canvas.drawRect(0,pointY+rectTop,getMeasuredWidth(),getMeasuredHeight(),paint);
        //画矩形选择框
        paint.setAlpha(255);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(6);
        paint.setStyle(Paint.Style.STROKE);
        if(rectLeft !=0 && rectTop != 0){
            canvas.drawRect(pointX - rectLeft, pointY - rectTop, pointX + rectLeft, pointY + rectTop, paint);
        }

        //画竖线
        canvas.drawLine(pointX,0,pointX,pointY - rectTop,paint);
        canvas.drawLine(pointX,pointY + rectTop,pointX,getMeasuredHeight(),paint);
        //画横线
        canvas.drawLine(0,pointY,pointX - rectLeft ,pointY,paint);
        canvas.drawLine(pointX + rectLeft,pointY,getMeasuredWidth(),pointY,paint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_UP:
                //防止多次点击，动画结束后在进行下一个
                if(state == ANIMATION_START){
                    break;
                }
                lastPointX = pointX;
                lastPointY = pointY;
                pointX = (int)event.getX();
                pointY = (int)event.getY();
                View touchView = getPointViewInfo(root,(int)event.getRawX(),(int)event.getRawY());
                if(touchView == null){
                    rectNewTop = rectNewLeft = RECT_PADDING;
                }else {
                    pointX = (int)rect.centerX() - ((int)event.getRawX() - pointX);
                    pointY = (int)rect.centerY() - ((int)event.getRawY() - pointY);
                    rectNewTop = touchView.getHeight()/2 ;
                    rectNewLeft = touchView.getWidth()/2 ;
                }
                startAnimation();
                break;
        }
        return true;
    }

    //遍历viewGroup
    private View getPointViewInfo(View view, int rawX, int rawY){
        if(null == view) {
            return null;
        }
        if(view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            LinkedList<ViewGroup> queue = new LinkedList<>();
            queue.add(viewGroup);
            while(!queue.isEmpty()) {
                //取出并删除节点
                ViewGroup current = queue.removeFirst();
                for(int i = 0; i < current.getChildCount(); i ++) {
                    if(current.getChildAt(i) instanceof ViewGroup) {
                        //加入链表末尾
                        queue.addLast((ViewGroup) current.getChildAt(i));
                        continue;
                    }
                    //Log.i("123", i+"----"+current+"---"+current.getChildAt(i));
                    // View view;
                    if((current.getChildAt(i) instanceof GuideView)){
                        continue;
                    }
                    rect = calcViewScreenLocation(current.getChildAt(i));
                    if(rect.contains(rawX, rawY)){
                        return current.getChildAt(i);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 计算指定的 View 在屏幕中的坐标。
     */
    public static RectF calcViewScreenLocation(View view) {
        int[] location = new int[2];
        // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
        view.getLocationOnScreen(location);
        return new RectF(location[0], location[1], location[0] + view.getWidth(),
                location[1] + view.getHeight());
    }

    private void initAnimation(){
        leftZoomOutAnimator = ObjectAnimator.ofInt(this,"rectLeft",rectLeft, 0);
        topZoomOutAnimator = ObjectAnimator.ofInt(this,"rectTop", rectTop, 0);
        colLineAnimator = ObjectAnimator.ofInt(this,"pointX",lastPointX, pointX);
        rowLineAnimator = ObjectAnimator.ofInt(this,"pointY",lastPointY , pointY);
        leftZoomInAnimator = ObjectAnimator.ofInt(this,"rectLeft", 0 ,rectNewLeft);
        topZoomInAnimator = ObjectAnimator.ofInt(this,"rectTop", 0 , rectNewTop );
        setAnimationListener();
    }
    private void setAnimationListener() {
        leftZoomOutAnimator.addListener(new AnimatorListenerAdapter() {
            int pointX0 , pointY0;
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                pointX = pointX0;
                pointY = pointY0;
                if(leftZoomOutAnimator != null){
                    leftZoomOutAnimator.removeListener(this);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                state = ANIMATION_START;
                pointX0 = pointX;
                pointY0 = pointY;
                pointX = lastPointX;
                pointY = lastPointY;
            }
        });

        leftZoomInAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                state = ANIMATION_END;
                if(leftZoomInAnimator != null){
                    leftZoomInAnimator.removeListener(this);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                rectTop = rectNewTop;
                rectLeft = rectNewLeft;
            }

        });
    }

    private void startAnimation(){
        initAnimation();
        animatorSet.play(colLineAnimator).with(rowLineAnimator);
        animatorSet.play(colLineAnimator).before(leftZoomInAnimator);
        animatorSet.play(colLineAnimator).after(leftZoomOutAnimator);
        animatorSet.play(leftZoomOutAnimator).with(topZoomOutAnimator);
        animatorSet.play(leftZoomInAnimator).with(topZoomInAnimator);
        animatorSet.setDuration(500).start();
    }


    public void setPointX(int pointX) {
        this.pointX = pointX;
    }

    public void setPointY(int pointY) {
        this.pointY = pointY;
        invalidate();
    }

    public void setRectLeft(int rectLeft) {
        this.rectLeft = rectLeft;
    }

    public void setRectTop(int rectTop) {
        this.rectTop = rectTop;
        invalidate();
    }
}
