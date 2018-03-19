package com.xinrui.smart.view_custom;

public interface DragGridBaseAdapter {
	/**
	 *
	 * @param oldPosition
	 * @param newPosition
	 */
	public void reorderItems(int oldPosition, int newPosition);
	
	
	/**
	 *
	 * @param hidePosition
	 */
	public void setHideItem(int hidePosition);
	

}
