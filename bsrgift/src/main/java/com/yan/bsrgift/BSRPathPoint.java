package com.yan.bsrgift;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.ThumbnailUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by yan on 2016/12/8.
 */
public class BSRPathPoint extends BSRPathBase {
    private Bitmap res;
    private Paint paint;
    private Matrix matrix;
    private Interpolator interpolator;

    private BSREvaluator.OnValueBackListener backListener;

    public BSRPathPoint() {
        super();
        paint = new Paint();
        matrix = new Matrix();
    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

/*    public void setRes(Context context, int res) {
        this.res = BitmapFactory.decodeResource(context.getResources(), res);
    }*/

    public void setRes(Context context, String filepath) {

        Bitmap bm = BitmapFactory.decodeFile(filepath);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);

        int screenWidth = dm.widthPixels;               //获取当前屏幕宽度
        int screenHeight = dm.heightPixels;             //获取当前屏幕高度
//        float w = (float) screenWidth / bm.getWidth();  //计算当前图片要全屏幕，宽度需要放大尺寸
//        float h = (float) screenHeight / bm.getHeight();//计算当前图片要全屏，高度需要放大尺寸
//
//        if (w >= h)//选取较小尺寸进行放大
//            w = h;
//
//        Matrix matrix = new Matrix();
//        matrix.postScale(w, w);//设置宽高放大比例（这里为等比例放大）
        this.res = ThumbnailUtils.extractThumbnail(bm,  screenWidth, screenHeight);

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 4;
//        this.res = BitmapFactory.decodeFile(filepath, options);

    }

    public void setAntiAlias(boolean isAlias) {
        paint.setAntiAlias(isAlias);
    }

    public void drawBSRPoint(Canvas canvas, float viewWidth, float viewHeight, boolean isFrameAnimation) {
        screenWidth = viewWidth;
        screenHeight = viewHeight;
        if (isFrameAnimation) {
            if (scaleInScreen != -10000) {
                float timesWidth = viewWidth / res.getWidth();
                float timesHeight = viewHeight / res.getHeight();
                if (timesWidth >= timesHeight) {
                    matrix.setScale(timesHeight * scaleInScreen, timesHeight * scaleInScreen);
                    if (isCenterInside)
                        matrix.preTranslate((viewWidth - res.getWidth() * timesHeight) / 2, 0);
                    else
                        matrix.preTranslate(0, (viewHeight - res.getHeight() * timesHeight * scaleInScreen) / 2);
                } else {
                    matrix.setScale(timesWidth * scaleInScreen, timesWidth * scaleInScreen);
                    if (isCenterInside)
                        matrix.preTranslate(0, (viewHeight - res.getHeight() * timesWidth) / 2);
                    else
                        matrix.preTranslate((viewWidth - res.getWidth() * timesWidth * scaleInScreen) / 2, 0);
                }
            }
            canvas.drawBitmap(res, matrix, paint);

        } else {
            float timesWidth = viewWidth / res.getWidth();
            float timesHeight = viewHeight / res.getHeight();

            float degree = 0;

            if (attachPathBase != null) {
                matrix.set(((BSRPathPoint) attachPathBase).getMatrix());
                matrix.preTranslate(
                        (isPositionInScreen ? attachDx * screenWidth : attachDx)
                        , (isPositionInScreen ? attachDy * screenHeight : attachDy));
            } else {
                if (trueRotation == -10000) {
                    if (lastPoint == null) {
                        lastPoint = new PointF();
                        lastPoint.set(truePointX, truePointY);
                    }
                    degree = getRotationPoint2Point(lastPoint.x, lastPoint.y, truePointX, truePointY);
                }

                if (isCenterInside) {
                    if (timesWidth > timesHeight) {
                        matrix.setTranslate((viewWidth) / 2, 0);
                        matrix.preScale(timesHeight * scaleInScreen, timesHeight * scaleInScreen);
                    } else {
                        matrix.setTranslate(0, (viewHeight / 2));
                        matrix.preScale(timesWidth * scaleInScreen, timesWidth * scaleInScreen);
                    }
                } else {
                    matrix.setTranslate(
                            truePointX - res.getWidth() * xPositionPercent
                            , truePointY - res.getHeight() * yPositionPercent
                    );
                }
            }

            if (!isCenterInside && scaleInScreen != -10000) {
                if (timesWidth > timesHeight) {
                    matrix.preScale(timesHeight * scaleInScreen, timesHeight * scaleInScreen, res.getWidth() * xPercent, res.getHeight() * yPercent);
                } else {
                    matrix.preScale(timesWidth * scaleInScreen, timesWidth * scaleInScreen, res.getWidth() * xPercent, res.getHeight() * yPercent);
                }
            }

            if (!isCenterInside && trueScaleValue != -1) {
                matrix.preScale(trueScaleValue, trueScaleValue, res.getWidth() * xPercent, res.getHeight() * yPercent);
            }

            if (trueRotation == -10000) {
                matrix.preRotate(degree + getFirstRotation(), res.getWidth() * xPercent, res.getHeight() * yPercent);
                if (lastPoint != null)
                    lastPoint.set(truePointX, truePointY);
            } else {
                matrix.preRotate(trueRotation, res.getWidth() * xPercent, res.getHeight() * yPercent);
            }

            if (canDraw) {
                canvas.drawBitmap(res, matrix, paint);
            }
        }
    }

    public void startBsrAnimation(OnAnmEndListener endListener, final float alphaTrigger) {
        BSREvaluator bsrEvaluator = new BSREvaluator();
        if (alphaTrigger != -1) {
            if (backListener == null) {
                backListener = new BSREvaluator.OnValueBackListener() {
                    @Override
                    public void onValueBack(float value) {
                        if (value > alphaTrigger)
                            if (paint != null)
                                paint.setAlpha((int) (255 - 255 * (1 - ((1 / (1 - alphaTrigger)) * (1 - value)))));
                    }
                };
            }
            bsrEvaluator.setBackListener(backListener);
        }
        endListeners.add(endListener);
        final ValueAnimator anim = ValueAnimator.ofObject(bsrEvaluator, this);
        anim.setInterpolator((interpolator == null ? new AccelerateDecelerateInterpolator() : interpolator));
        anim.setDuration(during);

        anim.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        for (OnAnmEndListener endListener : endListeners) {
                            endListener.onAnimationEnd(BSRPathPoint.this);
                        }
                    }
                }
        );

        ValueAnimator tempAnm = ValueAnimator.ofFloat(1);
        tempAnm.setDuration((long) delayTime);
        if (delayTime == 0) {
            anim.start();
            canDraw = true;
        } else {
            tempAnm.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    anim.start();
                    canDraw = true;
                }
            });
            tempAnm.start();
        }
    }

    public Matrix getMatrix() {
        return matrix;
    }

    boolean canDraw = false;

}
