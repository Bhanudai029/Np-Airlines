package com.npairlines.ui.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity<VM extends BaseViewModel> extends AppCompatActivity {

    protected VM viewModel;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = getViewModel();
        
        if (viewModel != null) {
            setupObservers();
        }
    }

    protected abstract VM getViewModel();

    protected void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) showLoading();
            else hideLoading();
        });

        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null) {
                showError(message);
            }
        });
    }

    protected void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    protected void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    protected void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
