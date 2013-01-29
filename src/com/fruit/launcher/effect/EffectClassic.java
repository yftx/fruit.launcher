package com.fruit.launcher.effect;

import android.graphics.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;

final class EffectClassic extends EffectBase {

	public EffectClassic(int id, int type, String title) {
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
		return false;
	}
}