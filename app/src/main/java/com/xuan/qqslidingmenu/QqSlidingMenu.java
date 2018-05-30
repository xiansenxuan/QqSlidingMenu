package com.xuan.qqslidingmenu;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;

/**
 * com.xuan.qqslidingmenu
 *
 * @author by xuan on 2018/5/26
 * @version [版本号, 2018/5/26]
 * @update by xuan on 2018/5/26
 * @descript
 */
public class QqSlidingMenu extends HorizontalScrollView {
    //获取屏幕宽
    int screenWidth=getResources().getDisplayMetrics().widthPixels;
    int menuWidth= screenWidth-240;

    //最小透明度
    private float minAlpha= 0.3f;
    //阴影
    private float minShadow=0.5f;

    private ViewGroup menuView;
    private ViewGroup contentView;
    private View shadowView;

    //快速滑动处理
    private GestureDetector gestureDetector;

    private boolean menuViewIsOpen=false;

    private boolean isIntercept=false;

    public QqSlidingMenu(Context context) {
        this(context,null);
    }

    public QqSlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public QqSlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        gestureDetector=new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
//            @Override
//            public boolean onDown(MotionEvent e) {
//                return false;
//            }
//
//            @Override
//            public void onShowPress(MotionEvent e) {
//
//            }
//
//            @Override
//            public boolean onSingleTapUp(MotionEvent e) {
//                return false;
//            }
//
//            @Override
//            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//                return false;
//            }
//
//            @Override
//            public void onLongPress(MotionEvent e) {
//
//            }
//
//            @Override
//            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//                return false;
//            }
//        });

        gestureDetector=new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                        //快速滑动
                        Log.i("TAG","velocityX "+velocityX);
                        Log.i("TAG","velocityY "+velocityY);
                        if(menuViewIsOpen){
                            //关闭menu 往左边快速滑动 velocityX 负数
                            if(velocityX<0 && Math.abs(velocityX)>Math.abs(velocityY)){
                                closeMenu();
                                return true;
                            }

                        }else{
                            //打开menu 往右快速滑动 velocityX 正数
                            if(velocityX>0 && Math.abs(velocityX)>Math.abs(velocityY)){
                                openMenu();
                                return true;
                            }

                        }

                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                });
    }

    /*

    LayoutInflater -
        void rInflate(XmlPullParser parser, View parent, Context context,
                AttributeSet attrs, boolean finishInflate){

                    if (finishInflate) {
                        parent.onFinishInflate();
                    }
                }
    */
    @Override//布局解析完毕 调用 是在onLayout之前调用
    protected void onFinishInflate() {
        super.onFinishInflate();
        //获取根部局LinearLayout
        ViewGroup linearLayout= (ViewGroup) getChildAt(0);

        if(linearLayout.getChildCount()>2){
            throw  new RuntimeException("KgSlidingMenu的子布局最多2个");
        }

        // include_menu 屏幕宽度-右边菜单缩放后的宽度
        menuView = (ViewGroup) linearLayout.getChildAt(0);
        ViewGroup.LayoutParams menuParams = menuView.getLayoutParams();
        menuParams.width=menuWidth;
        menuView.setLayoutParams(menuParams);

        //此处无效 因为这个方法是在 onLayout之前执行
        // onLayout方法执行又重新摆放了
        //初始化显示include_content布局
        //scrollTo(width,0);//移动一个屏幕的距离 显示include_content

        addCotentShadow(linearLayout);
    }

    //把content移除 然后套一层阴影 再添加进去
    private void addCotentShadow(ViewGroup linearLayout) {
        // include_content 宽度=屏幕宽度
        contentView = (ViewGroup) linearLayout.getChildAt(1);

        //移除布局
        linearLayout.removeView(contentView);
        //新增外层布局
        RelativeLayout relativeLayout=new RelativeLayout(getContext());
        //给新增的外层布局和content 设置宽度
        ViewGroup.LayoutParams contentParams = contentView.getLayoutParams();
        contentParams.width=screenWidth;
        contentView.setLayoutParams(contentParams);
        relativeLayout.setLayoutParams(contentParams);
        //添加到 relativeLayout 底部
        relativeLayout.addView(contentView);
        //创建阴影
        shadowView = new View(getContext());
        shadowView.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.shadow));
        shadowView.setAlpha(0.0f);
        //添加到 relativeLayout 顶部
        relativeLayout.addView(shadowView);

        //添加到原位置
        linearLayout.addView(relativeLayout);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        scrollTo(screenWidth,0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(isIntercept==true){
            return true;
        }

        //当手指滑动速率 大于某个速率 就认为是快速滑动
        //如果执行了快速滑动 下面代表不执行 避免冲突导致快速滑动无效
        if(gestureDetector.onTouchEvent(ev)){
            return gestureDetector.onTouchEvent(ev);
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                //初始化现实content的状态 x坐标是 = menuWidth
                //手指抬起移动的距离 大于 menuWidth的一半 显示右边 content
                if(getScrollX()>screenWidth/2){
                    closeMenu();
                }else{
                    //显示左边 menu
                    openMenu();
                }

                //不执行 super.onTouchEvent(ev);
                return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //重置 防止改变状态后拦截onTouch
        isIntercept=false;

        if(menuViewIsOpen){
            //菜单打开的时候 触摸右边 关闭
            Log.i("TAG","onInterceptTouchEvent ev.getX() "+ev.getX());
            if(ev.getX()>menuWidth){
                closeMenu();
                //拦截右边菜单事件 子view不响应触摸事件
                // 返回true 拦截子 view touch事件 会 响应自身的 onTouch事件
                isIntercept=true;
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void openMenu() {
        // smoothScrollTo(); 有移动动画 但是没有移动
        // 需要onTouchEvent return true;
        // ScrollTo(); 没有动画 但是可以直接移动
        smoothScrollTo(0,0);

        menuViewIsOpen=true;
    }

    private void closeMenu() {
        smoothScrollTo(screenWidth,0);
        menuViewIsOpen=false;
    }

    // 左边缩放+透明度 右边缩放
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
//        Log.i("TAG"," l "+l+" t "+t+" oldl "+oldl+" oldt "+oldt);
        // 向右滑动 l和oldl不断减少 计算缩放比
        float scale=(float)l/menuWidth;// 1-0

        // 0.3 - 1
        float leftAlpha=minAlpha+(1-scale)*(1-minAlpha);
        leftAlphaAnim(leftAlpha);

        //右边阴影 0 - 0.7
        rightShadow((1-minAlpha)*(1-scale));

        //退出按钮刚  开始在右边，划出时出字的变化
        ViewCompat.setTranslationX(menuView,0.17f*l);
    }

    private void rightShadow(float rightAlpha) {
        shadowView.setAlpha(rightAlpha);
    }

    private void leftAlphaAnim(float leftAlpha) {
        ViewCompat.setAlpha(menuView,leftAlpha);
    }


}
