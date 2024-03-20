package michal.malek.auth.models;

import lombok.Getter;

@Getter
public enum AuthResponse {

    SUCCESS{
        @Override
        public String toString() {
            return "Logged in!";
        }

        @Override
        public boolean isOk(){
            return true;
        }
    },
    FAIL{
        @Override
        public String toString() {
            return "Failed to logged in!";
        }
    };

    public boolean isOk(){
        return false;
    }
}
