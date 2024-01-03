package kz.carveo.mvvm.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kz.carveo.mvvm.R;

public class FormattedEditText extends androidx.appcompat.widget.AppCompatEditText {

    private String mFormatMask;
    private String mMaskPlaceholder;

    public FormattedEditText(@NonNull Context context) {
        super(context);
        init(null);
    }

    public FormattedEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public FormattedEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attrsSet = getContext().obtainStyledAttributes(attrs, R.styleable.FormattedEditText);
            mFormatMask = attrsSet.getString(R.styleable.FormattedEditText_mask);
            mMaskPlaceholder = attrsSet.getString(R.styleable.FormattedEditText_maskPlaceholder);
            attrsSet.recycle();
        }

        addTextChangedListener(new FormattedTextWatcher(this, mFormatMask, mMaskPlaceholder));
        setHint(mFormatMask);
    }

    private static class FormattedTextWatcher implements TextWatcher {

        private StringBuilder mCharSequence;
        private CharSequence mChar;

        private final androidx.appcompat.widget.AppCompatEditText mParent;
        private String mFormatMask;
        private final String mMaskPlaceholder;

        public FormattedTextWatcher(androidx.appcompat.widget.AppCompatEditText parent,
                                    String formatMask,
                                    String maskPlaceholder) {
            mParent = parent;
            mFormatMask = formatMask;
            mMaskPlaceholder = maskPlaceholder;
            mCharSequence = new StringBuilder();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mChar = s.subSequence(start, start + count);
            mCharSequence.append(mChar);
        }

        @Override
        public void afterTextChanged(Editable s) {
            mParent.removeTextChangedListener(this);

            mFormatMask = mFormatMask.replaceFirst("_", mChar.toString());
            mParent.setText(mFormatMask);

            mParent.addTextChangedListener(this);
        }
    }
}
