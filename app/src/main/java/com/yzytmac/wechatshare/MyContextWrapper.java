package com.yzytmac.wechatshare;

import android.content.Context;
import android.content.ContextWrapper;

class MyContextWrapper extends ContextWrapper {
        private String pckname;

        public Context getApplicationContext() {
            return this;
        }

        public MyContextWrapper(Context context, String str) {
            super(context);
            this.pckname = str;
        }

        public String getPackageName() {
            return this.pckname;
        }

        public String getPckname() {
            return this.pckname;
        }

        public void setPckname(String str) {
            this.pckname = str;
        }
    }