package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.view.DialogView;

/**
 * Created by Hiroshi on 2016/10/16.
 */

public class ChoiceDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private String[] mItems;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mItems = getArguments().getStringArray(DialogView.EXTRA_DIALOG_ITEMS);
        int choice = getArguments().getInt(DialogView.EXTRA_DIALOG_CHOICE_ITEMS);
        builder.setTitle(getArguments().getInt(DialogView.EXTRA_DIALOG_TITLE))
                .setSingleChoiceItems(mItems, choice, null)
                .setPositiveButton(R.string.dialog_positive, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        int index = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();
        String value = index == -1 ? null : mItems[index];
        int requestCode = getArguments().getInt(DialogView.EXTRA_DIALOG_REQUEST_CODE);
        Bundle bundle = new Bundle();
        bundle.putBundle(DialogView.EXTRA_DIALOG_BUNDLE, getArguments().getBundle(DialogView.EXTRA_DIALOG_BUNDLE));
        bundle.putInt(DialogView.EXTRA_DIALOG_RESULT_INDEX, index);
        bundle.putString(DialogView.EXTRA_DIALOG_RESULT_VALUE, value);
        DialogView target = (DialogView) (getTargetFragment() != null ? getTargetFragment() : getActivity());
        target.onDialogResult(requestCode, bundle);
    }

    public static ChoiceDialogFragment newInstance(int title, String[] item, int choice, Bundle extra, int requestCode) {
        ChoiceDialogFragment fragment = new ChoiceDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogView.EXTRA_DIALOG_TITLE, title);
        bundle.putStringArray(DialogView.EXTRA_DIALOG_ITEMS, item);
        bundle.putInt(DialogView.EXTRA_DIALOG_CHOICE_ITEMS, choice);
        bundle.putBundle(DialogView.EXTRA_DIALOG_BUNDLE, extra);
        bundle.putInt(DialogView.EXTRA_DIALOG_REQUEST_CODE, requestCode);
        fragment.setArguments(bundle);
        return fragment;
    }

}
