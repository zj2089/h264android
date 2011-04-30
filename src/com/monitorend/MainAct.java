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
	private EditText mCenterServerIpEdit = null;
	private Button mConnectButton = null;
	private TextView mMsgLogView = null;
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

		mConnectButton = (Button) findViewById(R.id.connect_button);
		mMsgLogView = (TextView) findViewById(R.id.msg_log_view);
		mCenterServerIpEdit = (EditText) findViewById(R.id.center_server_ip_edit);
		mAvailableCaptureEndList = new ArrayList<String>();
		mIntent = new Intent(this, WVSS.class);

		mConnectButton.setOnClickListener(new ButtonClickListener(this));
		mConnectButton.setOnCreateContextMenuListener( new ButtonContextMenuListener());

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				
				Bundle bundle = msg.getData();
				
				String senderStr = bundle.getCharSequence("Sender").toString();
				String msgStr = bundle.getCharSequence("Msg").toString();
				
				Log.d("MSG", "receive msg");
				
				if(senderStr.equals("mTextView2")) {
					mMsgLogView.append(msgStr + "\n");
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
			
			String centerServerIp = mCenterServerIpEdit.getText().toString();
			
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
			return (initCaptureEndDialog());
		default:
			Log.d("Debug", "new switch?");
			return null;
		}
		
	}
	
	private Dialog initCaptureEndDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainAct.this);
		
		builder.setIcon(R.drawable.alert_dialog_icon);
		if( 0 == mAvailableCaptureEndList.size()) {
			builder.setTitle(R.string.no_available_capture_end_title);
			builder.setMessage(R.string.no_available_capture_end_msg);
		}
		else {
			
			// set the default selection
			MainAct.this.mSelectCaptureEndName = mAvailableCaptureEndList.get(0);
			
			builder.setTitle(R.string.select_capture_end);
			String[] strCapList = mAvailableCaptureEndList.toArray(new String[0]);
			builder.setSingleChoiceItems(strCapList, 0, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					MainAct.this.mSelectCaptureEndName = mAvailableCaptureEndList.get(which);
				}
			});
			
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					mRequestMonitoringThread.setMonitorEndChoice(mSelectCaptureEndName);
					((MyApp)getApplicationContext()).setCurCaptureEnd(mSelectCaptureEndName);
				}
			});
			
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				
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
			((MyApp)getApplicationContext()).setCenterServerIp(mCenterServerIpEdit.getText().toString());
			
			mAvailableCaptureEndList.clear();
			
			/*
			 * update the available capture end list
			 */
			mRequestMonitoringThread = new RequestMonitoringThread(
					mAvailableCaptureEndList, 
					mActivity,
					mCenterServerIpEdit.getText().toString(),
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
