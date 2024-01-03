package kz.carveo.mvvm.ui.login;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import kz.carveo.mvvm.R;
import kz.carveo.mvvm.databinding.ActivityLoginBinding;
import kz.carveo.mvvm.ui.base.BaseActivity;
import kz.carveo.mvvm.ui.base.BaseViewModel;
import kz.carveo.mvvm.ui.main.MapActivity;

public class LoginActivity extends BaseActivity<ActivityLoginBinding, LoginViewModel> {

    private BottomSheetBehavior bsRegistration;
    private BottomSheetBehavior bsAuth;

    @Override
    protected ActivityLoginBinding createViewBinding(LayoutInflater layoutInflater) {
        return ActivityLoginBinding.inflate(layoutInflater);
    }

    @Override
    protected LoginViewModel createViewModel() {
        return new ViewModelProvider(this).get(LoginViewModel.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        bsRegistration = BottomSheetBehavior.from(mBinding.bsRegistration.getRoot());
        bsRegistration.setState(BottomSheetBehavior.STATE_HIDDEN);
        bsRegistration.setPeekHeight(displayMetrics.heightPixels);

        bsAuth = BottomSheetBehavior.from(mBinding.bsAuth.getRoot());
        bsAuth.setState(BottomSheetBehavior.STATE_HIDDEN);
        bsAuth.setPeekHeight(displayMetrics.heightPixels);

        setListeners();
        setInsetsOn(mBinding.loginUi);
    }

    private void setListeners() {
        mBinding.regButton.setOnClickListener(view -> {
            bsRegistration.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        mBinding.authButton.setOnClickListener(view -> {
            bsAuth.setState(BottomSheetBehavior.STATE_EXPANDED);
        });



        mBinding.bsRegistration.bsBackbutton.setOnClickListener(view -> {
            bsRegistration.setState(BottomSheetBehavior.STATE_HIDDEN);
        });
        mBinding.bsAuth.bsBackbutton.setOnClickListener(view -> {
            bsAuth.setState(BottomSheetBehavior.STATE_HIDDEN);
        });
    }
}