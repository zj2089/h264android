package com.monitorend;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainAct extends Activity {

	private EditText mEditText = null;
	private Button mButton = null;
	private TextView mTextView2 = null;
	private List<String> mAvailableCaptureEndList;
	private ConnectCenterServerThread mConnectCenterServerThread;
	private Handler mHandler;
	private Intent mIntent;

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
					mTextView2.setText(msgStr);
				}
				else {
					mButton.showContextMenu();
				}
			}
			
		};
	}
	
	class ButtonClickListener implements OnClickListener {
		
		private MainAct mActivity;
		
		public ButtonClickListener(MainAct act) {
			mActivity = act;
		}

		@Override
		public void onClick(View v) {
			
			Log.d("CLick", "click button");
			mConnectCenterServerThread = new ConnectCenterServerThread(
					mAvailableCaptureEndList, 
					mActivity,
					mEditText.getText().toString(),
					mHandler,
					mIntent);
			mConnectCenterServerThread.start();
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		mConnectCenterServerThread.setMonitorEndChoice(item.getTitle().toString());
		return super.onContextItemSelected(item);
	}
	
	class ButtonContextMenuListener implements Button.OnCreateContextMenuListener {

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {

			menu.setHeaderTitle(R.string.context_menu_header);
			for(int i = 0; i<mAvailableCaptureEndList.size(); i++) {
				menu.add(0, i, i, mAvailableCaptureEndList.get(i));
			}
			
		}
	}
}
