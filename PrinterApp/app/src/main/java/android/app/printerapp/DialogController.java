package android.app.printerapp;

import android.content.Context;
import android.content.DialogInterface;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


/**
 * Temporary class to show dialogs from static classes
 * @author alberto-baeza
 *
 */
public class DialogController {
	
	private Context mContext;
	
	public DialogController(Context context){
		
		mContext = context;
		
	}
	
	/**
	 * Display dialog
	 * @param msg the message shown
	 */
	public void displayDialog(String msg){
		MaterialAlertDialogBuilder madb = new MaterialAlertDialogBuilder(mContext);
		madb.setTitle(R.string.error)
				.setIcon(mContext.getResources().getDrawable(R.drawable.ic_warning_grey600_24dp))
				.setMessage(msg)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
					}
				}).show();
	}

}
