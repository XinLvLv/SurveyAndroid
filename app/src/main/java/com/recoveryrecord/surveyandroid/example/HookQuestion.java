package com.recoveryrecord.surveyandroid.example;

public class HookQuestion {
    String on_hook_id;
    String on_hook_value;
    String second_hook_value;
    HookQuestion(){
        this.second_hook_value = "null";
    }
    public String getOn_hook_id() {
        return on_hook_id;
    }

    public void setOn_hook_id(String on_hook_id) {
        this.on_hook_id = on_hook_id;
    }

    public void setOn_hook_value(String on_hook_value) {
        this.on_hook_value = on_hook_value;
    }

    public String getOn_hook_value(){
        return on_hook_value;
    }

    public String getSecond_hook_value(){return second_hook_value;}

    public void setSecond_hook_value(String second_hook_value){this.second_hook_value = second_hook_value;}
}
