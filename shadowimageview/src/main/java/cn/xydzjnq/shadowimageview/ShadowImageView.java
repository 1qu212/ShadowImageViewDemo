package cn.xydzjnq.shadowimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

public class ShadowImageView extends ImageView {
    private static final float DEFAULT_RADIUS = 7.0f;
    private static final float BRIGHTNESS = -25f;
    private static final float DEFAULT_SATURATION = 1.0f;
    private static final float HEIGHT_OFFSET = 2f;
    private static final float DEFAULT_SHADOW_WIDTH = 6.0f;
    private Context context;
    private float radiusOffset = DEFAULT_RADIUS;
    private float shadowWidth;
    private float saturation;
    private float shadowSaturation;

    public ShadowImageView(Context context) {
        this(context, null);
    }

    public ShadowImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadowImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        BlurShadow.getInstance().init(context);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShadowView, 0, 0);
        setRadiusOffset(typedArray.getFloat(R.styleable.ShadowView_radiusOffset, DEFAULT_RADIUS));
        int defaultShadowWidth = dp2px(DEFAULT_SHADOW_WIDTH);
        shadowWidth = typedArray.getDimension(R.styleable.ShadowView_shadowWidth, defaultShadowWidth);
        saturation = typedArray.getFloat(R.styleable.ShadowView_saturation, DEFAULT_SATURATION);
        shadowSaturation = typedArray.getFloat(R.styleable.ShadowView_shadowSaturation, DEFAULT_SATURATION);
        typedArray.recycle();
        setCropToPadding(true);
        setPadding((int) shadowWidth, (int) shadowWidth, (int) shadowWidth, (int) shadowWidth);
    }

    public void setRadiusOffset(float newValue) {
        if (newValue > 0 && newValue <= 25) {
            radiusOffset = newValue;
        } else if (newValue > 25) {
            radiusOffset = 25;
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (getHeight() != 0 && getMeasuredHeight() != 0) {
            super.setImageBitmap(bm);
            makeBlurShadow();
        } else {
            super.setImageBitmap(bm);
            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    makeBlurShadow();
                    return false;
                }
            });
        }
    }

    @Override
    public void setImageResource(int resId) {
        if (getHeight() != 0 && getMeasuredHeight() != 0) {
            super.setImageResource(resId);
            makeBlurShadow();
        } else {
            super.setImageResource(resId);
            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    makeBlurShadow();
                    return false;
                }
            });
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (getHeight() != 0 && getMeasuredHeight() != 0) {
            super.setImageDrawable(drawable);
            makeBlurShadow();
        } else {
            super.setImageDrawable(drawable);
            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    makeBlurShadow();
                    return false;
                }
            });
        }
    }

    public void setImageResource(int resId, boolean withShadow) {
        if (withShadow) {
            setImageResource(resId);
        } else {
            super.setImageResource(resId);
        }
    }

    public void setImageDrawable(Drawable drawable, boolean withShadow) {
        if (withShadow) {
            setImageDrawable(drawable);
        } else {
            super.setImageDrawable(drawable);
        }
    }

    private void makeBlurShadow() {
        Bitmap bitmap = getBitmap();
        ColorMatrix colorMatrix = new ColorMatrix(new float[]{
                1f, 0f, 0f, 0f, BRIGHTNESS,
                0f, 1f, 0f, 0f, BRIGHTNESS,
                0f, 0f, 1f, 0f, BRIGHTNESS,
                0f, 0f, 0f, 1f, 0f});
        colorMatrix.setSaturation(saturation);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
        bitmapDrawable.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        float radius = radiusOffset;
        Bitmap blur = BlurShadow.getInstance().blur(this, getWidth(), getHeight() - dp2px(HEIGHT_OFFSET), radius);
        ColorMatrix blurColorMatrix = new ColorMatrix(new float[]{
                1f, 0f, 0f, 0f, BRIGHTNESS,
                0f, 1f, 0f, 0f, BRIGHTNESS,
                0f, 0f, 1f, 0f, BRIGHTNESS,
                0f, 0f, 0f, 1f, 0f});
        blurColorMatrix.setSaturation(shadowSaturation);
        BitmapDrawable blurBitmapDrawable = new BitmapDrawable(getResources(), blur);
        blurBitmapDrawable.setColorFilter(new ColorMatrixColorFilter(blurColorMatrix));
        super.setImageDrawable(bitmapDrawable);
        setBackground(blurBitmapDrawable);
    }

    private Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    private int dp2px(final float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}