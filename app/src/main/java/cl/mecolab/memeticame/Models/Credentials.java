package cl.mecolab.memeticame.Models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import cl.mecolab.memeticame.BR;

/**
 * Created by sasalatart on 8/27/16.
 */
public class Credentials extends BaseObservable {
    private String mPhoneNumber;
    private String mPassword;

    public Credentials(String phoneNumber, String password) {
        this.mPhoneNumber = phoneNumber;
        this.mPassword = password;
    }

    @Bindable
    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.mPhoneNumber = phoneNumber;
        notifyPropertyChanged(BR.phoneNumber);
    }

    @Bindable
    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        this.mPassword = password;
        notifyPropertyChanged(BR.password);
    }
}
