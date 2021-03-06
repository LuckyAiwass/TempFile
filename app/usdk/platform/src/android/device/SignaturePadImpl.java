package android.device;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IMaxqEncryptService;
import android.os.ISignatureActionListener;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
public class SignaturePadImpl {
    private String TAG = "SignaturePadImpl";
    private IMaxqEncryptService mService;
    public SignaturePadImpl() {
        IBinder b = ServiceManager.getService("maxqservice");
        mService = IMaxqEncryptService.Stub.asInterface(b);
    }
    public int startSignature(ISignatureActionListener listener, Bundle bundle) {
        if (listener != null) {
            try {
                return mService.startSignature(bundle, listener);
             } catch (RemoteException ex) {
                 Log.e(TAG, "startSignature: DeadObjectException", ex);
             }
         }
         return -1;
    }
    public int stopSignature() {
        try {
            Log.i(TAG, "stopSignature");
            return mService.stopSignature();
        } catch (RemoteException ex) {
            Log.e(TAG, "stopSignature: DeadObjectException", ex);
        }
        return -1;
    }
    /*public interface SignatureListener {
        public void handleResult(int result,int length,byte[] bitmap);
    }
    public int startSignature(SignatureListener listener, Bundle bundle) {
        if (listener != null) {
            try {
                 Log.i(TAG, "startSignature");
                 synchronized (mInputListeners) {
                     SignatureInputTransport transport = mInputListeners.get(listener);
                     if (transport == null) {
                         transport = new SignatureInputTransport(listener);
                     }
                     mInputListeners.put(listener, transport);
                     return mService.startSignature(bundle, transport);
                 }
             } catch (RemoteException ex) {
                 Log.e(TAG, "startSignature: DeadObjectException", ex);
             }
         }
         return -1;
    }
    public int stopSignature() {
        try {
            Log.i(TAG, "stopSignature");
            return mService.stopSignature();
        } catch (RemoteException ex) {
            Log.e(TAG, "stopSignature: DeadObjectException", ex);
        }
        return -1;
    }
    private HashMap<SignatureListener, SignatureInputTransport> mInputListeners = new HashMap<SignatureListener, SignatureInputTransport>();
    private class SignatureInputTransport extends ISignatureActionListener.Stub {
        private SignatureListener mListener;
        protected static final int MSG_INPUT_DATA = 1;
        protected final Handler mListenerHandler;

        public SignatureInputTransport(SignatureListener listener) {
            this.mListener = listener;
            mListenerHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                    case MSG_INPUT_DATA:
                        int result = msg.arg1;
                        int keylen = msg.arg2;
                        byte[] key = (byte[]) msg.obj;
                        mListener.handleResult(result, keylen, key);
                        break;
                    default:
                        break;
                    }
                }
            };
        }
        
        public void handleResult(int result, int keylen, byte[] key) {
            Log.d(TAG, "handleResult");
            Message msg = Message.obtain();
            msg.what = MSG_INPUT_DATA;
            msg.obj = key;
            msg.arg1 = result;
            msg.arg2 = keylen;
            mListenerHandler.removeMessages(MSG_INPUT_DATA);
            mListenerHandler.sendMessage(msg);
        }
    }*/
}
                                                                                                                   