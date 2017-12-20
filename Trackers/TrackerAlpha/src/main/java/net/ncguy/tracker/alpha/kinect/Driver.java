package net.ncguy.tracker.alpha.kinect;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public class Driver {

    public interface KinectLibrary extends StdCallLibrary {
        KinectLibrary INSTANCE = (KinectLibrary) Native.loadLibrary("Kinect20", KinectLibrary.class);

        //_Check_return_ HRESULT NUIAPI NuiGetSensorCount( _In_ int * pCount );
        NativeLong NuiGetSensorCount(IntByReference pCount);
    }

    public static void main(String[] args) {
        IntByReference a = new IntByReference();
        KinectLibrary.INSTANCE.NuiGetSensorCount(a);
        System.out.println("Devices: " + a.getValue());
    }

}
