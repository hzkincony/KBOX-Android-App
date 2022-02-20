package com.kincony.KControl.wifi;


import java.io.Serializable;

public class StringTransfer implements Serializable {

    //内容
    private String content;

    //大小
    private long size;

    //MD5码
    private String md5;

    public StringTransfer() {

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public String toString() {
        return "StringTransfer{" +
                "content='" + content + '\'' +
                ", size=" + size +
                ", md5='" + md5 + '\'' +
                '}';
    }
}