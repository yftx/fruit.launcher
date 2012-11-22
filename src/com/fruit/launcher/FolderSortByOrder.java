package com.fruit.launcher;

import java.util.Comparator;

public class FolderSortByOrder implements Comparator<ItemInfo> {

	@Override
	public int compare(ItemInfo appInfoA, ItemInfo appInfoB) {
		// TODO Auto-generated method stub
		return (appInfoA.orderId - appInfoB.orderId);
	}
}