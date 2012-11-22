package com.fruit.launcher;

import android.graphics.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;

public abstract class EffectBase {

	public final int id;
	public final int type;
	public final String title;

	public EffectBase(int id, int type, String title) {
		this.id = id;
		this.type = type;
		this.title = title;
	}

	public abstract boolean getCellLayoutChildStaticTransformation(ViewGroup parent, View view, Transformation transformation, Camera camera, float ratio, int currentScreen, float indicatorOffset, boolean isPortrait);

	public abstract boolean getWorkspaceChildStaticTransformation(ViewGroup parent, View view, Transformation transformation, Camera camera, float ratio, int currentScreen, float indicatorOffset, boolean isPortrait);

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Effect id=").append(id);
		sb.append(", type=").append(type);
		sb.append(", title=").append(title);

		return sb.toString();
	}
}