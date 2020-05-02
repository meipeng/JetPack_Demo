package com.example.camerax;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

/**
 * @author meipeng
 * @date 2020/5/2
 **/
public class PreviewView extends TextureView {
    private String tag = "PreviewView";
    private int ratioWidth = 0;
    private int ratioHeight = 0;

    public PreviewView(Context context) {
        super(context);
        init();
    }

    public PreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setSurfaceTextureListener(new SurfaceCallback());
    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("设置错误");
        }
        ratioWidth = width;
        ratioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == ratioWidth || 0 == ratioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * ratioWidth / ratioHeight) {
                setMeasuredDimension(width, width * ratioHeight / ratioWidth);
            } else {
                setMeasuredDimension(height * ratioWidth / ratioHeight, height);
            }
        }
    }

    /**
     * 设置录制预览
     *
     * @param previewSize 预览大小
     */
    public void setRecordPreview(Size previewSize) {
        if (null == previewSize) {
            return;
        }
        Context context = getContext();
        if (!(context instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) context;
        //
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        //activity 的旋转角度
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        setTransform(matrix);
    }

    /**
     * 设置播放视频时 不合尺寸的视频，区中裁剪，铺满TextureView播放
     *
     * @param videoWidth  视频宽
     * @param videoHeight 视频高
     */
    public void setPayCenterCropFill(int videoWidth, int videoHeight) {
        float sx = (float) getWidth() / (float) videoWidth;
        float sy = (float) getHeight() / (float) videoHeight;
        //
        Matrix matrix = new Matrix();
        float maxScale = Math.max(sx, sy);
        //第1步:把视频区移动到View区,使两者中心点重合.
        matrix.preTranslate((getWidth() - videoWidth) / 2, (getHeight() - videoHeight) / 2);
        //第2步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        matrix.preScale(videoWidth / (float) getWidth(), videoHeight / (float) getHeight());
        //第3步,等比例放大或缩小,直到视频区的一边超过View一边, 另一边与View的另一边相等. 因为超过的部分超出了View的范围,所以是不会显示的,相当于裁剪了.
        matrix.postScale(maxScale, maxScale, getWidth() / 2, getHeight() / 2);//后两个参数坐标是以整个View的坐标系以参考的
        setTransform(matrix);
        postInvalidate();

    }

    /**
     * 设置播放视频时 不合尺寸的视频，设置在TextureView中心播放
     *
     * @param videoWidth  视频宽
     * @param videoHeight 视频高
     */
    public void setPayTextureViewCenter(int videoWidth, int videoHeight) {
        float sx = (float) getWidth() / (float) videoWidth;
        float sy = (float) getHeight() / (float) videoHeight;
        Matrix matrix = new Matrix();
        //第1步:把视频区移动到View区,使两者中心点重合.
        matrix.preTranslate((getWidth() - videoWidth) / 2, (getHeight() - videoHeight) / 2);
        //第2步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        matrix.preScale(videoWidth / (float) getWidth(), videoHeight / (float) getHeight());
        //第3步,等比例放大或缩小,直到视频区的一边和View一边相等.如果另一边和view的一边不相等，则留下空隙
        if (sx >= sy) {
            matrix.postScale(sy, sy, getWidth() / 2, getHeight() / 2);
        } else {
            matrix.postScale(sx, sx, getWidth() / 2, getHeight() / 2);
        }
        setTransform(matrix);
        postInvalidate();
    }

    private boolean isOpne;
    private boolean isTextureInit;
    private boolean isForOpne;

    public void onInitCamera() {
        if (!isTextureInit) {
            isForOpne = true;
            return;
        }
        if (isOpne) {
            return;
        }
        isOpne = true;
//        CameraGatherManager.getInstance().startBackgroundThread();
//        CameraGatherManager.getInstance().setTextureView(this);
//        CameraGatherManager.getInstance().initCamera(getContext());
    }

    class SurfaceCallback implements SurfaceTextureListener {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            isTextureInit = true;
            if (isForOpne) {
                isForOpne = false;
                onInitCamera();
            }
            //当SurefaceTexture可用的时候，设置相机参数并打开相机
            //onInitCamera();
           // DLog.e(tag, "预览可用 onSurfaceTextureAvailable width：" + width + " height:" + height);

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
          //  DLog.e(tag, "预览大小发生改变 onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
           // DLog.e(tag, "预览关闭 onSurfaceTextureDestroyed");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // DLog.e(tag, "预览发生更新 onSurfaceTextureDestroyed");
        }
    }
}
