package com.example.handymic;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private EditText ipAddressEditText;
    private EditText portEditText;
    public static boolean isStreaming = false;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private PermissionCallback permissionCallback;
    private interface PermissionCallback {
        void onPermissionResult(boolean granted);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity","create app");
        ipAddressEditText = findViewById(R.id.ip_address_edit_text);
        portEditText = findViewById(R.id.port_edit_text);
        Button connectButton = findViewById(R.id.connect_button);

        connectButton.setOnClickListener(v -> {
            if (!isStreaming) {
                Log.d("MainActivity","Create audioRecorder thread!");
                Thread audioRecorder = new Thread(() -> {
                    String ipAddress = ipAddressEditText.getText().toString();;
                    int port = getPort();
                    //try (Socket server = new Socket(ipAddress, port)) { tcp
                    try (DatagramSocket socket = new DatagramSocket();) {
                        requestPermission(granted -> {
                            Log.d("MicrophoneStreamThread", "Permission now: " + granted);
                            if (granted) {
                                try {
                                    MainActivity.isStreaming = true;
                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); //udp
                                    DataOutputStream dataStream = new DataOutputStream(outputStream);//udp
                                    InetAddress serverAddress = InetAddress.getByName(ipAddress);
                                    int sampleRate = 44100; //udp
                                    int bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                                    // DataOutputStream outputStream = new DataOutputStream(server.getOutputStream()); tcp

                                    @SuppressLint("MissingPermission") AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                                    byte[] buffer = new byte[bufferSize];
                                    audioRecord.startRecording();
                                    Log.d("MicrophoneStreamThread","Start record audio!");
                                    int sum = 0;
                                    while (MainActivity.isStreaming) {
                                        int bytesRead = audioRecord.read(buffer, 0, bufferSize);
                                        outputStream.write(buffer, 0, bytesRead);
                                        byte[] data = outputStream.toByteArray(); //udp
                                        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, port);// udp
                                        Log.d("MicrophoneStreamThread",  Arrays.toString(packet.getData()));
                                        sum = sum + 1;
                                        socket.send(packet);//udp
                                        outputStream.reset();//udp
                                        //printSentData(buffer);
                                    }
                                    byte[] disconnectMsg = "Disconnect".getBytes(StandardCharsets.UTF_8);// udp
                                    DatagramPacket disconnectPacket = new DatagramPacket(disconnectMsg, disconnectMsg.length, serverAddress, port);// udp
                                    socket.send(disconnectPacket);//udp

                                    // outputStream.write("Disconnect".getBytes(StandardCharsets.UTF_8)); //tcp
                                    audioRecord.stop();
                                    audioRecord.release();
                                    outputStream.close();
                                    Log.d("MicrophoneStreamThread", "closing connection: data send:" + sum);
                                } catch (IOException e) {
                                    Log.d("MicrophoneStreamThread", "Exception: " + e);
                                } finally {
                                    isStreaming = false;
                                }
                            } else {
                                Log.d("MicrophoneStreamThread", "Die Audio-Berechtigung wurde verweigert!");
                            }
                        });
                    } catch (IOException e) {
                        Log.d("MicrophoneStreamThread", "Exception: " + e);
                    }
                });
                audioRecorder.start();

            } else {
                Log.d("LEVI", "stop streaming");
                //TODO stop streaming
                isStreaming = false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            boolean result = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            permissionCallback.onPermissionResult(result);
        }
    }

    private void requestPermission(PermissionCallback callback) {
        Log.d("MicrophoneStreamThread","Request permission!");
        this.permissionCallback = callback;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.d("MicrophoneStreamThread","Permission already granted!");
            this.permissionCallback.onPermissionResult(true);
        } else {
            Log.d("MicrophoneStreamThread","Permission not granted, asking for it!");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    private int getPort(){
        try{
            return Integer.parseInt(portEditText.getText().toString());
        } catch (NumberFormatException e){
            Log.d("Error","exception: " + e);
            return -1;
        }
    }

    public static void printSentData(byte[] data) {
        Log.d("MicrophoneStreamThread",new String(data));
    }
}