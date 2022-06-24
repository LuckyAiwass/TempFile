
package android.os;
import java.util.Map;

/** {@hide} */
interface IULightsManager
{
    void setBrightness(int id, int brightness, int brightnessMode);
    void setColor(int id, int color);
    void setFlashing(int id, int color, int mode, int onMS, int offMS);
    void pulse(int id, int color, int onMS);
    void turnOff(int id);
    void setVrMode(int id, boolean enabled);
    int getBrightness(int id);
}

