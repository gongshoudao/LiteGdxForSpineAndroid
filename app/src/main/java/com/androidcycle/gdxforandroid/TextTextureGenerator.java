package com.androidcycle.gdxforandroid;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Px;
import android.support.annotation.RequiresApi;
import android.text.BoringLayout;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * generate bitmap or libgdx texture by text.
 */
public class TextTextureGenerator {
    private static final String TAG = "TextureGenerator";
    private static final RectF TEMP_RECTF = new RectF();
    private final Resources mRes;
    private final TextPaint mTextPaint;
    private int[] mAutoSizeTextSizesInPx = new int[0];
    private TextPaint mTempTextPaint;
    private float mAutoSizeMinTextSizeInPx;
    private float mAutoSizeMaxTextSizeInPx;
    private float mAutoSizeStepGranularityInPx;
    private boolean mHasPresetAutoSizeValues;

    public TextTextureGenerator(Resources res) {
        mRes = res;
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        mTextPaint.setTextSize(14);//default text size 14px
    }

    public TextTextureGenerator reset() {
        mTextPaint.reset();
        return this;
    }

    public TextTextureGenerator setTextColor(@ColorInt int textColor) {
        mTextPaint.setColor(textColor);
        return this;
    }

    public TextTextureGenerator setTextSize(@Px float size) {
        mTextPaint.setTextSize(size);
        return this;
    }

    public TextTextureGenerator setTypeface(AssetManager assetManager, String fontFilePath) {
        mTextPaint.setTypeface(Typeface.createFromAsset(assetManager, fontFilePath));
        return this;
    }

    public TextTextureGenerator setTypeface(String fontFilePath) {
        mTextPaint.setTypeface(Typeface.createFromFile(fontFilePath));
        return this;
    }

    public TextTextureGenerator setTypeface(File fontFile) {
        mTextPaint.setTypeface(Typeface.createFromFile(fontFile));
        return this;
    }

    public TextTextureGenerator setAutoSizeTextTypeUniformWithConfiguration(int autoSizeMinTextSize,
                                                                            int autoSizeMaxTextSize,
                                                                            int autoSizeStepGranularity,
                                                                            int unit) throws IllegalArgumentException {
        final DisplayMetrics displayMetrics = mRes.getDisplayMetrics();
        final float autoSizeMinTextSizeInPx = TypedValue.applyDimension(unit, autoSizeMinTextSize, displayMetrics);
        final float autoSizeMaxTextSizeInPx = TypedValue.applyDimension(unit, autoSizeMaxTextSize, displayMetrics);
        final float autoSizeStepGranularityInPx = TypedValue.applyDimension(unit, autoSizeStepGranularity, displayMetrics);

        validateAndSetAutoSizeTextTypeUniformConfiguration(autoSizeMinTextSizeInPx,
                autoSizeMaxTextSizeInPx,
                autoSizeStepGranularityInPx);

        //setup auto size.
        setupAutoSizeText();
        return this;
    }

    public TextPaint getTextPaint() {
        return mTextPaint;
    }

    public Texture genTexture(Application app, CharSequence text, int maxWidth, int maxHeight) {
        Bitmap bitmap = genBitmap(text, maxWidth, maxHeight);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bitmap.recycle();
        Pixmap pixmap = new Pixmap(byteArray, 0, byteArray.length);
        return new Texture(pixmap, app);
    }

    public Bitmap genBitmap(CharSequence text, int maxWidth, int maxHeight) {
        Layout layout = makeLayout(text, maxWidth, maxHeight);
        int width = layout.getWidth();
        int height = layout.getHeight();
        if (BuildConfig.DEBUG)
            Log.i(TAG, String.format("max = [%d*%d], layout = [%d,%d]", maxWidth, maxHeight, width, height));
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        layout.draw(canvas);
        return bitmap;
    }

