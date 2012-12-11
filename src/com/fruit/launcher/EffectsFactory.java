package com.fruit.launcher;

import java.util.ArrayList;
import java.util.Iterator;

public class EffectsFactory {

	private static ArrayList<EffectBase> sEffectsPool;

	public static ArrayList<EffectBase> getAllEffects() {
		return getEffectsList();
	}

	public static EffectBase getEffectByType(int type) {
		if (sEffectsPool == null) {
			return null;
		}

		EffectBase effect = null;
		Iterator<EffectBase> iterator = sEffectsPool.iterator();

		while (iterator.hasNext()) {
			effect = iterator.next();
			if (effect.type == type) {
				return effect;
			}
		}
		return null;
	}

	public static int getEffectsCount() {
		if (sEffectsPool == null) {
			return 0;
		}
		return sEffectsPool.size();
	}

	private static ArrayList<EffectBase> getEffectsList() {
		// TODO Auto-generated method stub
		if (sEffectsPool == null) {
			sEffectsPool = new ArrayList<EffectBase>();

			sEffectsPool.add(new EffectClassic(0, Effects.EFFECT_TYPE_CLASSIC,
					Effects.EFFECT_TITLE_CLASSIC));
			sEffectsPool.add(new EffectRotate(0, Effects.EFFECT_TYPE_ROTATE,
					Effects.EFFECT_TITLE_ROTATE));
			sEffectsPool.add(new EffectFade(0, Effects.EFFECT_TYPE_FADE,
					Effects.EFFECT_TITLE_FADE));
			sEffectsPool.add(new EffectStack(0, Effects.EFFECT_TYPE_STACK,
					Effects.EFFECT_TITLE_STACK));
			sEffectsPool
					.add(new EffectWindmill(0, Effects.EFFECT_TYPE_WINDMILL,
							Effects.EFFECT_TITLE_WINDMILL));
			sEffectsPool.add(new EffectCube(0, Effects.EFFECT_TYPE_CUBE,
					Effects.EFFECT_TITLE_CUBE));
			sEffectsPool.add(new EffectTornado(0, Effects.EFFECT_TYPE_TOR,
					Effects.EFFECT_TITLE_TOR));
		}
		return sEffectsPool;
	}
}