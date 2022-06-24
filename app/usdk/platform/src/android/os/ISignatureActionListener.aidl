package android.os;

import android.graphics.Bitmap;
/** */
interface ISignatureActionListener 
{
    /** */
    void handleResult(int result, int length, in byte[] signed, in Bitmap bitmap);
}
