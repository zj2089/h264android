package com.monitorend;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class WVSS extends Activity {

	private WVSSView mWvssView;

//	private MulticastLock mMulticastLock;
	private DatagramPacketQueue mDatagramPacketQueue;
	private Dialog mCommandDialog;
	private String mCommandType;
	private String mCenterServerIp;
	private String mCurCaptureEnd;
	
	/*
	 *  the threads created by this activity
	 */
	private ReceiveNaluThread mReceiveNaluThread;
	private RecvSpsPpsThread mRecvSpsPpsThread; 
	private ProcessRtpPacketThread mProcessRtpPacketThread; 
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWvssView = new WVSSView(this, 320, 240);
        setContentView(mWvssView);
        
//        // allow this application to receive multicast packets
//		WifiManager wifi = (WifiManager) getSystemService( Context.WIFI_SERVICE );
//		mMulticastLock = wifi.createMulticastLock("receive_nalu");
//		mMulticastLock.setReferenceCounted(false);
//		mMulticastLock.acquire();
        
//        Intent intent = this.getIntent();
//        String multicastAddress = intent.getStringExtra("multicastAddress");
        
        MyApp myApp = (MyApp) getApplicationContext();
        Socket socket = myApp.getSocket();
        
        // get the global CenterServerIp
        mCenterServerIp = myApp.getCenterServerIp();
        
        // get the global Current Capture End Name
        mCurCaptureEnd = myApp.getCurCaputureEnd();
		
		mDatagramPacketQueue = new DatagramPacketQueue();
		
        mReceiveNaluThread = new ReceiveNaluThread(/*multicastAddress, */mDatagramPacketQueue);
        mRecvSpsPpsThread = new RecvSpsPpsThread(mWvssView, mReceiveNaluThread, socket);
        mRecvSpsPpsThread.start();
		
        mProcessRtpPacketThread = new ProcessRtpPacketThread(mWvssView, mDatagramPacketQueue);
        mProcessRtpPacketThread.start();
    }
    
    @Override
	protected void onStop() {
		
		super.onStop();
		
		/**
		 * force finishing the following threads
		 */
		mReceiveNaluThread.setFinished();
		mRecvSpsPpsThread.setFinished();
		mProcessRtpPacketThread.setFinished();
		
		try {
			mReceiveNaluThread.join();
			mRecvSpsPpsThread.join();
			mProcessRtpPacketThread.join();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
		Log.i("STOP", "WVSS activity stopping");
		
//		mMulticastLock.release();
	}
    

	@Override
	protected void onDestroy() {
		
		Intent intent = new Intent();
		this.setResult(MainAct.REQ_CODE_FINISHED, intent);
		
		super.onDestroy();
	}


	// Menu item IDs
    public static final int SENDCOMMAND_ID = Menu.FIRST;    
    public static final int SWITH_TO_OTHER_ID = Menu.FIRST + 1;
	private static final int SEND_COMMAND_DIALOG = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, SENDCOMMAND_ID, 0, R.string.send_command);   
        menu.add(0, SWITH_TO_OTHER_ID, 1, R.string.switch_to_other);

        return true;
    }

    @Override
	protected Dialog onCreateDialog(int id) {
		
    	switch(id) 
    	{
    	case SEND_COMMAND_DIALOG:
    		return (initCommandDialog());
    	}
		return super.onCreateDialog(id);
	}
    
    private Dialog initCommandDialog() {
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(WVSS.this);
    	LayoutInflater factory = LayoutInflater.from(this);
    	final View commandDialogView = factory.inflate(R.layout.send_command, null);
    	
    	builder.setIcon(R.drawable.icon);
    	builder.setTitle(R.string.command_title);
    	builder.setView(commandDialogView);
    	
    	final EditText argument1 = (EditText) commandDialogView.findViewById(R.id.argument1_edit);
    	final EditText argument2 = (EditText) commandDialogView.findViewById(R.id.argument2_edit);
    	final EditText argument3 = (EditText) commandDialogView.findViewById(R.id.argument3_edit);
    	
    	builder.setPositiveButton(R.string.command_ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				String localIpAddr = null;
				try {
					localIpAddr = InetAddress.getLocalHost().getHostAddress();
				} catch (UnknownHostException e) {
					Log.d("Debug", "UnknownHostException in WVSS");
					e.printStackTrace();
				}
				SendCommandThread sendCommandThread = new SendCommandThread(
						mCenterServerIp,
						localIpAddr,
						mCurCaptureEnd,
						mCommandType,
						argument1.getText().toString(),
						argument2.getText().toString(),
						argument3.getText().toString()
						);
				sendCommandThread.start();
			}
		});
    	
    	builder.setNegativeButton(R.string.command_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				// do nothing
				
			}
		});
    	
    	Spinner spinner = (Spinner) commandDialogView.findViewById(R.id.spinner_commandtype);
    	

    	
        /*
         * Create a backing mLocalAdapter for the Spinner from a list of the
         * planets. The list is defined by XML in the strings.xml file.
         */
    	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
    			WVSS.this, 
    			R.array.command_type, 
    			android.R.layout.simple_dropdown_item_1line);
    	
        /*
         * Attach the mLocalAdapter to the spinner.
         */
    	spinner.setAdapter(adapter);
    	
        /*
         * Create a listener that is triggered when Android detects the
         * user has selected an item in the Spinner.
         */

        OnItemSelectedListener spinnerListener = new myOnItemSelectedListener(WVSS.this, adapter);

        /*
         * Attach the listener to the Spinner.
         */

        spinner.setOnItemSelectedListener(spinnerListener);
    	
    	mCommandDialog = builder.create();
    	
    	return mCommandDialog;
    }
    
    class myOnItemSelectedListener implements OnItemSelectedListener {

        /*
         * provide local instances of the mLocalAdapter and the mLocalContext
         */

        ArrayAdapter<CharSequence> mLocalAdapter;
        Activity mLocalContext;

        /**
         *  Constructor
         *  @param c - The activity that displays the Spinner.
         *  @param ad - The Adapter view that
         *    controls the Spinner.
         *  Instantiate a new listener object.
         */
        public myOnItemSelectedListener(Activity c, ArrayAdapter<CharSequence> ad) {

          this.mLocalContext = c;
          this.mLocalAdapter = ad;

        }

        /**
         * When the user selects an item in the spinner, this method is invoked by the callback
         * chain. Android calls the item selected listener for the spinner, which invokes the
         * onItemSelected method.
         *
         * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(
         *  android.widget.AdapterView, android.view.View, int, long)
         * @param parent - the AdapterView for this listener
         * @param v - the View for this listener
         * @param pos - the 0-based position of the selection in the mLocalAdapter
         * @param row - the 0-based row number of the selection in the View
         */
        public void onItemSelected(AdapterView<?> parent, View v, int pos, long row) {

        	WVSS.this.mCommandType = parent.getItemAtPosition(pos).toString();
        }

        /**
         * The definition of OnItemSelectedListener requires an override
         * of onNothingSelected(), even though this implementation does not use it.
         * @param parent - The View for this Listener
         */
        public void onNothingSelected(AdapterView<?> parent) {

            // do nothing

        }
    }

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {        
	        case SENDCOMMAND_ID:
	        {	
	        	showDialog(SEND_COMMAND_DIALOG);
//	        	new ConnectCenterServerThread(mWvssView).start();
	        	
	            return true;
	        }
	        case SWITH_TO_OTHER_ID:
	        {
	        	finish();
	            return true;
	        }
        }
        return super.onOptionsItemSelected(item);
    }
}