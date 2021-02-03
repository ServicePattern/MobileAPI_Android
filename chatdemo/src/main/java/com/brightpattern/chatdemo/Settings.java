package com.brightpattern.chatdemo;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.Serializable;

public class Settings implements Serializable {
    private static final long serialVersionUID = 012;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String appID;
    private String serverAddress;
    private String tenant;
    private String clientId;

    public Settings() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneName(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public static Settings load(Context context) {
        SharedPreferences userSettings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        Settings s = new Settings();
        s.setFirstName(userSettings.getString("firstname",""));
        s.setLastName(userSettings.getString("lastname",""));
        s.setPhoneName(userSettings.getString("phonenumber",""));
        s.setAppID(userSettings.getString("appID",""));
        s.setServerAddress(userSettings.getString("serveraddress",""));
        s.setTenant(userSettings.getString("tenant",""));
        s.setClientId(userSettings.getString("clientId", ""));
        return s;
    }

    public void save(Context context) {
        SharedPreferences userSettings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = userSettings.edit();
        edit.clear();
        edit.putString("firstname", firstName);
        edit.putString("lastname", lastName);
        edit.putString("phonenumber", phoneNumber);
        edit.putString("appID", appID);
        edit.putString("serveraddress", serverAddress);
        edit.putString("tenant", tenant);
        edit.putString("clientId", clientId);
        edit.commit();
    }

    public boolean isValidConnectionSettings() {
        return !(appID.isEmpty() || serverAddress.isEmpty() || tenant.isEmpty() || clientId.isEmpty()) ;
    }
}
