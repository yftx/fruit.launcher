package com.fruit.launcher;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;

public class EffectCube extends EffectBase {

	public EffectCube(int id, int type, String title) {
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
		float angle = 0.0f;
		Matrix matrix = transformation.getMatrix();

		if (isPortrait) {
			width = view.getMeasuredWidth();
			height = view.getMeasuredHeight() - indicatorOffset;
		} else {
			width = view.getMeasuredWidth() - indicatorOffset;
			height = view.getMeasuredHeight();
		}

		camera.save();
		if (isPortrait) {
			angle = ratio * -90.0f;
			camera.rotateY(angle);
		} else {
			angle = ratio * 90.0f;
			camera.rotateX(angle);
		}
		camera.getMatrix(matrix);
		camera.restore();

		if (angle >= 0.0f) {
			if (isPortrait) {
				matrix.preTranslate(0.0f, -height / 2.0f);
				matrix.postTranslate(0.0f, height / 2.0f);
			} else {
				matrix.preTranslate(-width / 2.0f, 0.0f);
				matrix.postTranslate(width / 2.0f, 0.0f);
			}
		} else {
			if (isPortrait) {
				matrix.preTranslate(-width, -height / 2.0f);
				matrix.postTranslate(width, height / 2.0f);
			} else {
				matrix.preTranslate(-width / 2.0f, -height);
				matrix.postTranslate(width / 2.0f, height);
			}
		}

		transformation.setTransformationType(Transformation.TYPE_MATRIX);
		return true;
	}
}