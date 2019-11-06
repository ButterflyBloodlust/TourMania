package com.hal9000.tourmania.ui.tour_waypoints_list;

public interface ItemTouchHelperAdapter {
    void onItemMove(int fromPosition, int toPosition);
    void onItemDismiss(int position);
}