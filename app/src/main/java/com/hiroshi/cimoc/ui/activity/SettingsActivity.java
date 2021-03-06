package com.hiroshi.cimoc.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.global.Extra;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.SettingsPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.ui.activity.settings.ReaderConfigActivity;
import com.hiroshi.cimoc.ui.fragment.dialog.ChoiceDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.SliderDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.StorageEditorDialogFragment;
import com.hiroshi.cimoc.ui.view.SettingsView;
import com.hiroshi.cimoc.utils.ServiceUtils;
import com.hiroshi.cimoc.utils.StringUtils;
import com.hiroshi.cimoc.utils.ThemeUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class SettingsActivity extends BackActivity implements SettingsView {

    private static final int DIALOG_REQUEST_OTHER_LAUNCH = 0;
    private static final int DIALOG_REQUEST_READER_MODE = 1;
    private static final int DIALOG_REQUEST_OTHER_THEME = 2;
    private static final int DIALOG_REQUEST_DOWNLOAD_CONN = 3;
    private static final int DIALOG_REQUEST_OTHER_STORAGE = 4;
    private static final int DIALOG_REQUEST_DOWNLOAD_THREAD = 5;
    private static final int DIALOG_REQUEST_DOWNLOAD_DELETE = 6;
    private static final int DIALOG_REQUEST_DOWNLOAD_SCAN = 7;

    @BindViews({R.id.settings_reader_title, R.id.settings_download_title, R.id.settings_other_title, R.id.settings_search_title})
    List<TextView> mTitleList;
    @BindView(R.id.settings_layout) View mSettingsLayout;
    @BindView(R.id.settings_reader_bright_checkbox) AppCompatCheckBox mBrightBox;
    @BindView(R.id.settings_reader_hide_checkbox) AppCompatCheckBox mHideBox;
    @BindView(R.id.settings_search_complete_checkbox) AppCompatCheckBox mCompleteBox;

    private SettingsPresenter mPresenter;

    private int mLaunchChoice;
    private int mThemeChoice;
    private int mReaderModeChoice;
    private int mConnectionValue;
    private int mThreadValue;
    private String mStoragePath;
    private String mTempStorage;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new SettingsPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        super.initView();
        mLaunchChoice = mPreference.getInt(PreferenceManager.PREF_OTHER_LAUNCH, PreferenceManager.HOME_COMIC);
        mThemeChoice = mPreference.getInt(PreferenceManager.PREF_OTHER_THEME, ThemeUtils.THEME_BLUE);
        mReaderModeChoice = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
        if (CimocApplication.getDocumentFile() != null) {
            mStoragePath = CimocApplication.getDocumentFile().getUri().toString();
        }
        mConnectionValue = mPreference.getInt(PreferenceManager.PREF_DOWNLOAD_CONNECTION, 0);
        mThreadValue = mPreference.getInt(PreferenceManager.PREF_DOWNLOAD_THREAD, 1);
        mBrightBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_KEEP_ON, false));
        mHideBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_HIDE_INFO, false));
        mCompleteBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_SEARCH_COMPLETE, false));
    }

    @OnClick({R.id.settings_reader_bright_btn, R.id.settings_reader_hide_btn, R.id.settings_search_complete_btn})
    void onCheckBoxClick(View view) {
        switch (view.getId()) {
            case R.id.settings_reader_bright_btn:
                checkedAndSave(mBrightBox, PreferenceManager.PREF_READER_KEEP_ON);
                break;
            case R.id.settings_reader_hide_btn:
                checkedAndSave(mHideBox, PreferenceManager.PREF_READER_HIDE_INFO);
                break;
            case R.id.settings_search_complete_btn:
                checkedAndSave(mCompleteBox, PreferenceManager.PREF_SEARCH_COMPLETE);
                break;
        }
    }

    private void checkedAndSave(CheckBox box, String key) {
        boolean checked = !box.isChecked();
        box.setChecked(checked);
        mPreference.putBoolean(key, checked);
    }

    @OnClick(R.id.settings_reader_config_btn) void onReaderConfigBtnClick() {
        Intent intent = new Intent(this, ReaderConfigActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.settings_reader_mode_btn) void onReaderModeClick() {
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(R.string.settings_reader_mode,
                getResources().getStringArray(R.array.reader_mode_items), mReaderModeChoice, null, DIALOG_REQUEST_READER_MODE);
        fragment.show(getFragmentManager(), null);
    }

    @OnClick(R.id.settings_other_launch_btn) void onOtherLaunchClick() {
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(R.string.settings_other_launch,
                getResources().getStringArray(R.array.home_items), mLaunchChoice, null, DIALOG_REQUEST_OTHER_LAUNCH);
        fragment.show(getFragmentManager(), null);
    }

    @OnClick(R.id.settings_other_theme_btn) void onOtherThemeBtnClick() {
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(R.string.settings_other_theme,
                getResources().getStringArray(R.array.theme_items), mThemeChoice, null, DIALOG_REQUEST_OTHER_THEME);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case DIALOG_REQUEST_OTHER_STORAGE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // Todo release permission ?
                        showProgressDialog();
                        Uri uri = data.getData();
                        int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        getContentResolver().takePersistableUriPermission(uri, flags);
                        mTempStorage = uri.toString();
                        mPresenter.moveFiles(DocumentFile.fromTreeUri(this, uri));
                    } else {
                        showProgressDialog();
                        String path = data.getStringExtra(Extra.EXTRA_PICKER_PATH);
                        if (!StringUtils.isEmpty(path)) {
                            DocumentFile file = DocumentFile.fromFile(new File(path));
                            mTempStorage = file.getUri().toString();
                            mPresenter.moveFiles(file);
                        } else {
                            onExecuteFail();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        int index, value;
        switch (requestCode) {
            case DIALOG_REQUEST_READER_MODE:
                index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                mPreference.putInt(PreferenceManager.PREF_READER_MODE, index);
                mReaderModeChoice = index;
                break;
            case DIALOG_REQUEST_OTHER_LAUNCH:
                index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                mPreference.putInt(PreferenceManager.PREF_OTHER_LAUNCH, index);
                mLaunchChoice = index;
                break;
            case DIALOG_REQUEST_OTHER_THEME:
                index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                if (mThemeChoice != index) {
                    mPreference.putInt(PreferenceManager.PREF_OTHER_THEME, index);
                    mThemeChoice = index;
                    int theme = ThemeUtils.getThemeById(index);
                    setTheme(theme);
                    int primary = ThemeUtils.getResourceId(this, R.attr.colorPrimary);
                    int accent = ThemeUtils.getResourceId(this, R.attr.colorAccent);
                    changeTheme(primary, accent);
                    mPresenter.changeTheme(theme, primary, accent);
                }
                break;
            case DIALOG_REQUEST_DOWNLOAD_CONN:
                value = bundle.getInt(EXTRA_DIALOG_RESULT_VALUE);
                mPreference.putInt(PreferenceManager.PREF_DOWNLOAD_CONNECTION, value);
                mConnectionValue = value;
                break;
            case DIALOG_REQUEST_OTHER_STORAGE:
                showSnackbar(R.string.settings_other_storage_not_found);
                break;
            case DIALOG_REQUEST_DOWNLOAD_THREAD:
                value = bundle.getInt(EXTRA_DIALOG_RESULT_VALUE);
                mPreference.putInt(PreferenceManager.PREF_DOWNLOAD_THREAD, value);
                mThreadValue = value;
                break;
            case DIALOG_REQUEST_DOWNLOAD_SCAN:
                showProgressDialog();
                mPresenter.scanTask();
                break;
            case DIALOG_REQUEST_DOWNLOAD_DELETE:
                showProgressDialog();
                mPresenter.deleteTask();
                break;
        }
    }

    private void changeTheme(int primary, int accent) {
        if (mToolbar != null) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(this, primary));
        }
        for (TextView textView : mTitleList) {
            textView.setTextColor(ContextCompat.getColor(this, primary));
        }
        ColorStateList stateList = new ColorStateList(new int[][]{{ -android.R.attr.state_checked }, { android.R.attr.state_checked }},
                new int[]{0x8A000000, ContextCompat.getColor(this, accent)});
        mBrightBox.setSupportButtonTintList(stateList);
        mHideBox.setSupportButtonTintList(stateList);
        mCompleteBox.setSupportButtonTintList(stateList);
    }

    @OnClick(R.id.settings_other_storage_btn) void onOtherStorageClick() {
        if (ServiceUtils.isServiceRunning(this, DownloadService.class)) {
            showSnackbar(R.string.download_ask_stop);
        } else {
            StorageEditorDialogFragment fragment = StorageEditorDialogFragment.newInstance(R.string.settings_other_storage,
                    mStoragePath, DIALOG_REQUEST_OTHER_STORAGE);
            fragment.show(getFragmentManager(), null);
        }
    }

    @OnClick(R.id.settings_download_connection_btn) void onDownloadConnectionClick() {
        SliderDialogFragment fragment =
                SliderDialogFragment.newInstance(R.string.settings_download_connection, 0, 10, mConnectionValue, DIALOG_REQUEST_DOWNLOAD_CONN);
        fragment.show(getFragmentManager(), null);
    }

    @OnClick(R.id.settings_download_thread_btn) void onDownloadThreadClick() {
        SliderDialogFragment fragment =
                SliderDialogFragment.newInstance(R.string.settings_download_thread, 1, 10, mThreadValue, DIALOG_REQUEST_DOWNLOAD_THREAD);
        fragment.show(getFragmentManager(), null);
    }

    @OnClick(R.id.settings_download_scan_btn) void onDownloadScanClick() {
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm, R.string.settings_download_scan_confirm,
                true, null, DIALOG_REQUEST_DOWNLOAD_SCAN);
        fragment.show(getFragmentManager(), null);
    }

    @OnClick(R.id.settings_download_delete_btn) void onDownloadDeleteClick() {
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm, R.string.settings_download_delete_confirm,
                true, null, DIALOG_REQUEST_DOWNLOAD_DELETE);
        fragment.show(getFragmentManager(), null);
    }

    @OnClick(R.id.settings_other_cache_btn) void onOtherCacheClick() {
        showProgressDialog();
        mPresenter.clearCache();
        showSnackbar(R.string.common_execute_success);
        hideProgressDialog();
    }

    @Override
    public void onFileMoveSuccess() {
        hideProgressDialog();
        mPreference.putString(PreferenceManager.PREF_OTHER_STORAGE, mTempStorage);
        mStoragePath = mTempStorage;
        ((CimocApplication) getApplication()).initRootDocumentFile();
        showSnackbar(R.string.common_execute_success);
    }

    @Override
    public void onExecuteSuccess() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_success);
    }

    @Override
    public void onExecuteFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_fail);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.drawer_settings);
    }

    @Override
    protected View getLayoutView() {
        return mSettingsLayout;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_settings;
    }
    
}
