
package com.dingying.smartertraffic.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


public class NetUtil {
    private static final String TAG = "Test";
    private static final int READ_TIME = 1000 * 3;
    private static final int CONNECT_TIME = 1000 * 3;
    private boolean isDebug = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    public interface ResponseListener {

        void success(String result);

        void error(String msg);
    }


    /**
     * 判断网络是否连接
     *
     * @param context 上下文对象
     * @return 网络是否连接
     */
    public static boolean isNetworkAvailable(Context context) {
        boolean isNetworkOK = false;
        try {
            ConnectivityManager conn = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (null == conn || null == conn.getActiveNetworkInfo()) {
                isNetworkOK = false;
            } else {
                isNetworkOK = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isNetworkOK;
    }

    private void asynsPost(final String urlString, final String params,
                           final ResponseListener listener) {

        new Thread() {
            public void run() {
                if (listener != null) {

                    String result = "";
                    try {
                        result = postData(urlString, params);
                    } catch (MalformedURLException e) {
                        listener.error(e + "");
                        e.printStackTrace();
                    } catch (IOException e) {
                        listener.error(e + "");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        listener.error(e + "");
                        e.printStackTrace();
                    }
                    listener.success(result);
                }
            };
        }.start();
    }

    public void asynPost(final String urlString, final String params,
                         final ResponseListener listener) {

        asynsPost(urlString, params, new ResponseListener() {

            @Override
            public void success(final String result) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.success(result);
                    }
                });
            }

            @Override
            public void error(final String msg) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.error(msg);
                    }
                });
            }
        });
    }

    public String postData(String urlString, String params) throws MalformedURLException,
            IOException, JSONException {
        String result = "";
        if (isDebug) {
            Log.d(TAG + " url:", urlString);
            Log.d(TAG + " body:", params);
        }
        HttpURLConnection mConnection = initURL(urlString);
        setURLParams(mConnection);
        sendData(params, mConnection);
        result = readData(mConnection);
        if (isDebug) {
            Log.d(TAG + "response_code:", mConnection.getResponseCode() + "");
            Log.d(TAG + "response:", result);
        }

        result = new JSONObject(result).getString("serverinfo");
        return result;
    }

    private String readData(HttpURLConnection mConnection) throws IOException {
        InputStream in = null;//字节输入流
        InputStreamReader is = null;//字符输入流
        BufferedReader mReader = null;//整行读取
        String result = "";

        try {
            in = mConnection.getInputStream();
            is = new InputStreamReader(in);
            mReader = new BufferedReader(is);
            result = "";
            String line;
            while ((line = mReader.readLine()) != null) {
                if (result.equals("")) {
                    result += line;
                } else {
                    result += "\n" + line;
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (is != null) {
                is.close();
            }
            if (mReader != null) {
                mReader.close();
            }
        }
        return result;
    }

    private void sendData(final String params, HttpURLConnection mConnection) throws IOException,
            UnsupportedEncodingException {
        OutputStream os = null;
        OutputStreamWriter osw = null;

        try {
            os = mConnection.getOutputStream();
            osw = new OutputStreamWriter(os, "utf-8");
            osw.write(params);
            osw.flush();
        } finally {
            if (os != null) {
                os.close();
            }
            if (osw != null) {
                osw.close();
            }
        }
    }

    private void setURLParams(HttpURLConnection mConnection) throws ProtocolException {
        mConnection.setRequestProperty("accept", "*/*");//同意所以文件类型
        mConnection.setRequestProperty("connection", "Keep-Alive");//连接方式
        mConnection.setRequestMethod("POST");//以post形式发送
        mConnection.setRequestProperty("Content-Type", "text/html; charset=UTF-8");//发送格式
        mConnection.setConnectTimeout(CONNECT_TIME);//连接超时
        mConnection.setReadTimeout(READ_TIME);//读取超时
        mConnection.setDoOutput(true);
        mConnection.setDoInput(true);
    }

    private HttpURLConnection initURL(String urlString) throws MalformedURLException,
            IOException {
        URL mUrl;
        HttpURLConnection mConnection;

        mUrl = new URL(urlString);
        mConnection = (HttpURLConnection) mUrl.openConnection();
        return mConnection;
    }
}
