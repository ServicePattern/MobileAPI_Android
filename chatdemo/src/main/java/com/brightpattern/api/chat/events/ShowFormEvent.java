package com.brightpattern.api.chat.events;


import com.brightpattern.api.data.ShowFormData;

public class ShowFormEvent extends ChatEvent {

    private ShowFormData formDate;

    protected ShowFormEvent() {
        super(Type.SHOW_FORM);
    }

    public ShowFormData getFormDate() {
        return formDate;
    }

    public static ShowFormEvent create(ShowFormData formDate) {
        ShowFormEvent event = new ShowFormEvent();
        event.formDate = formDate;
        return event;
    }
}