    private Layout makeLayout(CharSequence charSequence, int maxWidth, int maxHeight) {
        synchronized (TEMP_RECTF) {
            TEMP_RECTF.setEmpty();
            TEMP_RECTF.right = maxWidth;
            TEMP_RECTF.bottom = maxHeight;
            final float optimalTextSize = findLargestTextSizeWhichFits(charSequence, null, TEMP_RECTF);
            if (optimalTextSize != mTextPaint.getTextSize()) {
                mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, optimalTextSize, mRes.getDisplayMetrics()));
            }
        }
        Layout result = null;
        BoringLayout.Metrics boring = BoringLayout.isBoring(charSequence, mTextPaint, new BoringLayout.Metrics());
        if (boring != null) {
            if (boring.width <= maxWidth) {
                result = BoringLayout.make(charSequence, mTextPaint,
                        maxWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0f,
                        boring, true, TextUtils.TruncateAt.END,
                        maxWidth);
            }
        }
        if (result == null) {
            result = new StaticLayout(charSequence, mTextPaint, maxWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0f, true);
        }
        return result;
    }

    private void setupAutoSizeText() {
        // Calculate the sizes set based on minimum size, maximum size and step size if we do
        // not have a predefined set of sizes or if the current sizes array is empty.
        if (!mHasPresetAutoSizeValues || mAutoSizeTextSizesInPx.length == 0) {
            // Calculate sizes to choose from based on the current auto-size configuration.
            int autoSizeValuesLength = 1;
            float currentSize = Math.round(mAutoSizeMinTextSizeInPx);
            while (Math.round(currentSize + mAutoSizeStepGranularityInPx)
                    <= Math.round(mAutoSizeMaxTextSizeInPx)) {
                autoSizeValuesLength++;
                currentSize += mAutoSizeStepGranularityInPx;
            }
            int[] autoSizeTextSizesInPx = new int[autoSizeValuesLength];
            float sizeToAdd = mAutoSizeMinTextSizeInPx;
            for (int i = 0; i < autoSizeValuesLength; i++) {
                autoSizeTextSizesInPx[i] = Math.round(sizeToAdd);
                sizeToAdd += mAutoSizeStepGranularityInPx;
            }
            mAutoSizeTextSizesInPx = cleanupAutoSizePresetSizes(autoSizeTextSizesInPx);
        }
    }

    // Returns distinct sorted positive values.
    private int[] cleanupAutoSizePresetSizes(int[] presetValues) {
        final int presetValuesLength = presetValues.length;
        if (presetValuesLength == 0) {
            return presetValues;
        }
        Arrays.sort(presetValues);

        final List<Integer> uniqueValidSizes = new ArrayList<>();
        for (int i = 0; i < presetValuesLength; i++) {
            final int currentPresetValue = presetValues[i];

            if (currentPresetValue > 0
                    && Collections.binarySearch(uniqueValidSizes, currentPresetValue) < 0) {
                uniqueValidSizes.add(currentPresetValue);
            }
        }

        if (presetValuesLength == uniqueValidSizes.size()) {
            return presetValues;
        } else {
            final int uniqueValidSizesLength = uniqueValidSizes.size();
            final int[] cleanedUpSizes = new int[uniqueValidSizesLength];
            for (int i = 0; i < uniqueValidSizesLength; i++) {
                cleanedUpSizes[i] = uniqueValidSizes.get(i);
            }
            return cleanedUpSizes;
        }
    }

    /**
     * If all params are valid then save the auto-size configuration.
     *
     * @throws IllegalArgumentException if any of the params are invalid
     */
    private void validateAndSetAutoSizeTextTypeUniformConfiguration(
            float autoSizeMinTextSizeInPx,
            float autoSizeMaxTextSizeInPx,
            float autoSizeStepGranularityInPx) throws IllegalArgumentException {
        // First validate.
        if (autoSizeMinTextSizeInPx <= 0) {
            throw new IllegalArgumentException("Minimum auto-size text size ("
                    + autoSizeMinTextSizeInPx + "px) is less or equal to (0px)");
        }

        if (autoSizeMaxTextSizeInPx <= autoSizeMinTextSizeInPx) {
            throw new IllegalArgumentException("Maximum auto-size text size ("
                    + autoSizeMaxTextSizeInPx + "px) is less or equal to minimum auto-size "
                    + "text size (" + autoSizeMinTextSizeInPx + "px)");
        }

        if (autoSizeStepGranularityInPx <= 0) {
            throw new IllegalArgumentException("The auto-size step granularity ("
                    + autoSizeStepGranularityInPx + "px) is less or equal to (0px)");
        }

        // All good, persist the configuration.
        mAutoSizeMinTextSizeInPx = autoSizeMinTextSizeInPx;
        mAutoSizeMaxTextSizeInPx = autoSizeMaxTextSizeInPx;
        mAutoSizeStepGranularityInPx = autoSizeStepGranularityInPx;
        mHasPresetAutoSizeValues = false;
    }

    /**
     * Performs a binary search to find the largest text size that will still fit within the size
     * available to this view.
     */
    private int findLargestTextSizeWhichFits(CharSequence text, Layout layout, RectF availableSpace) {
        final int sizesCount = mAutoSizeTextSizesInPx.length;
        if (sizesCount == 0) {
            throw new IllegalStateException("No available text sizes to choose from.");
        }

        int bestSizeIndex = 0;
        int lowIndex = bestSizeIndex + 1;
        int highIndex = sizesCount - 1;
        int sizeToTryIndex;
        while (lowIndex <= highIndex) {
            sizeToTryIndex = (lowIndex + highIndex) / 2;
            if (suggestedSizeFitsInSpace(text, layout, mAutoSizeTextSizesInPx[sizeToTryIndex], availableSpace)) {
                bestSizeIndex = lowIndex;
                lowIndex = sizeToTryIndex + 1;
            } else {
                highIndex = sizeToTryIndex - 1;
                bestSizeIndex = highIndex;
            }
        }

        return mAutoSizeTextSizesInPx[bestSizeIndex];
    }

    private boolean suggestedSizeFitsInSpace(CharSequence text, Layout originLayout, int suggestedSizeInPx, RectF availableSpace) {
        final int maxLines = -1;
        if (mTempTextPaint == null) {
            mTempTextPaint = new TextPaint();
        } else {
            mTempTextPaint.reset();
        }
        mTempTextPaint.set(mTextPaint);
        mTempTextPaint.setTextSize(suggestedSizeInPx);

        // Needs reflection call due to being private.
        Layout.Alignment alignment = originLayout == null ? Layout.Alignment.ALIGN_CENTER : originLayout.getAlignment();
        final StaticLayout layout = Build.VERSION.SDK_INT >= 23
                ? createStaticLayoutForMeasuring(
                text, alignment, Math.round(availableSpace.right), maxLines)
                : createStaticLayoutForMeasuringPre23(
                text, alignment, Math.round(availableSpace.right));

        // Height overflow.
        return !(layout.getHeight() > availableSpace.bottom);
    }

    @RequiresApi(23)
    private StaticLayout createStaticLayoutForMeasuring(CharSequence text,
                                                        Layout.Alignment alignment, int availableWidth, int maxLines) {

        final StaticLayout.Builder layoutBuilder = StaticLayout.Builder.obtain(
                text, 0, text.length(), mTempTextPaint, availableWidth);

        return layoutBuilder.setAlignment(alignment)
                .setIncludePad(true)
                .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NORMAL)
                .setMaxLines(maxLines == -1 ? Integer.MAX_VALUE : maxLines)
                .build();
    }

    private StaticLayout createStaticLayoutForMeasuringPre23(CharSequence text,
                                                             Layout.Alignment alignment, int availableWidth) {
        // Setup defaults.
        float lineSpacingMultiplier = 1.0f;
        float lineSpacingAdd = 0.0f;

        // The layout could not be constructed using the builder so fall back to the
        // most broad constructor.
        return new StaticLayout(text, mTempTextPaint, availableWidth,
                alignment,
                lineSpacingMultiplier,
                lineSpacingAdd,
                true);
    }
}
