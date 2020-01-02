package com.hal9000.tourmania.ui.join_tour;

/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Originally written in Kotlin. Ported to Java 1:1.
 */

import android.content.Context;
import android.graphics.Matrix;
import android.hardware.display.DisplayManager;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import java.lang.IllegalArgumentException;
import java.lang.ref.WeakReference;

/**
 * Builder for [Preview] that takes in a [WeakReference] of the view finder and [PreviewConfig],
 * then instantiates a [Preview] which automatically resizes and rotates reacting to config changes.
 */
class AutoFitPreviewBuilder {

    private final static String TAG = AutoFitPreviewBuilder.class.getSimpleName();

    /** Public instance of preview use-case which can be used by consumers of this adapter */
    Preview useCase;

    /** Internal variable used to keep track of the use case's output rotation */
    private Integer bufferRotation = 0;

    /** Internal variable used to keep track of the view's rotation */
    private Integer viewFinderRotation = null;

    /** Internal variable used to keep track of the use-case's output dimension */
    private Size bufferDimens = new Size(0, 0);

    /** Internal variable used to keep track of the view's dimension */
    private Size viewFinderDimens = new Size(0, 0);

    /** Internal variable used to keep track of the view's display */
    private Integer viewFinderDisplay = -1;

    /** Internal reference of the [DisplayManager] */
    private DisplayManager displayManager;

    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private DisplayManager.DisplayListener displayListener;

    public AutoFitPreviewBuilder(PreviewConfig config, WeakReference<TextureView> viewFinderRef) {
        displayListener = new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {

            }

            @Override
            public void onDisplayRemoved(int displayId) {

            }

            @Override
            public void onDisplayChanged(int displayId) {
                TextureView viewFinder = viewFinderRef.get();
                if (viewFinder == null)
                    return;
                if (displayId == viewFinderDisplay) {
                    Display display = displayManager.getDisplay(displayId);
                    int rotation = getDisplaySurfaceRotation(display);
                    updateTransform(viewFinder, rotation, bufferDimens, viewFinderDimens);
                }
            }
        };

        // Make sure that the view finder reference is valid
        TextureView viewFinder = viewFinderRef.get();
        if (viewFinder == null)
            throw new IllegalArgumentException("Invalid reference to view finder used");

        // Initialize the display and rotation from texture view information
        viewFinderDisplay = viewFinder.getDisplay().getDisplayId();
        viewFinderRotation = getDisplaySurfaceRotation(viewFinder.getDisplay());
        if (viewFinderRotation == null)
            viewFinderRotation = 0;

        // Initialize public use-case with the given config
        useCase = new Preview(config);

        // Every time the view finder is updated, recompute layout
        useCase.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                TextureView viewFinder = viewFinderRef.get();
                if (viewFinder == null)
                    return;

                Log.d(TAG, "Preview output changed. " +
                        "Size: ${it.textureSize}. Rotation: ${it.rotationDegrees}");

                // To update the SurfaceTexture, we have to remove it and re-add it
                ViewGroup parent = (ViewGroup)viewFinder.getParent();
                parent.removeView(viewFinder);
                parent.addView(viewFinder, 0);

                // Update internal texture
                viewFinder.setSurfaceTexture(output.getSurfaceTexture());

