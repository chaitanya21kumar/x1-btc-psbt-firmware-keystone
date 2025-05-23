/*
 * Copyright (c) 2021 X1-BTC-PSBT-Firmware
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file LICENSE.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.x1-btc-psbt-firmware.cold.ui.views;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Keep;

import com.x1-btc-psbt-firmware.cold.R;

public class CircleProgressView extends View {
    private float mProgress;
    private float mCircleWidth;
    private float backgroundStrokeWidth;

    private int mCircleColor;
    private int mBackgroundColor;

    private int mTextColor;

    private RectF mRectF;
    private Paint mBackgroundPaint;
    private Paint mCirclePaint;
    private Interpolator mInterpolator;

    private boolean mIsTextEnabled;

    private String mTextPrefix;
    private String mTextSuffix;

    private float mStartAngle;

    private TextView mTextView;

    private int mTextSize;
    LinearLayout mLayout;

    private ProgressAnimationListener progressAnimationListener;

    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mRectF = new RectF();

        setDefaultValues();
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircularProgressView, 0, 0);

        try {
            mProgress = typedArray.getFloat(R.styleable.CircularProgressView_cpv_progress, mProgress);
            mCircleWidth = typedArray.getDimension(R.styleable.CircularProgressView_cpv_circle_width, mCircleWidth);
            backgroundStrokeWidth = typedArray.getDimension(R.styleable.CircularProgressView_cpv_background_circle_width, backgroundStrokeWidth);
            mCircleColor = typedArray.getInt(R.styleable.CircularProgressView_cpv_circle_color, mCircleColor);
            mBackgroundColor = typedArray.getInt(R.styleable.CircularProgressView_cpv_background_circle_color, mBackgroundColor);
            mTextColor = typedArray.getInt(R.styleable.CircularProgressView_cpv_text_color, mTextColor);
            mTextSize = typedArray.getInt(R.styleable.CircularProgressView_cpv_text_size, mTextSize);
            mTextPrefix = typedArray.getString(R.styleable.CircularProgressView_cpv_text_prefix);
            mTextSuffix = typedArray.getString(R.styleable.CircularProgressView_cpv_text_suffix);
        } finally {
            typedArray.recycle();
        }

        // Init Background
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(backgroundStrokeWidth);

        // Init Circle
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(mCircleWidth);

        // Init TextView
        mTextView = new TextView(context);
        mTextView.setVisibility(View.VISIBLE);
        mTextView.setTextSize(mTextSize);
        mTextView.setTextColor(mTextColor);

        // Init Layout
        mLayout = new LinearLayout(context);
        mLayout.addView(mTextView);
        showTextView(mIsTextEnabled);
    }

    @SuppressLint("SetTextI18n")
    private void showTextView(boolean mIsTextEnabled) {
        mTextView.setText(getTextPrefix() +
                Math.round(mProgress) +
                getTextSuffix());
        mTextView.setVisibility(mIsTextEnabled ? View.VISIBLE : View.GONE);
        invalidate();
    }

    private void setDefaultValues() {
        mProgress = 0;
        mCircleWidth = getResources().getDimension(R.dimen.default_circle_width);
        backgroundStrokeWidth = getResources().getDimension(R.dimen.default_circle_background_width);
        mCircleColor = Color.BLACK;
        mTextColor = Color.BLACK;
        mBackgroundColor = Color.GRAY;
        mStartAngle = -90;
        mIsTextEnabled = true;
        mTextSize = 20;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw Background Circle
        canvas.drawOval(mRectF, mBackgroundPaint);

        // Draw Circle
        float angle = 360 * mProgress / 100;
        canvas.drawArc(mRectF, mStartAngle, angle, false, mCirclePaint);

        // Draw TextView
        mLayout.measure(getWidth(), getHeight());
        mLayout.layout(0, 0, getWidth(), getHeight());
        canvas.translate(getWidth() / 2 - mTextView.getWidth() / 2,
                getHeight() / 2 - mTextView.getHeight() / 2);
        mLayout.draw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        float stroke = (mCircleWidth > backgroundStrokeWidth) ? mCircleWidth : backgroundStrokeWidth;
        mRectF.set(0 + stroke / 2, 0 + stroke / 2, min - stroke / 2, min - stroke / 2);
    }

    public float getCircleWidth() {
        return mCircleWidth;
    }

    public void setCirclerWidth(float circleWidth) {
        this.mCircleWidth = circleWidth;
        mCirclePaint.setStrokeWidth(circleWidth);
        requestLayout();
        invalidate();
    }

    public int getCircleColor() {
        return mCircleColor;
    }

    public void setCircleColor(int circleColor) {
        this.mCircleColor = circleColor;
        mCirclePaint.setColor(circleColor);
        invalidate();
    }

    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public String getTextPrefix() {
        return mTextPrefix != null ? mTextPrefix : "";
    }

    public void setTextPrefix(String textPrefix) {
        this.mTextPrefix = textPrefix;
        showTextView(mIsTextEnabled);
    }

    public String getTextSuffix() {
        return mTextSuffix != null ? mTextSuffix : "";
    }

    public void setTextSuffix(String textSuffix) {
        this.mTextSuffix = textSuffix;
        showTextView(mIsTextEnabled);
    }

    public float getProgress() {
        return mProgress;
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void setTextSize(int textSize) {
        this.mTextSize = textSize;
        mTextView.setTextSize(textSize);
        invalidate();
    }

    public boolean isTextEnabled() {
        return mIsTextEnabled;
    }

    public void setTextEnabled(boolean isTextEnabled) {
        this.mIsTextEnabled = isTextEnabled;
        showTextView(isTextEnabled);
    }

    public float getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(float startAngle) {
        this.mStartAngle = startAngle;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
        mTextView.setTextColor(textColor);
        invalidate();
    }

    @Keep
    public void setProgress(float progress) {
        this.mProgress = (progress <= 100) ? progress : 100;
        mTextView.setText(mTextPrefix + Math.round(mProgress) + mTextSuffix);
        showTextView(mIsTextEnabled);
        invalidate();

        if (progressAnimationListener != null) {
            progressAnimationListener.onValueChanged(progress);
        }
    }

    @SuppressLint("SetTextI18n")
    public void setProgressWithAnimation(final float progress, int duration) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "progress", progress);
        objectAnimator.setDuration(duration);
        objectAnimator.setInterpolator(mInterpolator != null ? mInterpolator : new DecelerateInterpolator());
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mProgress = (progress <= 100) ? progress : 100;
                if (progressAnimationListener != null) {
                    progressAnimationListener.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.addUpdateListener(animation -> mTextView.setText(mTextPrefix +
                Math.round((Float) animation.getAnimatedValue()) +
                mTextSuffix));
        objectAnimator.start();

        if (progressAnimationListener != null) {
            progressAnimationListener.onValueChanged(progress);
        }
    }

    public ProgressAnimationListener getProgressAnimationListener() {
        return progressAnimationListener;
    }

    public void addAnimationListener(ProgressAnimationListener progressAnimationListener) {
        this.progressAnimationListener = progressAnimationListener;
    }

    public interface ProgressAnimationListener {

        void onValueChanged(float value);

        void onAnimationEnd();
    }
}