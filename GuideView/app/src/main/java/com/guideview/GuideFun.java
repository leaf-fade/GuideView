package com.guideview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * Created by Administrator on 2018/3/2.
 * 使guideView 方便调用，且将其渲染在最外层的frameLayout上
 */

public class GuideFun {

    private ViewGroup parent;
    private View view;

    public GuideFun(View anchorView , int resId) {
        //1.渲染布局进来
        view = LayoutInflater.from(anchorView.getContext()).inflate(resId, null, false);
        GuideView guideView = (GuideView)view.findViewById(R.id.guideView);
        //2.找到FrameLayout id 为content
        parent = findSuitableParent(anchorView);
        guideView.bindViewGroup(parent);
    }

    public static GuideFun makeFun(View anchorView , int resId){
        return new GuideFun(anchorView,resId);
    }

    public void show(){
        //3.content.addView()
        if(view.getParent() != null){
           parent.removeView(view);
        }
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        parent.addView(view,layoutParams);

    }

    private static ViewGroup findSuitableParent(View view) {

        do {
            if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    return (ViewGroup) view;
                }
            }

            if (view != null) {
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        return null;
    }
}
