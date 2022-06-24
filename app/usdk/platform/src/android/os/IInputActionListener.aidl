package android.os;

import android.os.Bundle;
/** */
oneway interface IInputActionListener 
{
    /** */
    void onInputChanged(int result, int keylen, in Bundle bundle);
}
