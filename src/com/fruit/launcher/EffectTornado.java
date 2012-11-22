package com.fruit.launcher;


import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;

public class EffectTornado extends EffectBase {

	public EffectTornado(int id, int type, String title) {
		super(id, type, title);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean getCellLayoutChildStaticTransformation(ViewGroup parent,
			View view, Transformation transformation, Camera camera,
			float ratio, int currentScreen, float indicatorOffset,
			boolean isPortrait) {
		// TODO Auto-generated method stub
		int childCount = parent.getChildCount();
		// if current cell layout has no items, just exit without any transformation
		if (childCount == 0) {
			return false;
		}

		float cellWidth = view.getMeasuredWidth();
		float cellHeight = view.getMeasuredHeight();
		float measuredWidth = 0.0f;
		float measuredHeight = 0.0f;
		float scale = 0.0f;
		Matrix matrix = transformation.getMatrix();
		ItemInfo info = (ItemInfo) view.getTag();

		if (info == null) {
			return false;
		}

		if (isPortrait) {
			measuredWidth = parent.getMeasuredWidth();
			measuredHeight = parent.getMeasuredHeight() - indicatorOffset;
		} else {
			measuredWidth = parent.getMeasuredWidth() - indicatorOffset;
			measuredHeight = parent.getMeasuredHeight();
		}

		int cellX = info.cellX;
		int cellY = info.cellY;
		int otherCellX = 0;
		int otherCellY = 0;
		int cellIndex = 0;

		// find current view(cell)'s display index in CellLayout
		for (int i = 0; i < childCount; i++) {
			ItemInfo otherInfo = (ItemInfo) parent.getChildAt(i).getTag();
			if (otherInfo != null) {
				otherCellX = otherInfo.cellX;
				otherCellY = otherInfo.cellY;
				if (otherCellY > cellY ||
						(otherCellY == cellY && otherCellX >= cellX)) {
					continue;
				}
			}
			cellIndex++;
		}

		float angle = 360.0f / childCount * cellIndex;
		float radius = 0.0f;
		float distanceDegrees = 0.0f;
		float distanceWidth = 0.0f;
		float distanceHeight = 0.0f;

		if (isPortrait) {
			radius = measuredWidth / 4.0f * 6.0f / 4.0f;
		} else {
			radius = measuredHeight / 4.0f * 6.0f / 4.0f;
		}

		// Calculate the angles that the cell will be rotated 
		distanceDegrees = angle + 90.0f;

		if (ratio >= 0.5f) {
			angle += 90.0f;
			distanceDegrees += 90.0f;
			if (angle >= 360.0f) {
				angle -= 360.0f;
			}
		}

		if (info.spanX > 1 || info.spanY > 1) {
			scale = 1.0f / Math.max(info.spanX, info.spanY);
		}

		// Calculate the distances that the cell will be moved 
		distanceWidth =
			parent.getLeft() + measuredWidth / 2.0f - view.getLeft() - cellWidth / 2.0f;
		distanceHeight =
			parent.getTop() + measuredHeight / 2.0f - view.getTop() - cellHeight / 2.0f;
		if (angle > 180.0f) {
			distanceWidth += (float) (radius * Math.cos((360.0f - angle) * Math.PI / 180.0d));
			distanceHeight -= (float) (radius * Math.sin((angle - 360.0f) * Math.PI / 180.0d));
		} else {
			distanceWidth += (float) (radius * Math.cos(angle * Math.PI / 180.0d));
			distanceHeight -= (float) (radius * Math.sin(angle * Math.PI / 180.0d));
		}

		if (isPortrait) {
			distanceWidth -= info.screen * measuredWidth;
		} else {
			distanceHeight -= info.screen * measuredHeight;
		}

		// Translate
		if (Math.abs(ratio) < 0.5f) {
			if (scale != 0.0f) {
				if (Math.abs(ratio) <= 0.25f) {
					scale = (scale - 1.0f) * Math.abs(ratio) * 4.0f + 1.0f;
				}
				matrix.preScale(scale, scale);
				matrix.preTranslate(-cellWidth / 2.0f, -cellHeight / 2.0f);
				matrix.postTranslate(cellWidth / 2.0f, cellHeight / 2.0f);
			}
			float degrees = -Math.abs(ratio) * 2.0f * distanceDegrees;
			matrix.postRotate(degrees, cellWidth / 2.0f, cellHeight / 2.0f);
			matrix.postTranslate(Math.abs(ratio) * 2.0f * distanceWidth,
					Math.abs(ratio) * 2.0f * distanceHeight);
		} else {
			if (scale != 0.0f) {
				matrix.preScale(scale, scale);
				matrix.preTranslate(-cellWidth / 2.0f, -cellHeight / 2.0f);
				matrix.postTranslate(cellWidth / 2.0f, cellHeight / 2.0f);
			}
			matrix.postRotate(-distanceDegrees, cellWidth / 2.0f, cellHeight / 2.0f);
			matrix.postTranslate(distanceWidth, distanceHeight);
		}

		transformation.setTransformationType(Transformation.TYPE_MATRIX);
		return true;
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

		if (ratio <= -0.5f) {
			angle = (0.5f + ratio) * 180.0f;
		} else if (ratio >= 0.5f) {
			angle = (1.0f - ratio) * 180.0f;
		}
		matrix.postRotate(angle, width / 2.0f, height / 2.0f);

		transformation.setTransformationType(Transformation.TYPE_MATRIX);
		return true;
	}
}