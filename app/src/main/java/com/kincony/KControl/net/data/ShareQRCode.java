package com.kincony.KControl.net.data;

import java.util.List;

public class ShareQRCode {
    private List<IPAddress> allAddress;
    private List<Device> allDevice;
    private List<Scene> allScene;

    public List<IPAddress> getAllAddress() {
        return allAddress;
    }

    public void setAllAddress(List<IPAddress> allAddress) {
        this.allAddress = allAddress;
    }

    public List<Device> getAllDevice() {
        return allDevice;
    }

    public void setAllDevice(List<Device> allDevice) {
        this.allDevice = allDevice;
    }

    public List<Scene> getAllScene() {
        return allScene;
    }

    public void setAllScene(List<Scene> allScene) {
        this.allScene = allScene;
    }
}
