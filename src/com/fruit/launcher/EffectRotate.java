package com.fruit.launcher;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;

public class EffectRotate extends EffectBase {

	public EffectRotate(int id, int type, String title) {
		super(id, type, title);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean getCellLayoutChildStaticTransformation(ViewGroup parent,
			View view, Transformation transformation, Camera camera,
			float ratio, int currentScreen, float indicatorOffset,
			boolean isPortrait) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getWorkspaceChildStaticTransformation(ViewGroup parent,
			View view, Transformation transformation, Camera camera,
			float ratio, int currentScreen, float indicatorOffset,
			boolean isPortrait) {
		// TODO Auto-generated method stub
		float width = 0.0f;
		float height = 0.0f;
		Matrix matrix = transformation.getMatrix();

		if (isPortrait) {
			width = view.getMeasuredWidth();
			height = view.getMeasuredHeight() - indicatorOffset;
		} else {
			width = view.getMeasuredWidth() - indicatorOffset;
			height = view.getMeasuredHeight();
		}

		if (ratio != 0.0f) {
			transformation.setAlpha(1.0f - Math.abs(ratio));

			camera.save();
			if (isPortrait) {
				camera.translate(width * ratio, 0.0f, 0.0f);
				camera.rotateY(90.0f * ratio);
			} else {
				camera.translate(0.0f, -height * ratio, 0.0f);
				camera.rotateX(-90.0f * ratio);
			}
			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate(-width / 2.0f, -height / 2.0f);
			matrix.postTranslate(width / 2.0f, height / 2.0f);

			transformation.setTransformationType(Transformation.TYPE_BOTH);
			return true;
		}
		return false;
	}
}