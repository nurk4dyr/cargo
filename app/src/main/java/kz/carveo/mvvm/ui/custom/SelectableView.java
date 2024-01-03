package kz.carveo.mvvm.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import kz.carveo.mvvm.R;

public class SelectableView extends RelativeLayout implements Checkable {

    private boolean mChecked;
    private Drawable background;

    public SelectableView(Context context) {
        super(context);
        init(null);
    }

    public SelectableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SelectableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public SelectableView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray a = getContext().obtainStyledAttributes(attributeSet, R.styleable.SelectableView);
            mChecked = a.getBoolean(R.styleable.SelectableView_checked, false);
            background = a.getDrawable(R.styleable.SelectableView_android_background);
            a.recycle();
        }

        if (getId() == View.NO_ID) {
            int generatedId = generateViewId();
            setId(generatedId);
        }

        if (background != null)
            setBackground(background);
        setClickable(true);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        if (isChecked())
            mergeDrawableStates(drawableState, new int[]{android.R.attr.state_checked});

        return drawableState;
    }

    @Override
    public void setChecked(boolean b) {
        mChecked = b;
        refreshDrawableState();
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }
}
