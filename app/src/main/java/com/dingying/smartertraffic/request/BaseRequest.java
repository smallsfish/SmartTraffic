
package com.dingying.smartertraffic.request;

import android.content.Context;

import com.dingying.smartertraffic.utils.NetUtil;

public abstract class BaseRequest {
        public interface OnGetDataListener {
            void onReturn(Object data);
        }

        private NetUtil mNetUtil1;
        private String url;
        private Context context;

        public BaseRequest(Context context) {
            this.context = context;
            url = "http://"
                    + context.getSharedPreferences("ipset", 0).getString("ip", "192.168.1.131") + ":"
                    + 8080
                    + "/transportservice/type/jason/action/";
        }

        public void connec(final OnGetDataListener listener) {
            mNetUtil1 = new NetUtil();
            mNetUtil1.asynPost(url + getAddr(), getParams(), new NetUtil.ResponseListener() {

                @Override
                public void success(String result) {
                    if (listener != null) {
                        if (!result.isEmpty()) {
                            listener.onReturn(anaylizeResponse(result));
                        }
                    }
                }

                @Override
                public void error(String msg) {
                    // MyToast.getToastLong(context, msg);
                }
            });
        }

        protected abstract String getAddr();

        protected abstract String getParams();

        protected abstract Object anaylizeResponse(String responseString);
}
