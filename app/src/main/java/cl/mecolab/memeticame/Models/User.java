package cl.mecolab.memeticame.Models;

/**
 * Created by Andres Matte on 8/10/2016.
 */
public class User {
    public final String mName;
    public final String mPhoneNumber;

    private User(String name, String phoneNumber) {
        mName = name;
        mPhoneNumber = phoneNumber;
    }

    public static class Builder {
        private String mName;
        private String mPhoneNumber;

        public Builder name(String name) {
            mName = name;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            mPhoneNumber = phoneNumber;
            return this;
        }

        public User build() {
            return new User(mName, mPhoneNumber);
        }
    }
}
