package com.doctorview.app.utils;

import android.util.Log;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Temporary diagnostic helper — logs how THIS app process sees the
 * network (DNS answers and a raw TCP connect to Google), so connection
 * problems are visible in logcat under the given tag.
 */
public final class NetworkDiagnostics {

    private NetworkDiagnostics() {
        // No instances
    }

    /** Runs a DNS + raw TCP test on a background thread and logs every step. */
    public static void run(String tag) {
        new Thread(() -> {
            try {
                InetAddress[] addresses = InetAddress.getAllByName("www.googleapis.com");
                for (InetAddress address : addresses) {
                    Log.w(tag, "DIAG DNS returned: " + address.getHostAddress());
                }
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(addresses[0], 443), 15000);
                Log.w(tag, "DIAG TCP connect OK to " + addresses[0].getHostAddress() + ":443");
                socket.close();
            } catch (Exception e) {
                Log.w(tag, "DIAG failed: " + e.getClass().getName() + ": " + e.getMessage());
            }
        }).start();
    }
}