                // Apply relevant transformations
                bufferRotation = output.getRotationDegrees();
                Integer rotation = getDisplaySurfaceRotation(viewFinder.getDisplay());
                updateTransform(viewFinder, rotation, output.getTextureSize(), viewFinderDimens);
            }
        });

        // Every time the provided texture view changes, recompute layout
        viewFinder.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                TextureView viewFinder = (TextureView)v;
                Size newViewFinderDimens = new Size(right - left, bottom - top);
                Log.d(TAG, "View finder layout changed. Size: $newViewFinderDimens");
                Integer rotation = getDisplaySurfaceRotation(viewFinder.getDisplay());
                updateTransform(viewFinder, rotation, bufferDimens, newViewFinderDimens);
            }
        });

        // Every time the orientation of device changes, recompute layout
        // NOTE: This is unnecessary if we listen to display orientation changes in the camera
        //  fragment and call [Preview.setTargetRotation()] (like we do in this sample), which will
        //  trigger [Preview.OnPreviewOutputUpdateListener] with a new
        //  [PreviewOutput.rotationDegrees]. CameraX Preview use case will not rotate the frames for
        //  us, it will just tell us about the buffer rotation with respect to sensor orientation.
        //  In this sample, we ignore the buffer rotation and instead look at the view finder's
        //  rotation every time [updateTransform] is called, which gets triggered by
        //  [CameraFragment] display listener -- but the approach taken in this sample is not the
        //  only valid one.
        displayManager = (DisplayManager) viewFinder.getContext().getSystemService(Context.DISPLAY_SERVICE);
        displayManager.registerDisplayListener(displayListener, null);

        // Remove the display listeners when the view is detached to avoid holding a reference to
        //  it outside of the Fragment that owns the view.
        // NOTE: Even though using a weak reference should take care of this, we still try to avoid
        //  unnecessary calls to the listener this way.
        viewFinder.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                displayManager.registerDisplayListener(displayListener, null);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                displayManager.unregisterDisplayListener(displayListener);
            }
        });
    }

    /** Helper function that fits a camera preview into the given [TextureView] */
    private void updateTransform(TextureView textureView, Integer rotation, Size newBufferDimens, Size newViewFinderDimens) {
        Log.d("crashTest", "rotation = " + rotation + " ; newBufferDimens = " + newBufferDimens + " ; newViewFinderDimens = " + newViewFinderDimens);

        // This should not happen anyway, but now the linter knows
        if (textureView == null)
            return;

        if (rotation == viewFinderRotation &&
                newBufferDimens == bufferDimens &&
                newViewFinderDimens == viewFinderDimens) {
            // Nothing has changed, no need to transform output again
            return;
        }

        if (rotation == null) {
            // Invalid rotation - wait for valid inputs before setting matrix
            return;
        } else {
            // Update internal field with new inputs
            viewFinderRotation = rotation;
        }

        if (newBufferDimens.getWidth() == 0 || newBufferDimens.getHeight() == 0) {
            // Invalid buffer dimens - wait for valid inputs before setting matrix
            return;
        } else {
            // Update internal field with new inputs
            bufferDimens = newBufferDimens;
        }

        if (newViewFinderDimens.getWidth() == 0 || newViewFinderDimens.getHeight() == 0) {
            // Invalid view finder dimens - wait for valid inputs before setting matrix
            return;
        } else {
            // Update internal field with new inputs
            viewFinderDimens = newViewFinderDimens;
        }

        Matrix matrix = new Matrix();
        Log.d("crashTest", "Applying output transformation.\n" +
                "View finder size: $viewFinderDimens.\n" +
                "Preview output size: $bufferDimens\n" +
                "View finder rotation: $viewFinderRotation\n" +
                "Preview output rotation: $bufferRotation");

        // Compute the center of the view finder
        float centerX = viewFinderDimens.getWidth() / 2f;
        float centerY = viewFinderDimens.getHeight() / 2f;

        // Correct preview output to account for display rotation
        matrix.postRotate(-((float)viewFinderRotation), centerX, centerY);

        // Buffers are rotated relative to the device's 'natural' orientation.
        boolean isNaturalPortrait = ((viewFinderRotation == 0 || viewFinderRotation == 180) &&
                viewFinderDimens.getWidth() < viewFinderDimens.getHeight())
                || ((viewFinderRotation == 90 || viewFinderRotation == 270) &&
                viewFinderDimens.getWidth() >= viewFinderDimens.getHeight());
        int bufferWidth;
        int bufferHeight;
        if (isNaturalPortrait) {
            bufferWidth = bufferDimens.getHeight();
            bufferHeight = bufferDimens.getWidth();
        } else {
            bufferWidth = bufferDimens.getWidth();
            bufferHeight = bufferDimens.getHeight();
        }
        // Scale back the buffers back to the original output buffer dimensions.
        float xScale = bufferWidth / ((float)viewFinderDimens.getWidth());
        float yScale = bufferHeight / ((float)viewFinderDimens.getHeight());

        int bufferRotatedWidth;
        int bufferRotatedHeight;
        if (viewFinderRotation == 0 || viewFinderRotation == 180) {
            bufferRotatedWidth = bufferWidth;
            bufferRotatedHeight = bufferHeight;
        } else {
            bufferRotatedWidth = bufferHeight;
            bufferRotatedHeight = bufferWidth;
        }

        // Scale the buffer so that it just covers the viewfinder.
        float scale = Math.max(viewFinderDimens.getWidth() / (float)bufferRotatedWidth,
                viewFinderDimens.getHeight() / (float)bufferRotatedHeight);
        xScale *= scale;
        yScale *= scale;

        // Scale input buffers to fill the view finder
        matrix.preScale(xScale, yScale, centerX, centerY);

        // Finally, apply transformations to our TextureView
        textureView.setTransform(matrix);
    }

    /** Helper function that gets the rotation of a [Display] in degrees */
    private Integer getDisplaySurfaceRotation(Display display) {
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                return null;
        }
    }

    /**
     * Main entrypoint for users of this class: instantiates the adapter and returns an instance
     * of [Preview] which automatically adjusts in size and rotation to compensate for
     * config changes.
     */
    public static Preview build(PreviewConfig config, TextureView viewFinder) {
        return new AutoFitPreviewBuilder(config, new WeakReference<TextureView>(viewFinder)).useCase;
    }
}