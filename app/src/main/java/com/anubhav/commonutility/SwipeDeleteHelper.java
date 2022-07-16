package com.anubhav.commonutility;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.anubhav.takeanote.R;
import com.anubhav.takeanote.interfaces.RvItemDeleteListener;

import org.jetbrains.annotations.NotNull;

public class SwipeDeleteHelper extends ItemTouchHelper.SimpleCallback {

    private final RvItemDeleteListener itemDeleteListener;
    private final Drawable deleteIcon;
    private final GradientDrawable background;
    private static final int LIMIT_SWIPE_LENGTH = 8;

    public SwipeDeleteHelper(RvItemDeleteListener itemDeleteListener, Context context) {
        super(0, ItemTouchHelper.LEFT);
        this.itemDeleteListener = itemDeleteListener;
        int color = ContextCompat.getColor(context, R.color.colorPrimary);
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_round_delete_outline_24);
        deleteIcon.setTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
        background = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{color, color});
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.LEFT) {
            if (itemDeleteListener != null) {
                itemDeleteListener.onItemDelete(position);
            }
        }
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.5f;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return 1f;
    }

    @Override
    public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        dX = dX / LIMIT_SWIPE_LENGTH;

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int iconOffsetX = 50;
        int backgroundCornerRadius = 42;
        int backgroundCornerOffset = 42;

        int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();

        if (dX < 0) { //left swipe
            int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            deleteIcon.setBounds(iconLeft + iconOffsetX, iconTop, iconRight + iconOffsetX, iconBottom);

            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else { //unswiped
            background.setBounds(0, 0, 0, 0);
        }

        //background.setCornerRadius(backgroundCornerRadius);
        background.setCornerRadii(new float[]{0f, 0f, backgroundCornerRadius, backgroundCornerRadius, backgroundCornerRadius, backgroundCornerRadius, 0f, 0f});

        background.draw(c);
        deleteIcon.draw(c);
    }

}