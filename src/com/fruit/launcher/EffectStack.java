package com.fruit.launcher;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;

public class EffectStack extends EffectBase {

	public EffectStack(int id, int type, String title) {
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
		float newRadio = 0.0f;
		float scale = 0.0f;
		Matrix matrix = transformation.getMatrix();

		if (isPortrait) {
			width = view.getWidth();
			height = view.getHeight() - indicatorOffset;
		} else {
			width = view.getMeasuredWidth() - indicatorOffset;
			height = view.getMeasuredHeight();
		}

		// Only the left screen will be transformed
		if (ratio > 0.0f) {
			newRadio = 1.0f - ratio;
			transformation.setAlpha(newRadio);
			scale = (0.4f * newRadio) + 0.6f;
			matrix.setScale(scale, scale);

			if (isPortrait) {
				matrix.postTranslate((1.0f - scale) * width * 3.0f,
						(1.0f - scale) * height * 0.5f);
			} else {
				matrix.postTranslate((1.0f - scale) * width * 0.5f,
						(1.0f - scale) * height * 3.0f);
			}

			transformation.setTransformationType(Transformation.TYPE_BOTH);
			return true;
		}
		return false;
	}
}