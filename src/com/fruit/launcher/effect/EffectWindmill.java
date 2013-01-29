package com.fruit.launcher.effect;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;

public class EffectWindmill extends EffectBase {

	public EffectWindmill(int id, int type, String title) {
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

			angle = -90.0f * ratio;
			matrix.setRotate(angle, width / 2.0f, height);
		} else {
			width = view.getMeasuredWidth() - indicatorOffset;
			height = view.getMeasuredHeight();

			angle = 90.0f * ratio;
			matrix.setRotate(angle, width, height / 2.0f);
		}

		transformation.setTransformationType(Transformation.TYPE_MATRIX);
		return true;
	}
}