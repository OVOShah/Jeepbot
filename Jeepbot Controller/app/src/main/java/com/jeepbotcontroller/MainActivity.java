/*
 * Released under MIT License http://opensource.org/licenses/MIT
 * Copyright (c) 2013 Plasty Grove
 * Refer to file LICENSE or URL above for full text 
 */

package com.jeepbotcontroller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends Activity implements View.OnTouchListener{

	private static final String TAG = "BlueTest5-MainActivity";
	private UUID mDeviceUUID;
	private BluetoothSocket mBTSocket;

	private boolean mIsUserInitiatedDisconnect = false;

	// All controls here
	private Button btnLights;
    private ImageView buttonUp, buttonDown, buttonRight, buttonLeft;

	private boolean mIsBluetoothConnected = false;

	private BluetoothDevice mDevice;

	private ProgressDialog progressDialog;

    public boolean up, down, right, left;
    public boolean lights = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActivityHelper.initialize(this);

		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		mDevice = b.getParcelable(Homescreen.DEVICE_EXTRA);
		mDeviceUUID = UUID.fromString(b.getString(Homescreen.DEVICE_UUID));

		Log.d(TAG, "Ready");

		btnLights = (Button) findViewById(R.id.btnLights);
        btnLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!lights){
                    lights = true;
                    sendSignal('W');
                }
                else {
                    lights = false;
                    sendSignal('w');
                }
            }
        });

        buttonUp = (ImageView) findViewById(R.id.buttonUp);
        buttonDown = (ImageView) findViewById(R.id.buttonDown);
        buttonLeft = (ImageView) findViewById(R.id.buttonLeft);
        buttonRight = (ImageView) findViewById(R.id.buttonRight);

        buttonUp.setOnTouchListener(this);
        buttonRight.setOnTouchListener(this);
        buttonLeft.setOnTouchListener(this);
        buttonDown.setOnTouchListener(this);
	}

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.i("SWITCHCASE", "RUN");
            if (v.getId() == R.id.buttonDown) {
                down = true;
                Log.i("signal", "DOWN");
            }
            else if (v.getId() == R.id.buttonRight) {
                right = true;
                Log.i("signal", "RIGHT");
            }
            else if (v.getId() == R.id.buttonLeft) {
                left = true;
                Log.i("signal", "LEFT");
            }
            else if (v.getId() == R.id.buttonUp) {
                up = true;
                Log.i("signal", "UP");
            }

            determineSignal(up,down,left,right);
        }

        if (event.getAction() == MotionEvent.ACTION_UP){
            if (v.getId() == R.id.buttonDown) {
                down = false;
                Log.i("signal", "DOWN");
            }
            else if (v.getId() == R.id.buttonRight) {
                right = false;
                Log.i("signal", "RIGHT");
            }
            else if (v.getId() == R.id.buttonLeft) {
                left = false;
                Log.i("signal", "LEFT");
            }
            else if (v.getId() == R.id.buttonUp) {
                up = false;
                Log.i("signal", "UP");
            }

            determineSignal(up,down,left,right);
        }

        return true;
    }

    public void determineSignal(Boolean up, Boolean down, Boolean left, Boolean right){

        if (up && !left && !right){sendSignal('F');}
        else if (down && !right && !left){sendSignal('B');}
        else if (right && !up && !down){sendSignal('R');}
        else if (left && !up && !down){sendSignal('L');}
        else if (!left && !up && !down && !right){sendSignal('S');}
        else if (left && up){sendSignal('G');}
        else if (right && up){sendSignal('I');}
        else if (down && left){sendSignal('H');}
        else if (down && right){sendSignal('J');}

    }

    public void sendSignal(char signal){
        Log.i("signal","SENDING SIGNAL " + signal);
        try {
            mBTSocket.getOutputStream().write(signal);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(Void... params) {

			try {
				mBTSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mIsBluetoothConnected = false;
			if (mIsUserInitiatedDisconnect) {
				finish();
			}
		}

	}

	private void msg(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPause() {
		if (mBTSocket != null && mIsBluetoothConnected) {
			new DisConnectBT().execute();
		}
		Log.d(TAG, "Paused");
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (mBTSocket == null || !mIsBluetoothConnected) {
			new ConnectBT().execute();
		}
		Log.d(TAG, "Resumed");
		super.onResume();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "Stopped");
		super.onStop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	private class ConnectBT extends AsyncTask<Void, Void, Void> {
		private boolean mConnectSuccessful = true;

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(MainActivity.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
		}

		@Override
		protected Void doInBackground(Void... devices) {

			try {
				if (mBTSocket == null || !mIsBluetoothConnected) {
					mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
					BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
					mBTSocket.connect();
				}
			} catch (IOException e) {
				// Unable to connect to device
				e.printStackTrace();
				mConnectSuccessful = false;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (!mConnectSuccessful) {
				Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
				finish();
			} else {
				msg("Connected to device");
				mIsBluetoothConnected = true;
			}

			progressDialog.dismiss();
		}

	}

}
