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
import com.anubhav.takeanote.interfaces.RvItemSwipeListener;

import org.jetbrains.annotations.NotNull;

public class ItemSwipeHelper extends ItemTouchHelper.SimpleCallback {

    private final RvItemSwipeListener itemSwipeListener;
    private final Drawable deleteIcon, favoriteIcon;
    private GradientDrawable background;
    private static final int LIMIT_SWIPE_LENGTH = 8;

    int colorRed, colorBlue;
    int iconOffsetX = 38;
    int backgroundCornerRadius = 42;
    int backgroundCornerOffset = 42;

    public ItemSwipeHelper(RvItemSwipeListener itemSwipeListener, Context context) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.itemSwipeListener = itemSwipeListener;

        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_round_delete_outline_24);
        favoriteIcon = ContextCompat.getDrawable(context, R.drawable.ic_round_favorite_border_24);
        deleteIcon.setTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
        favoriteIcon.setTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));

        colorRed = ContextCompat.getColor(context, R.color.red_500);
        colorBlue = ContextCompat.getColor(context, R.color.colorPrimary);
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
        if (itemSwipeListener != null) {
            if (direction == ItemTouchHelper.LEFT) {
                itemSwipeListener.onItemDelete(position);
            } else if (direction == ItemTouchHelper.RIGHT) {
                itemSwipeListener.onItemFavorite(position);
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

        if (dX < 0) {
            //left swipe
            setLeftSwipe(c, itemView, dX);

        } else if (dX > 0) {
            //right swipe
            setRightSwipe(c, itemView, dX);

        } else {
            //unSwiped
            background = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{colorBlue, colorBlue});
            background.setBounds(0, 0, 0, 0);
            background.draw(c);
        }
    }

    private void setLeftSwipe(Canvas canvas, View itemView, float dX) {
        int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();

        int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
        int iconRight = itemView.getRight() - iconMargin;
        deleteIcon.setBounds(iconLeft + iconOffsetX, iconTop, iconRight + iconOffsetX, iconBottom);

        background = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{colorRed, colorRed});

        background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                itemView.getTop(), itemView.getRight(), itemView.getBottom());

        background.setCornerRadii(new float[]{0f, 0f, backgroundCornerRadius, backgroundCornerRadius, backgroundCornerRadius, backgroundCornerRadius, 0f, 0f});

        background.draw(canvas);
        deleteIcon.draw(canvas);
    }

    private void setRightSwipe(Canvas canvas, View itemView, float dX) {
        int iconMargin = (itemView.getHeight() - favoriteIcon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - favoriteIcon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + favoriteIcon.getIntrinsicHeight();

        int iconLeft = itemView.getLeft() + iconMargin;
        int iconRight = itemView.getLeft() + iconMargin + favoriteIcon.getIntrinsicWidth();
        favoriteIcon.setBounds(iconLeft - iconOffsetX, iconTop, iconRight - iconOffsetX, iconBottom);

        background = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{colorBlue, colorBlue});

        background.setBounds(itemView.getLeft(), itemView.getTop(),
                itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getBottom());

        background.setCornerRadii(new float[]{backgroundCornerRadius, backgroundCornerRadius, 0f, 0f, 0f, 0f, backgroundCornerRadius, backgroundCornerRadius});

        background.draw(canvas);
        favoriteIcon.draw(canvas);
    }

}