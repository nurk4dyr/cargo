package kz.carveo.mvvm.ui.base;

import android.content.Context;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewbinding.ViewBinding;

import kz.carveo.mvvm.R;

public abstract class BaseActivity<BINDING extends ViewBinding, VM extends BaseViewModel> extends AppCompatActivity {

    protected BINDING mBinding;
    protected VM mViewModel;
    protected Context mContext;

    protected abstract BINDING createViewBinding(LayoutInflater layoutInflater);
    protected abstract VM createViewModel();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_CarveoMVVM);
        mBinding = createViewBinding(LayoutInflater.from(this));
        mContext = getApplicationContext();
        setContentView(mBinding.getRoot());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected void setInsetsOn(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();

            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.topMargin = insets.top;
            mlp.leftMargin = insets.left;
            mlp.bottomMargin = insets.bottom;
            mlp.rightMargin = insets.right;
            v.setLayoutParams(mlp);

            return WindowInsetsCompat.CONSUMED;
        });
    }
}
