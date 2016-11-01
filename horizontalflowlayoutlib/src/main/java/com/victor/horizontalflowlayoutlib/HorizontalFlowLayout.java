package com.victor.horizontalflowlayoutlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/** 横向流布局
 * Created by Victor on 2016/11/1.
 */
public class HorizontalFlowLayout extends ViewGroup {

    /**默认的行间距和竖直间距 8dip*/
    public static int DEFAULT_SPACE = 8;

    /**默认的最大行数 100*/
    public static int DEFAULT_MAX_LINES = 100;

    /**当前行已经使用的宽度*/
    private int mUsedWidth;

    /**子控件之间的横向间距*/
    private int mHorizontalSpace;

    /**子控件之间的竖直间距*/
    private int mVerticalSpace;

    /**最大行数*/
    private int mMaxLines = DEFAULT_MAX_LINES;

    /**维护所有行的集合*/
    private List<Line> mLineList = new ArrayList<>();


    /**当前行对象*/
    private Line mLine;

    public HorizontalFlowLayout(Context context) {
        this(context, null);

    }

    public HorizontalFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHorizontalSpace = dip2px(DEFAULT_SPACE);
        mVerticalSpace =dip2px(DEFAULT_SPACE);

        // 获取属性值
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HorizontalFlowLayout);
        mHorizontalSpace = a.getDimensionPixelSize(R.styleable.HorizontalFlowLayout_horizontalSpace, mHorizontalSpace);
        mVerticalSpace = a.getDimensionPixelSize(R.styleable.HorizontalFlowLayout_verticalSpace, mVerticalSpace);
        mMaxLines = a.getDimensionPixelSize(R.styleable.HorizontalFlowLayout_maxLines, mMaxLines);

        a.recycle();
    }

    /**
     * 获取行间距
     * @return 返回行间距 单位 px
     */
    public int getHorizontalSpace() {
        return mHorizontalSpace;
    }

    /**
     * 设置行间距
     * @param horizontalSpace 行间距 单位 dip
     */
    public void setHorizontalSpace(int horizontalSpace) {
        if (mHorizontalSpace != horizontalSpace) {
            mHorizontalSpace = dip2px(horizontalSpace);
            requestLayout();
        }
    }

    /**
     * 获取竖直间距
     * @return 竖直间距 单位 px
     */
    public int getVerticalSpace() {
        return mVerticalSpace;
    }

    /***
     * 设置竖直间距
     * @param verticalSpace 竖直间距 单位dip
     */
    public void setVerticalSpace(int verticalSpace) {
        if (mVerticalSpace != verticalSpace) {
            mVerticalSpace = dip2px(verticalSpace);
            requestLayout();
        }
    }

    /**
     * 获取最大行数
     * @return 最大行数
     */
    public int getMaxLines() {
        return mMaxLines;
    }

    /**
     * 设置最大行数
     * @param maxLines 最大行数
     */
    public void setMaxLines(int maxLines) {
        if (mMaxLines != maxLines) {
            mMaxLines = maxLines;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // 清空数据防止重复调用
        mLineList.clear();
        mLine = new Line();
        mUsedWidth = 0;

        // 获取有效宽度
        int availableWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        // 获取有效高度
        int availableHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        // 获取宽高模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() == GONE) {
                continue;
            }
            // 根据父控件的模式确定子控件的模式
            int childWithMeasureSpec = MeasureSpec.makeMeasureSpec(availableWidth,
                    widthMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : widthMode);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(availableHeight,
                    heightMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : heightMode);

            // 开始测量子控件的宽高
            childView.measure(childWithMeasureSpec, childHeightMeasureSpec);

            if (mLine == null) { // 判断当前行对象是否为空
                mLine = new Line();
            }

            int childWidth = childView.getMeasuredWidth();
            // 重新计算当前行已使用的宽度
            mUsedWidth += childWidth;
            // 判断加上当前子控件宽度是否超过整个宽度
            if (mUsedWidth <= availableWidth) {
                // 未超出 往行里添加子控件
                mLine.addView(childView);
                mUsedWidth += mHorizontalSpace;
                if(mUsedWidth >= availableWidth) { // 如果加上水平间距超出边距
                    // 开启新的一行
                    if(!lineFeed()) { // 如果换行失败 结束循环
                        break;
                    }
                }

            } else {
                // 已超出
                // 当前行是空行 但是子控件宽度超过行宽
                if (mLine.getChildCount() == 0) {
                    mLine.addView(childView); // 强制添加
                    if(!lineFeed()) { // 换行
                        break;
                    }
                } else {// 当前行已经有控件 添加子控件后宽度超过行宽
                    // 先换行
                    if (!lineFeed()) {
                        break;
                    }
                    mLine.addView(childView);
                    mUsedWidth += childWidth + mHorizontalSpace;
                }

            }
        }

        // 保存最后一行的数据
        if (mLine != null && mLine.getChildCount() > 0 && !mLineList.contains(mLine)) {
            mLineList.add(mLine);
        }

        // 获取整个FlowLayout的宽度和高度
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = 0;
        for (int i =0; i < mLineList.size(); i++) {
            Line line = mLineList.get(i);
            height += line.mMaxHeight;
        }
        // 加上竖直间距
        height += (mLineList.size() - 1) * mVerticalSpace;
        // 加上空白边界
        height += getPaddingTop() + getPaddingBottom();
        // 设置尺寸
        setMeasuredDimension(width, height);
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        for (int i =0; i< mLineList.size(); i++) {
            Line line = mLineList.get(i);
            line.onLayout(left, top);

            // 为下一行 更改top
            top += line.mMaxHeight + mVerticalSpace;
        }
    }

    /**
     * 换行
     * @return true 换行成功 false 换行失败
     */
    private boolean lineFeed() {
        mLineList.add(mLine); // 保存上一行数据
        if (mLineList.size() < mMaxLines) {
            mLine = new Line();
            mUsedWidth = 0;

            return true;
        }
        return false;
    }

    /** dip转为px
     * @param dip
     * @return px大小
     */
    public int dip2px(float dip) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f);
    }

    /**
     * 行对象
     */
    class Line {

        /**当前行所有控件的宽度*/
        private int mTotalWidth = 0;

        /**用于保存当前行已有控件的最高高度*/
        public int mMaxHeight = 0;

        /**用于保存当前行对象已有控件的集合*/
        private List<View> mChildViewList = new ArrayList<>();

        /**
         * 往行里面添加控件
         * @param view 需要添加的控件
         */
        public void addView(View view) {
            mChildViewList.add(view);
            mTotalWidth += view.getMeasuredWidth(); // 更新当前行的总宽度
            if(view.getMeasuredHeight() > mMaxHeight) {
                // 更新当前行的最高高度
                mMaxHeight = view.getMeasuredHeight();
            }
        }

        /**
         * 获取当前行子控件的数量
         * @return 当前行子控件的数量
         */
        public int getChildCount() {
            return mChildViewList.size();
        }

        /**
         * 子控件位置设置
         * @param left 左边距
         * @param top 上边距
         */
        public void onLayout(int left, int top) {
            int childCount = getChildCount();
            // 计算剩余空间(当前行没有刚好排满的情况)
            int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            int surplusWidht = availableWidth - mTotalWidth
                    - (childCount - 1) * mHorizontalSpace;

            if (surplusWidht >= 0) {
                int avgSpace = (int) (surplusWidht * 1.0f / childCount + 0.5f);

                // 重新测量子控件
                for (int i =0;i < childCount; i++) {
                    View childView = mChildViewList.get(i);
                    int measuredWidth = childView.getMeasuredWidth();
                    int measuredHeight = childView.getMeasuredHeight();

                    measuredWidth += avgSpace; // 增加宽度

                    if (avgSpace > 0) { // 如果剩余平均剩余空间大于0 需要重新测量
                        // 重新测量控件
                        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY);
                        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY);
                        childView.measure(widthMeasureSpec, heightMeasureSpec);

                    }

                    // 计算控件顶部偏移量(控件小于最大高度的时候 居中显示)
                    int topOffset = (int) ((mMaxHeight - measuredHeight) / 2.0 + 0.5f);
                    if (topOffset < 0) {
                        topOffset = 0;
                    }

                    // 布局控件
                    childView.layout(left, top + topOffset, left + measuredWidth, top + topOffset + measuredHeight);
                    left += measuredWidth + mHorizontalSpace;
                }
            } else {
                // 一个控件很长 超过了整个行宽
                View childView = mChildViewList.get(0);
                childView.layout(left, top,
                        left + childView.getMeasuredWidth(),
                        top + childView.getMeasuredHeight());
            }
        }
    }
}
