package com.fruit.launcher;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;

public class EffectFade extends EffectBase {

	public EffectFade(int id, int type, String title) {
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
		transformation.setAlpha(1.0f - Math.abs(ratio));
		matrix.preScale(1.0f + ratio, 1.0f + ratio);
		matrix.preTranslate(-width / 2.0f, -height / 2.0f);

		if (isPortrait) {
			matrix.postTranslate((0.5f + ratio) * width, height / 2.0f);
		} else {
			matrix.postTranslate(width / 2.0f, (0.5f + ratio) * height);
		}

		transformation.setTransformationType(Transformation.TYPE_BOTH);
		return true;
	}
}