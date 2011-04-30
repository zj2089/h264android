package com.monitorend;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainAct extends Activity {

	protected static final int AVAILABLE_CAPTURE_END = 0;
	public static final int REQ_CODE_FINISHED = 2011;
	private EditText mEditText = null;
	private Button mButton = null;
	private TextView mTextView2 = null;
	private List<String> mAvailableCaptureEndList;
	private RequestMonitoringThread mRequestMonitoringThread;
	private Handler mHandler;
	private Intent mIntent;
	
	// the current capture end name monitor end selects
	private String mSelectCaptureEndName = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mButton = (Button) findViewById(R.id.button1);
		mTextView2 = (TextView) findViewById(R.id.textView2);
		mEditText = (EditText) findViewById(R.id.editText1);
		mAvailableCaptureEndList = new ArrayList<String>();
		mIntent = new Intent(this, WVSS.class);

		mButton.setOnClickListener(new ButtonClickListener(this));
		mButton.setOnCreateContextMenuListener( new ButtonContextMenuListener());

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				
				Bundle bundle = msg.getData();
				
				String senderStr = bundle.getCharSequence("Sender").toString();
				String msgStr = bundle.getCharSequence("Msg").toString();
				
				Log.d("MSG", "receive msg");
				
				if(senderStr.equals("mTextView2")) {
					mTextView2.append(msgStr + "\n");
				}
				else {
//					mButton.showContextMenu();
					showDialog(AVAILABLE_CAPTURE_END);
				}
			}
			
		};
	}
	
	@Override
	protected void onStop() {
		
		super.onStop();
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		/*
		 * the current requestCode is legal
		 */
		if( REQ_CODE_FINISHED == requestCode ) {
			
			String centerServerIp = mEditText.getText().toString();
			
			/*
			 * quit monitoring the current capture end
			 */
			QuitMonitoringThread quitMonitoringThread = new QuitMonitoringThread(
					centerServerIp,
					mSelectCaptureEndName);
			quitMonitoringThread.start();
			
			try {
				quitMonitoringThread.join();
			} catch (InterruptedException e) {
				Log.d("Debug", "InterruptedException in MainAct");
				e.printStackTrace();
			}
			
			/*
			 * update the available capture end list
			 */
			mRequestMonitoringThread = new RequestMonitoringThread(
					mAvailableCaptureEndList, 
					this,
					centerServerIp,
					mHandler,
					mIntent);
			mRequestMonitoringThread.start();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) 
		{
		case AVAILABLE_CAPTURE_END:
			return initCaptureEndDialog(mAvailableCaptureEndList);
		}
		return super.onCreateDialog(id);
	}
	
	private Dialog initCaptureEndDialog(final List<String> capList) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainAct.this);
		
		if( 0 == capList.size()) {
			builder.setTitle(R.string.no_available_capture_end_title);
			builder.setMessage(R.string.no_available_capture_end_msg);
		}
		else {
			builder.setTitle(R.string.select_capture_end);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					MainAct.this, 
					R.array.available_capture_end, 
					capList);
			builder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					MainAct.this.mSelectCaptureEndName = capList.get(which);
				}
			});
			
			builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					mRequestMonitoringThread.setMonitorEndChoice(mSelectCaptureEndName);
					((MyApp)getApplicationContext()).setCurCaptureEnd(mSelectCaptureEndName);
				}
			});
			
			builder.setNegativeButton(R.string.CANCLE, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					mSelectCaptureEndName = null;
					mRequestMonitoringThread.setMonitorEndChoice(mSelectCaptureEndName);
					((MyApp)getApplicationContext()).setCurCaptureEnd(mSelectCaptureEndName);
				}
			});
		}
		
		Dialog capDialog = builder.create();
		return capDialog;
	}


	class ButtonClickListener implements OnClickListener {
		
		private MainAct mActivity;
		
		public ButtonClickListener(MainAct act) {
			mActivity = act;
		}

		@Override
		public void onClick(View v) {
			
			/*
			 *  set the global variable : CenterServerIp
			 */
			((MyApp)getApplicationContext()).setCenterServerIp(mEditText.getText().toString());
			
			mAvailableCaptureEndList.clear();
			
			/*
			 * update the available capture end list
			 */
			mRequestMonitoringThread = new RequestMonitoringThread(
					mAvailableCaptureEndList, 
					mActivity,
					mEditText.getText().toString(),
					mHandler,
					mIntent);
			mRequestMonitoringThread.start();
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		String selectedItem = item.getTitle().toString();
		mRequestMonitoringThread.setMonitorEndChoice(selectedItem);
		((MyApp)getApplicationContext()).setCurCaptureEnd(selectedItem);
		return super.onContextItemSelected(item);
	}
	
	class ButtonContextMenuListener implements Button.OnCreateContextMenuListener {

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			
			menu.clear();

			menu.setHeaderTitle(R.string.context_menu_header);
			for(int i = 0; i<mAvailableCaptureEndList.size(); i++) {
				menu.add(0, i, i, mAvailableCaptureEndList.get(i));
			}
			
		}
	}
}
