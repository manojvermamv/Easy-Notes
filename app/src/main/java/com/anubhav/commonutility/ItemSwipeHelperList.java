package com.anubhav.commonutility;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.anubhav.easynotes.R;
import com.anubhav.easynotes.database.model.Note;
import com.anubhav.easynotes.interfaces.RvItemSwipeListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemSwipeHelperList extends ItemTouchHelper.SimpleCallback {

    private final Context context;
    private GradientDrawable background;

    @DrawableRes
    int rightDrawableRes = R.drawable.ic_round_favorite_border_24;
    int rightDrawableEnabledRes = R.drawable.ic_round_favorite_24;
    int leftDrawableRes = R.drawable.ic_round_delete_outline_24;
    @ColorRes
    int rightColorRes = R.color.colorPrimary;
    int leftColorRes = R.color.red_500;

    int swipeDir = 0;
    boolean swipeOutEnabled = true;

    int LIMIT_SWIPE_LENGTH = 8;
    int backgroundCornerOffset = 40;
    float backgroundCornerRadius = 42f;
    private List<Note> list;
    private final RvItemSwipeListener onSwipeListener;

    public ItemSwipeHelperList(Context context, List<Note> list, RvItemSwipeListener onSwipeListener) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.context = context;
        this.list = list;
        this.onSwipeListener = onSwipeListener;
    }

    public ItemSwipeHelperList(Context context, List<Note> list, RvItemSwipeListener onSwipeListener, float cornerRadius) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.context = context;
        this.list = list;
        this.onSwipeListener = onSwipeListener;
        this.backgroundCornerRadius = cornerRadius;
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
        if (onSwipeListener != null) {
            int position = viewHolder.getAdapterPosition();
            if (direction == ItemTouchHelper.LEFT) {
                onSwipeListener.onItemLeftSwipe(viewHolder, position);
            } else if (direction == ItemTouchHelper.RIGHT) {
                onSwipeListener.onItemRightSwipe(viewHolder, position);
            }
        }
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return swipeOutEnabled ? 0.5f : 1.0f;
    }

    @Override
    public float getSwipeVelocityThreshold(float defaultValue) {
        return swipeOutEnabled ? defaultValue : 1f;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return swipeOutEnabled ? defaultValue : Float.MAX_VALUE;
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (swipeDir != 0) {
            onSwiped(viewHolder, swipeDir);
            swipeDir = 0;
        }
    }

    @Override
    public void onChildDraw(@NotNull Canvas c, @NotNull RecyclerView recyclerView, @NotNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        //set true if it should swipe out
        boolean shouldSwipeOut = false;
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && (!shouldSwipeOut)) {
            swipeOutEnabled = false;

            //Limit swipe
            int maxMovement = recyclerView.getWidth() / LIMIT_SWIPE_LENGTH;

            //swipe right : left
            float sign = dX > 0 ? 1 : -1;

            float limitMovement = Math.min(maxMovement, sign * dX); // Only move to maxMovement

            float displacementPercentage = limitMovement / maxMovement;

            //limited threshold
            boolean swipeThreshold = displacementPercentage == 1;

            // Move slower when getting near the middle
            dX = sign * maxMovement * (float) Math.sin((Math.PI / 2) * displacementPercentage);

            if (isCurrentlyActive) {
                int dir = dX > 0 ? ItemTouchHelper.RIGHT : ItemTouchHelper.LEFT;
                swipeDir = swipeThreshold ? dir : 0;
            }
        } else {
            swipeOutEnabled = true;
        }

        //do decoration
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        // Getting the swiped itemView
        View itemView = viewHolder.itemView;

        // Getting the swiped item
        boolean isFavorite = false;
        if (list.size() > viewHolder.getAdapterPosition()) {
            Note note = list.get(viewHolder.getAdapterPosition());
            isFavorite = note.isFavorite();
        }

        if (dX < 0) {
            //left swipe
            Drawable leftIcon = ContextCompat.getDrawable(context, leftDrawableRes);
            leftIcon.setTintList(ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.white)));
            setLeftSwipe(c, itemView, dX, leftIcon);

        } else if (dX > 0) {
            //right swipe
            Drawable rightIcon = ContextCompat.getDrawable(context, isFavorite ? rightDrawableEnabledRes : rightDrawableRes);
            rightIcon.setTintList(ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.white)));
            setRightSwipe(c, itemView, dX, rightIcon);

        } else {
            //unSwiped
            int transparentColor = ContextCompat.getColor(context, android.R.color.transparent);
            background = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{transparentColor, transparentColor});
            background.setBounds(0, 0, 0, 0);
            background.draw(c);
        }
    }

    private void setLeftSwipe(Canvas canvas, View itemView, float dX, Drawable leftIcon) {
        int iconMargin = ((-(int) dX) - leftIcon.getIntrinsicWidth()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - leftIcon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + leftIcon.getIntrinsicHeight();

        int leftBoundIcon = itemView.getRight() - iconMargin - leftIcon.getIntrinsicWidth();
        int rightBoundIcon = itemView.getRight() - iconMargin;
        leftIcon.setBounds(leftBoundIcon, iconTop, rightBoundIcon, iconBottom);

        background = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{getColor(leftColorRes), getColor(leftColorRes)});

        background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                itemView.getTop(), itemView.getRight(), itemView.getBottom());

        background.setCornerRadii(new float[]{0f, 0f, backgroundCornerRadius, backgroundCornerRadius, backgroundCornerRadius, backgroundCornerRadius, 0f, 0f});

        background.draw(canvas);
        leftIcon.draw(canvas);
    }

    private void setRightSwipe(Canvas canvas, View itemView, float dX, Drawable rightIcon) {
        int iconMargin = (((int) dX) - rightIcon.getIntrinsicWidth()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - rightIcon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + rightIcon.getIntrinsicHeight();

        int leftBoundIcon = itemView.getLeft() + iconMargin;
        int rightBoundIcon = itemView.getLeft() + iconMargin + rightIcon.getIntrinsicWidth();
        rightIcon.setBounds(leftBoundIcon, iconTop, rightBoundIcon, iconBottom);

        background = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{getColor(rightColorRes), getColor(rightColorRes)});

        background.setBounds(itemView.getLeft(), itemView.getTop(),
                itemView.getRight() + ((int) dX) - backgroundCornerOffset, itemView.getBottom());

        background.setCornerRadii(new float[]{backgroundCornerRadius, backgroundCornerRadius, 0f, 0f, 0f, 0f, backgroundCornerRadius, backgroundCornerRadius});

        background.draw(canvas);
        rightIcon.draw(canvas);
    }

    @ColorInt
    private int getColor(@ColorRes int color) {
        return ContextCompat.getColor(context, color);
    }

}