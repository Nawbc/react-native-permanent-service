// IPermanentServiceBinder.aidl
package com.deskbtm.nawb.service.permanent;
import com.deskbtm.nawb.service.permanent.IPermanentService;

interface IPermanentServiceBinder {
    void invoke(String data);

    void bind(int id, IPermanentService service);

    void unBind(int id);
}
