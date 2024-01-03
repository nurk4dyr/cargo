package kz.carveo.mvvm.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;

import kz.carveo.mvvm.R;

public class SelectableGroup extends LinearLayout {

    private int checkedSelectableViewId = -1;

    public SelectableGroup(Context context) {
        super(context);
        init();
    }

    public SelectableGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectableGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SelectableGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            if (child instanceof SelectableView) {
                SelectableView selectableView = (SelectableView) child;

                if (selectableView.isChecked())
                    checkedSelectableViewId = selectableView.getId();

                selectableView.setOnClickListener(view -> onSelectableViewClicked((SelectableView) child));
            }
        }
    }

    public void addSelectableView(SelectableView selectableView) {
        selectableView.setOnClickListener(view -> onSelectableViewClicked(selectableView));
        addView(selectableView);
    }

    private void onSelectableViewClicked(SelectableView clickedSelectableView) {
        if (checkedSelectableViewId != -1) {
            SelectableView checkedView = findViewById(checkedSelectableViewId);
            checkedView.setChecked(false);
        }

        checkedSelectableViewId = clickedSelectableView.getId();
        clickedSelectableView.setChecked(true);
    }

    public int getCheckedSelectableViewId() {
        return checkedSelectableViewId;
    }
}
